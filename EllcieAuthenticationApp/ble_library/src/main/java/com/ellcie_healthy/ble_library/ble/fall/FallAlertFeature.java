package com.ellcie_healthy.ble_library.ble.fall;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ellcie_healthy.ble_library.ble.profile.command.data.FallState;
import com.ellcie_healthy.ble_library.ble.service.EHBleForegroundService;
import com.ellcie_healthy.ble_library.ble.utils.Utils;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetBoolean;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetInteger;
import com.ellcie_healthy.common.converters.Converters;
import com.ellcie_healthy.common.networks.INetworkSubscriber;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase.FallEvent;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase.FallObject;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase.IFirebaseDb;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase.VitarisEvent;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.models.Contact;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.networks.NetworkManager;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.voice.VoiceMessageManager;

import java.util.Map;
import java.util.Objects;


public class FallAlertFeature extends Feature implements INetworkSubscriber {

    private static final String TAG = "FallAlertFeature";
    public static final int DEFAULT_SOS_VALUE = 15;

    private final long DEFAULT_DELTA_SECONDS = 30;

    private boolean mFallAlertObserved = false;
    private FallObject mFallObject;

    private EllcieCommonCallbackGetBoolean mCbNoAlert;
    private EllcieCommonCallbackGetInteger mCbAlertEngaged;
    private EllcieCommonCallbackGetGeneric<RescueEvent> mCbAlertConfirmed;

    private String mLastLatitudePosition = "";
    private String mLastLongitudePosition = "";
    private long mLastLocationReceivedTs = 0;

    private final Context mContext;

    private long mTimeStampDisconnected;
    private long mTimeStampCancelled;

    private final EHBleForegroundService.EHBinder mServiceBinder;

    private IFirebaseDb mFirebaseDb;

    public FallAlertFeature(Context context, EHBleForegroundService.EHBinder binder) {
        mContext = context;
        mServiceBinder = binder;

        VoiceMessageManager.Companion.getInstance(mContext); // Call this method in order to be sure all process is ready for first fall (voice was able to not be initialized without this call here)

    }

    public void setTimeStampDisconnected(long timeStampDisconnected){
        this.mTimeStampDisconnected = timeStampDisconnected;
    }

    public void setFirebaseDb(IFirebaseDb db){
        mFirebaseDb = db;
    }


    @Override
    protected void onStartFeature() {
        NetworkManager.addSubscriber(mContext.getApplicationContext(), this);
    }

    @Override
    protected void onStopFeature() {
        NetworkManager.removeSubscriber(this);
    }

    /**
     * Called when the dashboard can be displayed (alert cancelled, no alert...)
     */
    public void setListenerNoAlert(EllcieCommonCallbackGetBoolean cb) {
        mCbNoAlert = cb;
    }

    /**
     * Called when the confirmation page can be displayed (alert cancelled, no alert...)
     */
    public void setListenerAlertEngaged(EllcieCommonCallbackGetInteger cb) {
        mCbAlertEngaged = cb;
    }

    public void setListenerNoAlertConfirmed(EllcieCommonCallbackGetGeneric<RescueEvent> cb) {
        mCbAlertConfirmed = cb;
    }

    public boolean isFallAlertInProgress() {
        return mFallObject != null;
    }

    public boolean isCountdownAlertActive() {
        return !mFallAlertObserved;
    }

    public void appStarted() {
        Logger.d(TAG, "appStarted()");
        if (!mFallAlertObserved) {
            Logger.d(TAG, "appStarted: fall not observed");
            alertAlreadyOngoing(fallObj -> {
                if (fallObj != null && fallObj.hasRescueEngaged()) {
                    mFallObject = fallObj;
                    listenOngoingRescueAlert(); // notify and fill fallObject

                    //go to waiting for help page
                    mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.NO_EVENT));

                } else {
                    Logger.d(TAG, "appStarted: NO rescue engaged");
                    mFallObject = null;
                    // do nothing.
                }
            });
        }
    }

    public void onNetworkStateChanged(boolean connected) {
        Logger.d(TAG, "onNetworkStateChanged()");
    }

    //Notif received when there is a DB update in the phase2Rescue node
    //Ex : alert is acquited by helper, emergency is contacted etc.

    public void onOngoingFallAlertChanged(@Nullable FallObject ongoingFallObj) {
        Logger.d(TAG, "onOngoingFallAlertChanged()");

        if(ongoingFallObj == null){
            return;
        }

        Logger.d(TAG, "onOngoingFallAlertChanged: fallObj: " + ongoingFallObj.toString());
        mFallObject = ongoingFallObj;
        if (!Objects.equals(mFallObject.getRescueEndType(), "")) {
            Logger.d(TAG, "onOngoingFallAlertChanged: rescue end type not null : " + mFallObject.getRescueEndType());
            removeListenerOngoingRescueAlert();

            if (mFallObject.alertIsCanceled()) {

                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    if (Objects.equals(mFallObject.getTriggerType(), "SOS")) {
                        mServiceBinder.cancelSosFallAlert();
                    } else {
                        mServiceBinder.cancelFallAlert();
                    }
                }
                mFallObject = null;

                //go to dashboard
                mCbNoAlert.done(true);

            } else if (mFallObject.acquittedByHelper()) {
                if (mFallObject.retrieveHelperAck() != null ) {
                    String displayName = mFallObject.retrieveHelperAck().getDisplayName();
                    mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.HELPER_ACK,
                            displayName));
                }

                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    mServiceBinder.helperAcquittal();
                }
                //Notify
            } else if (mFallObject.acquittedByVitaris()) {
                FallEvent event = mFallObject.getVitarisReasonAck();
                if (event != null) {

                    if (event.getEventType().equals(VitarisEvent.VITARIS_HELPER_ACK.getValue())) {
                        // Ack by helper, because an helper has been contacted by Vitaris and this helper take in charge.
                        int helperAckIndex;
                        helperAckIndex = Integer.parseInt(event.getPayload()); // contains the index of the helper index: [1..4]
                        Contact contact = mFallObject.retrieveHelperByPriority(helperAckIndex - 1); // index: [0..3]

                        assert contact != null;
                        mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.VITARIS_ACK,
                                event.getEventType(), contact.getDisplayName()));
                    } else {
                        mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.VITARIS_ACK,
                                event.getEventType(), ""));
                    }
                }
                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    mServiceBinder.helperAcquittal();
                }
            } else {
                mFallObject = null;
                //go to dashboard
                mCbNoAlert.done(true);
            }
            mFallObject = null;

        } else {
            //No rescue end type, check last person contacted
            Logger.d(TAG, "onOngoingFallAlertChanged: check last person contacted");
            if (mFallObject != null && mFallObject.isVitarisContacted()) {
                mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.ASSISTER_CONTACTED));
            } else if (mFallObject != null && mFallObject.retrieveLastHelperContacted() != null) {
                //check last person contacted.

                Contact lastContact = mFallObject.retrieveLastHelperContacted();
                if(lastContact != null) {
                    Logger.d(TAG, "onOngoingFallAlertChanged: last person contacted: " + lastContact.getDisplayName());
                    mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.LAST_CONTACTED_HELPER,
                            lastContact.getDisplayName()));
                } else {
                    Logger.d(TAG, "onGoingFallAlertChanged: last contact doesn't exist");
                }
            } else {
                Logger.d(TAG, "onOngoingFallAlertChanged: last person doesn't exists");
            }
            //   if contacted
           /* NotificationCenter.default.post(name: notifFallUiActionRequired, object: self,
                                            userInfo: ["action": "WAITING_FOR_HELP", "timing": 0]) // go to dashboard page*/
        }
    }

    public void onSerialNumberReceived() {
        Logger.d(TAG, "onSerialNumberChanged()");



        if (mFallAlertObserved) {
            if (mFallObject != null && Objects.equals(mFallObject.getTriggerType(), "SOS")) {
                mServiceBinder.confirmSosFall();
            } else {
                mServiceBinder.confirmAlert();
            }
        }
    }

    private void writeFallObjToFb(Map<String, Object> dico, long triggerDate) {
        if(mFirebaseDb != null) {
            mFirebaseDb.writeFallObject(dico, String.valueOf(triggerDate));
        }
    }

    private void writeFallObjToFb() {
        if (mFallObject != null && mFirebaseDb != null ) {
            mFirebaseDb.writeFallObject(mFallObject.convertToFirebase(), String.valueOf(mFallObject.getTriggerDate()));
        }
    }

    private void writeLastFallAlert() {
        if (mFallObject != null && mFirebaseDb != null) {
            mFirebaseDb.writeLastFallAlert(mFallObject.getTriggerDate());
        }
    }



        //Called when status is checked at connection and at fall status changes deteced by the glasses
    public void onFallStatusChanged(byte status, long timestampMs, boolean isNotif) {
        // check the fall status
        Logger.d(TAG, "onFallStatusChanged()");

        long deltaSeconds = (System.currentTimeMillis() - timestampMs) / 1000;

        Logger.d(TAG, "onFallStatusChanged, timestamp(ms) received -> " + timestampMs);
        Logger.d(TAG, "onFallStatusChanged, status -> " + Converters.getHexValue(status));
        Logger.d(TAG, "onFallStatusChanged, elapsed seconds -> " + deltaSeconds);

        if(status == FallState.NO_EVENT.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : No event : go to dashboard");

            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.NO_EVENT.getCode(), timestampMs);
            }

            if (mFallAlertObserved) {
                Logger.d(TAG, "onFallStatusChanged : No event : but an alert is ongoing");
                if (mFallObject != null) {
                    Logger.d(TAG, "onFallStatusChanged : No event : but an alert is ongoing");
                    if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                        if (Objects.equals(mFallObject.getTriggerType(), "SOS")) {
                            mServiceBinder.confirmSosFall();
                        } else {
                            mServiceBinder.confirmAlert();
                        }
                    }
                }
            } else if (mFallObject != null) {

                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    mServiceBinder.engageSosFall();
                }
            } else {
                // do nothing.
                //go to dashboard
                mCbNoAlert.done(true);
                mFallObject = null;
            }
        } else if(status == FallState.FALL_ENGAGED.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : Fall detection engaged : go to waiting for help page");
            if (deltaSeconds > DEFAULT_DELTA_SECONDS || isNotif) {
                // normally the delta is in range [0..30], but if the delta is upper (with security marge) we set the delta to 0
                deltaSeconds = DEFAULT_DELTA_SECONDS;
            }

            fallEngaged(timestampMs, deltaSeconds);
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.FALL_ENGAGED.getCode(), timestampMs);
            }
        } else if(status == FallState.FALL_RECOVERY.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : Fall Recovery : go to dashboard");
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.NO_EVENT.getCode(), timestampMs);
            }

            // event
            if (mFallAlertObserved && mFallObject != null) {
                VoiceMessageManager.Companion.getInstance(mContext).stopSpeaking(); //stop speaking if needed

                Logger.d(TAG, "onFallStatusChanged: fall alert observed");
                mFallObject.setRescueEndDate(timestampMs);
                mFallObject.setRescueEndType("RECOVERY");

                Map<String, Object> dicoFall = mFallObject.convertToFirebase();
                //Add event cancel to phase2Rescue node
                //noinspection unchecked
                Map<String, Object> phase2 = (Map<String, Object>) dicoFall.get("phase2Rescue");
                if (phase2 != null) {
                    phase2.put("events/" + timestampMs + "/RECOVERY", "");
                    dicoFall.put("phase2Rescue", phase2);
                }
                writeFallObjToFb(dicoFall, mFallObject.getTriggerDate());
                removeListenerOngoingRescueAlert();
            } else if (mFallObject != null) {
                VoiceMessageManager.Companion.getInstance(mContext).stopSpeaking(); //stop speaking if needed
                mFallObject.setEndType("RECOVERY");
                if (Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    mFallObject.setEndDevice(mServiceBinder.getSerialNumber());
                }
                mFallObject.setEndDate(timestampMs);
                writeLastFallAlert();
                writeFallObjToFb();
            }
            mFallObject = null;

            //go to dashboard.
            mCbNoAlert.done(true);
        } else if(status == FallState.FALL_CONFIRMED.getCode()) {
            if(mTimeStampCancelled > 0 && mTimeStampDisconnected > 0 ){//
                Log.d(TAG, "SOS has already been cancelled by user when glasses were not connected");
                mServiceBinder.cancelSosFallAlert();

                mTimeStampCancelled = 0;
                mTimeStampDisconnected = 0;
                mFallObject = null;
                return;
            }
            Logger.d(TAG, "onFallStatusChanged : Fall confirmed from glasses : go to waiting for help page");
            fallConfirmed(timestampMs, "GLASSES");
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.FALL_CONFIRMED.getCode(), timestampMs);
            }
        } else if (status == FallState.FALL_CONFIRMED_APP.getCode()) {
            Logger.d(TAG, "onFallStatusChanged Fall confirmed from mobile : go to waiting for help page");
            fallConfirmed(timestampMs, "MOBILE");
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.FALL_CONFIRMED_APP.getCode(), timestampMs);
            }
        } else if (status == FallState.FALL_CANCELLED.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : Fall cancelled : go to dashboard");
            VoiceMessageManager.Companion.getInstance(mContext).stopSpeaking(); //stop speaking if needed
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.NO_EVENT.getCode(), timestampMs);
            }
            // cancel possible only if the alert is not confirmed
            // not possible to cancel when the page waiting for help is displayed.

            if (mFallAlertObserved && mFallObject != null) {
                VoiceMessageManager.Companion.getInstance(mContext).stopSpeaking(); //stop speaking if needed
                Logger.d(TAG, "onFallStatusChanged: fall alert observed");
                mFallObject.setRescueEndDate(timestampMs);
                mFallObject.setRescueEndType("CANCEL");

                Map<String, Object> dicoFall = mFallObject.convertToFirebase();
                //Add event cancel to phase2Rescue node
                //noinspection unchecked
                Map<String, Object> phase2 = (Map<String, Object>) dicoFall.get("phase2Rescue");
                if (phase2 != null) {
                    phase2.put("events/" + timestampMs + "/CANCEL", "");
                    dicoFall.put("phase2Rescue", phase2);
                }
                writeFallObjToFb(dicoFall, mFallObject.getTriggerDate());
                removeListenerOngoingRescueAlert();
            } else if (mFallObject != null) {
                mFallObject.setEndType("CANCEL");
                if (mServiceBinder.getSerialNumber() != null) {
                    mFallObject.setEndDevice(mServiceBinder.getSerialNumber());
                }
                mFallObject.setEndDate(timestampMs);
                writeLastFallAlert();
                writeFallObjToFb();
            }
            mFallObject = null;
            //go to dashboard
            mCbNoAlert.done(true);
        } else if (status == FallState.SOS_ENGAGED.getCode()) {
            //Called when status is checked at connection, will probably be nerver happen (or if user lost/retrieve ble during countdown
            Logger.d(TAG, "onFallStatusChanged : SOS engaged from glasses : go to countdown fall page");
            if (deltaSeconds > DEFAULT_SOS_VALUE || isNotif) {
                // normally the delta is in range [0..15], but if the delta is upper (with security marge) we set the delta to 0
                deltaSeconds = DEFAULT_SOS_VALUE;
            }
            sosEngaged(timestampMs, "GLASSES", deltaSeconds);
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.SOS_ENGAGED.getCode(), timestampMs);
            }
        } else if (status == FallState.SOS_ENGAGED_FROM_MOBILE_APP.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : SOS engaged from mobile app : go to countdown fall page");
            if (deltaSeconds > DEFAULT_SOS_VALUE || isNotif) {
                // normally the delta is in range [0..15], but if the delta is upper (with security marge) we set the delta to 0
                deltaSeconds = DEFAULT_SOS_VALUE;
            }
            sosEngaged(timestampMs, "MOBILE", deltaSeconds);
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.SOS_ENGAGED_FROM_MOBILE_APP.getCode(), timestampMs);
            }
        } else if(status == FallState.SOS_CANCELLED.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : SOS cancelled : go to dashboard");
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.NO_EVENT.getCode(), timestampMs);
            }

            // event
            if (mFallAlertObserved && mFallObject != null) {
                VoiceMessageManager.Companion.getInstance(mContext).stopSpeaking(); //stop speaking if needed

                Map<String, Object> dicoFall = mFallObject.convertToFirebase();
                //Add event cancel to phase2Rescue node
                //noinspection unchecked
                Map<String, Object> phase2 = (Map<String, Object>) dicoFall.get("phase2Rescue");
                if (phase2 != null) {
                    phase2.put("events/" + timestampMs + "/CANCEL", "");
                    dicoFall.put("phase2Rescue", phase2);
                }
                writeFallObjToFb(dicoFall, mFallObject.getTriggerDate());

                mFallObject.setRescueEndDate(timestampMs);
                mFallObject.setRescueEndType("CANCEL");

                writeFallObjToFb();
                removeListenerOngoingRescueAlert();
            } else if (mFallObject != null) {
                mFallObject.setEndType("CANCEL");
                if (mServiceBinder.getSerialNumber() != null) {
                    mFallObject.setEndDevice(mServiceBinder.getSerialNumber());
                }

                mFallObject.setEndDate(timestampMs);
                writeLastFallAlert();
                writeFallObjToFb();
            }
            mFallObject = null;

            //go to dashboard
            mCbNoAlert.done(true);
        } else if(status == FallState.SOS_CONFIRMED.getCode()) {
            if(mTimeStampCancelled > 0 && mTimeStampDisconnected > 0 ){//
                Log.d(TAG, "SOS has already been cancelled by user when glasses were not connected");
                    mServiceBinder.cancelSosFallAlert();

                mTimeStampCancelled = 0;
                mTimeStampDisconnected = 0;
                mFallObject = null;
                return;
            }


            Logger.d(TAG, "onFallStatusChanged : SOS confirmed from glasses : go to waiting for help page");

            sosConfirmed(timestampMs, "GLASSES", "TIMEOUT");
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.SOS_CONFIRMED.getCode(), timestampMs);
            }
        } else if(status == FallState.SOS_CONFIRMED_FROM_MOBILE_APP.getCode()) {
            Logger.d(TAG, "onFallStatusChanged : SOS confirmed from glasses : go to waiting for help page");

            sosConfirmed(timestampMs, "MOBILE", "MANUAL_CONFIRM");
            if(mFirebaseDb != null) {
                mFirebaseDb.updateFallStatus((byte) FallState.SOS_CONFIRMED_FROM_MOBILE_APP.getCode(), timestampMs);
            }
        } else {
            Logger.d(TAG, "onFallStatusChanged status not recognized");
        }
    }


    private void fallEngaged(long triggerDate, long delay) {

        if (mFallAlertObserved) {
            // do nothing.
            Logger.d(TAG, "fallEngaged: do nothing");
        } else if (mFallObject != null) {
            Logger.d(TAG, "FallAlertManager: fallEngaged: fallObject not null");
            //go to confirm page with timing (delay)
            mCbAlertEngaged.done((int) delay);
        } else {



            VoiceMessageManager.Companion.getInstance(mContext).speechFallCountdownInitiated(mContext);

            Logger.d(TAG, "fallEngaged: fallObject null");
            mFallObject = new FallObject();
            mFallObject.setTriggerDate(triggerDate);
            mFallObject.setTriggerType("FALL");

            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

            if (mServiceBinder.getSerialNumber() != null) {
                mFallObject.setTriggerDevice(mServiceBinder.getSerialNumber());
            }

            //go to confirm page with delay (delay)
            mCbAlertEngaged.done((int) delay);
        }
    }

    private void fallConfirmed(long endDate, String device) {

        //go to waiting for help page
        mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.NO_EVENT));
        if(mFallAlertObserved){
            return;
        }

        VoiceMessageManager.Companion.getInstance(mContext).speechAlertConfirmed(mContext);

        if (mFallObject != null) {
            Logger.d(TAG, "fallConfirmed: fall alert not observed, fall NOT null");
            // at this time we are in the confirmation page
            mFallObject.setEndDate(endDate);
            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);
            if (device.equals("MOBILE")) {

                mFallObject.setEndDevice("MOBILE");
                mFallObject.setEndType("MANUAL_CONFIRM");
            } else {
                if (mServiceBinder.getSerialNumber() != null) {
                    mFallObject.setEndDevice("MOBILE");
                }
                mFallObject.setEndType("TIMEOUT");
            }

        } else {
            Logger.d(TAG, "fallConfirmed: fall alert not observed, fall IS null");
            // at this time we are in the dashboard
            mFallObject = new FallObject();
            mFallObject.setTriggerDate(endDate);
            mFallObject.setTriggerType("FALL");
            mFallObject.setTriggerDevice("MOBILE");

            mFallObject.setEndDevice("MOBILE");
            mFallObject.setEndDate(endDate);
            mFallObject.setEndType("TIMEOUT");

            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

        }
        writeLastFallAlert();
        writeFallObjToFb();
        listenOngoingRescueAlert();
    }

    private void sosEngaged(long triggerTs, String device, long delay) {

        if (mFallObject != null) {
            Logger.d(TAG, "FallAlertManager: sosEngaged: fall NOT null do nothing");
            // on confirm page
            // do nothing.
        } else {
            Logger.d(TAG, "FallAlertManager: sosEngaged: fall IS null");
            mFallObject = new FallObject();
            mFallObject.setTriggerDate(triggerTs);
            mFallObject.setTriggerType("SOS");

            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

            if (device.equals("MOBILE")) {
                mFallObject.setTriggerDevice("MOBILE");
                VoiceMessageManager.Companion.getInstance(mContext).speechSosCountdownInitiatedByMobile(mContext);
            } else {
                if (mServiceBinder.getSerialNumber() != null) {
                    mFallObject.setTriggerDevice(mServiceBinder.getSerialNumber());
                }
                VoiceMessageManager.Companion.getInstance(mContext).speechSosCountdownInitiatedByGlasses(mContext);
            }

            //go to confirm page
            mCbAlertEngaged.done((int) delay);
        }
    }

    private void sosConfirmed(long endTs, String device, String endType) {

        if (mFallAlertObserved) {
            // do nothing
            Logger.d(TAG, "sosConfirmed: fall is observed");
        } else if (mFallObject != null) {
            Logger.d(TAG, "sosConfirmed: fall NOT null");
            // here we are in confirm page
            mFallObject.setEndDate(endTs);
            mFallObject.setEndType(endType);

            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

            if (device.equals("MOBILE")) {
                mFallObject.setEndDevice("MOBILE");
            } else {
                if (mServiceBinder.getSerialNumber() != null) {
                    mFallObject.setEndDevice(mServiceBinder.getSerialNumber());
                }
            }

            writeLastFallAlert();
            writeFallObjToFb();
            listenOngoingRescueAlert();

            VoiceMessageManager.Companion.getInstance(mContext).speechAlertConfirmed(mContext);

            //go to waiting page.
            mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.NO_EVENT));
        } else {
            Logger.d(TAG, "sosConfirmed: fall IS null");
            // here on dashboard page
            mFallObject= new FallObject();
            mFallObject.setTriggerDate(endTs);
            mFallObject.setTriggerType("SOS");
            mFallObject.setTriggerDevice("MOBILE");
            mFallObject.setEndDate(endTs);
            mFallObject.setEndType(endType);

            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

            if (device.equals("MOBILE")) {
                mFallObject.setTriggerDevice("MOBILE");
            } else {
                if (mServiceBinder.getSerialNumber() != null) {
                    mFallObject.setTriggerDevice(mServiceBinder.getSerialNumber());
                }
            }

            writeLastFallAlert();
            writeFallObjToFb();
            listenOngoingRescueAlert();

            VoiceMessageManager.Companion.getInstance(mContext).speechAlertConfirmed(mContext);
            // go to waiting for help page
            mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.NO_EVENT));
        }

    }

    public void notifyLastFallStatus() {
        Logger.d(TAG, "notifyLastFallStatus()");
        if (isFallAlertInProgress() && isCountdownAlertActive()) {
            Logger.d(TAG, "notifyLastFallStatus: alert engaged");
            long delay = ((System.currentTimeMillis() - mFallObject.getTriggerDate()) / 1000);
            mCbAlertEngaged.done((int) delay);
        } else if (isFallAlertInProgress()) {
            Logger.d(TAG, "notifyLastFallStatus: alert confirmed");
            onOngoingFallAlertChanged(mFallObject);
        } else {
            Logger.d(TAG, "notifyLastFallStatus: no alert");
            if(mServiceBinder.getFallInfo() != null &&
                    mServiceBinder.getFallInfo().getValue() != null &&
                    (mServiceBinder.getFallInfo().getValue().getState() == FallState.FALL_ENGAGED ||
                            mServiceBinder.getFallInfo().getValue().getState() == FallState.FALL_CONFIRMED ||
                            mServiceBinder.getFallInfo().getValue().getState() == FallState.FALL_CONFIRMED_APP)){



                if(mServiceBinder.getFallInfo().getValue().getTimestamp() < mTimeStampDisconnected) {
                    Log.d(TAG, "SOS event has been triggered during glasses disconnected, stay on alert state");
                    //mTimeStampDisconnected = 0;
                    mFallObject = new FallObject();

                    long triggerDate = System.currentTimeMillis();
                    mFallObject.setTriggerDate(triggerDate);
                    mFallObject.setTriggerType("FALL");
                    mFallObject.setTriggerDevice("GLASSES");
                    writeLastFallAlert();
                    writeFallObjToFb();
                    listenOngoingRescueAlert();
                } else {

                    mServiceBinder.cancelFallAlert();
                }
            } else if (mServiceBinder.getFallInfo() != null &&
                    mServiceBinder.getFallInfo().getValue() != null &&
                    (mServiceBinder.getFallInfo().getValue().getState() == FallState.SOS_ENGAGED ||
                            mServiceBinder.getFallInfo().getValue().getState() == FallState.SOS_CONFIRMED ||
                            mServiceBinder.getFallInfo().getValue().getState() == FallState.SOS_CONFIRMED_FROM_MOBILE_APP ||
                            mServiceBinder.getFallInfo().getValue().getState() == FallState.SOS_ENGAGED_FROM_MOBILE_APP)){
                long tsInMilli = mServiceBinder.getFallInfo().getValue().getTimestamp() * 1000;
                if(tsInMilli< mTimeStampDisconnected && mTimeStampCancelled == 0) { //In other cases, the alert hgas been cancelled when glasses were not conected to the application
                    Log.d(TAG, "SOS event has been triggered during glasses disconnected, stay on alert state");
                    //mTimeStampDisconnected = 0;
                    mFallObject = new FallObject();


                    long triggerDate = System.currentTimeMillis();
                    mFallObject.setTriggerDate(triggerDate);
                    mFallObject.setTriggerType("SOS");
                    mFallObject.setTriggerDevice("GLASSES");
                    writeLastFallAlert();
                    writeFallObjToFb();
                    listenOngoingRescueAlert();
                } else {
                    Log.d(TAG, "alert is old ans already finished, we can reset glasses state");

                    mServiceBinder.cancelSosFallAlert();
                }
            }
            mCbNoAlert.done(true);
        }
    }

    private void listenOngoingRescueAlert() {
        Logger.d(TAG, "listenOngoingRescueAlert()");
        mFallAlertObserved = true;
        if(mFirebaseDb != null) {
            mFirebaseDb.listenOngoingRescueAlert(obj -> {
                Logger.d(TAG, "listenOngoingRescueAlert: update: " + obj.toString());
                onOngoingFallAlertChanged(obj);
            });
        }
    }

    private void removeListenerOngoingRescueAlert() {
        Logger.d(TAG, "removeListenerOngoingRescueAlert()");
        mFallAlertObserved = false;
        if(mFirebaseDb != null) {
            mFirebaseDb.removeOngoingRescueAlertListener();
        }
    }


    public void alertAlreadyOngoing(EllcieCommonCallbackGetGeneric<FallObject> cb) {
        Logger.d(TAG, "alertAlreadyOngoing()");
        if(mFirebaseDb != null) {
            mFirebaseDb.getLatestFallAlert(fallObj -> {
                if (fallObj != null) {
                    Logger.d(TAG, "alertAlreadyOngoing: latest fall alert obj exists");
                } else {
                    Logger.d(TAG, "alertAlreadyOngoing: latest fall alert obj doesn't exists");
                }
                cb.done(fallObj);
            });
        }
    }


    public void setLastGpsPosition(String latitude, String longitude) {
        Logger.d(TAG, "setLastGpsPosition: lat: " + latitude + " , long: " + longitude);
        mLastLatitudePosition = latitude;
        mLastLongitudePosition = longitude;
        mLastLocationReceivedTs = System.currentTimeMillis();

        if (isFallAlertInProgress() && isCountdownAlertActive()) {
            // alert not already pushed to FB. You can set the gps location.
            long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
            mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);
        }
    }

    public void engage() {
        Logger.d(TAG, "engage()");

        VoiceMessageManager.Companion.getInstance(mContext).speechSosCountdownInitiatedByMobile(mContext);

        // when the user click on the button SOS of the dashboard page
        // send command, get timestamp, create object fall
        mFallObject = new FallObject();
        mFallObject.setTriggerDate(System.currentTimeMillis());
        mFallObject.setTriggerType("SOS");
        mFallObject.setTriggerDevice("MOBILE");

        long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
        mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

        if(mServiceBinder.getBluetoothDevice() != null){ //Take care of no glasses connected
            mServiceBinder.engageSosFall();
        }
        if(mFirebaseDb != null) {
            mFirebaseDb.updateFallStatus((byte) FallState.SOS_ENGAGED_FROM_MOBILE_APP.getCode(), System.currentTimeMillis()); // engaged from mobile
        }

        //go to confirmation page
        mCbAlertEngaged.done((int) 15);
    }


    public void confirm(boolean timeout) {
        Logger.d(TAG, "confirm()");
        if (mFallObject == null) {
            return;
        }

        mFallObject.setEndDate(System.currentTimeMillis());

        long elapsedTimeMs = System.currentTimeMillis() - mLastLocationReceivedTs;
        mFallObject.setLatLongPosition(mLastLatitudePosition, mLastLongitudePosition, elapsedTimeMs);

        if (Objects.equals(mFallObject.getEndDevice(), "")) {
            VoiceMessageManager.Companion.getInstance(mContext).speechAlertConfirmed(mContext);
            if (timeout) {
                mFallObject.setEndType("TIMEOUT");
            } else {
                mFallObject.setEndType("MANUAL_CONFIRM");
            }

            mFallObject.setEndDevice("MOBILE");

            writeLastFallAlert();
            writeFallObjToFb();
            listenOngoingRescueAlert();

            if (Objects.equals(mFallObject.getTriggerType(), "SOS")) {
                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    mServiceBinder.confirmSosFall();
                }
                if(mFirebaseDb != null) {
                    mFirebaseDb.updateFallStatus((byte) FallState.SOS_CONFIRMED_FROM_MOBILE_APP.getCode(), System.currentTimeMillis()); // sos confirmed from mobile
                }

            } else {
                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    mServiceBinder.confirmAlert();
                }
                if(mFirebaseDb != null) {
                    mFirebaseDb.updateFallStatus((byte) FallState.FALL_CONFIRMED_APP.getCode(), System.currentTimeMillis()); // fall confirmed from mobile
                }

            }

            //go to waiting for help page
            mCbAlertConfirmed.done(new RescueEvent(RescueEvent.State.NO_EVENT));
        }
    }

    public void cancel() {
        Logger.d(TAG, "cancel()");
        mTimeStampCancelled = System.currentTimeMillis();
        if(mFirebaseDb != null) {
            mFirebaseDb.updateFallStatus((byte) FallState.NO_EVENT.getCode(), System.currentTimeMillis()); // cancelled -> no alert
        }

        if (mFallObject != null) {
            Logger.d(TAG, "cancel: fall object not null");
            VoiceMessageManager.Companion.getInstance(mContext).stopSpeaking();

            if (!mFallObject.acquittedByHelper() && !mFallObject.emergencyContacted() && !mFallObject.acquittedByVitaris()) {
                // if no acquittment -> send command.
                if(Utils.isBleEnabled() && mServiceBinder.getBluetoothDevice() != null) {
                    if (Objects.equals(mFallObject.getTriggerType(), "SOS")) {
                        Logger.d(TAG, "cancel: cancel sos");
                        mServiceBinder.cancelSosFallAlert();
                    } else {
                        Logger.d(TAG, "cancel: cancel fall");
                        mServiceBinder.cancelFallAlert();
                    }
                }
            }

            if (mFallAlertObserved) {
                Logger.d(TAG, "cancel: alert observed");
                Logger.d(TAG, "cancel: alert observed: trigger date: " + mFallObject.getTriggerDate());
                // cancel from waiting for help page
                removeListenerOngoingRescueAlert();
                mFallObject.setRescueEndType("CANCEL");
                long endDateTs = System.currentTimeMillis();

                mFallObject.setRescueEndDate(endDateTs);

                Map<String, Object> dicoFall = mFallObject.convertToFirebase();

                //Add event cancel to phase2Rescue node
                //noinspection unchecked
                Map<String, Object> phase2 = (Map<String, Object>) dicoFall.get("phase2Rescue");
                if (phase2 != null) {
                    phase2.put("events/" + endDateTs + "/CANCEL", "");
                    dicoFall.put("phase2Rescue", phase2);
                }
                writeFallObjToFb(dicoFall, mFallObject.getTriggerDate());
            } else {
                //cancel from countdown page, no alert was sent to firebase
                // no alert sent to firebase
                Logger.d(TAG, "cancel: no alert observed");
                Logger.d(TAG, "cancel: no alert observed: trigger date: " + mFallObject.getTriggerDate());
                Logger.d(TAG, "cancel: no alert observed: last location received ts: " + FallObject.Companion.getMElapsedTimeSinceLastGpsReceptionMs());
                long endDateTs = System.currentTimeMillis();
                mFallObject.setEndType("CANCEL");
                mFallObject.setEndDevice("MOBILE");
                mFallObject.setEndDate(endDateTs);
                writeFallObjToFb();

            }
        }

        mFallObject = null;
        mCbNoAlert.done(true);
    }

    public IFirebaseDb getFirebaseDbInstance() {
        return mFirebaseDb;
    }
}
