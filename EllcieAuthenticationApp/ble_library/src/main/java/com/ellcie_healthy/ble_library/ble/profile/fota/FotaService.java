package com.ellcie_healthy.ble_library.ble.profile.fota;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.BleWriteCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.fota.callback.FotaEventDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.fota.callback.ImageDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.fota.data.FotaEvent;
import com.ellcie_healthy.ble_library.ble.profile.fota.data.ImageData;
import com.ellcie_healthy.common.converters.Converters;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.LogEnum;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.otaUtils.ChecksumUtils;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.otaUtils.OtaUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FotaService extends BleService<FotaCallbacks> {
    private static final String TAG = "FotaService";

    /**
     * FOTA Service UUID.
     */
    private final static UUID FOTA_SERVICE_UUID = UUID.fromString("8a97f7c0-8506-11e3-baa7-0800200c9a66");
    public final static UUID IMAGE_CHARACTERISTIC_UUID = UUID.fromString("122e8cc0-8508-11e3-baa7-0800200c9a66");
    public final static UUID NEW_IMAGE_CHARACTERISTIC_UUID = UUID.fromString("210f99f0-8508-11e3-baa7-0800200c9a66");
    public final static UUID NEW_IMAGE_TU_CONTENT_CHARACTERISTIC_UUID = UUID.fromString("2691aa80-8508-11e3-baa7-0800200c9a66");
    public final static UUID FOTA_EVENT_CHARACTERISTIC_UUID = UUID.fromString("2bdc5760-8508-11e3-baa7-0800200c9a66");




    public interface IOtaStateListener {
        void onOtaStarted(boolean success);
        void onOtaCompleted(boolean success, int reason);
        void onOtaPercentageChanged(int progress);
    }

    public ImageDataCallback mImageDataCallback = new ImageDataCallback() {
        @Override
        public void onFotaImage(@NonNull BluetoothDevice device, @NonNull ImageData image) {
            Log.d(TAG, "onFotaImage: " + image);
            //read Ota image

        }
    };
    private final FotaEventDataCallback mFotaEventDataCallback = new FotaEventDataCallback() {
        @Override
        public void onFotaEvent(@NonNull BluetoothDevice device, @NonNull FotaEvent event) {
            Log.d(TAG, "onFotaEvent: " + event);
            //Evenement d'ecriture lors de la mise a jour OTA
        }
    };

    private final BleCharacteristic<FotaEventDataCallback> mFotaEvent;


    private final BleCharacteristic<ImageDataCallback> mImage = new BleCharacteristic<>(IMAGE_CHARACTERISTIC_UUID,
            "Image",
            false);



    /**
     * The "Notification Range" defines the number of write to
     * "New Image TU Content Characteristic" required before to emit a notification
     * (using "ExpectedImageTUSeqNumber Characteristic").
     * A too small number may cause a big overhead, while a too big will increase
     * the time required to detect and handle errors.
     **/
    private static final int NOTIFICATION_RANGE = 8;
    private static final int NB_PACKETS_PAUSE = 10; //10 packets with 20ms tempo 50/200 not working was 200/200 originally
    private static final byte OTA_RESERVED_BYTE = (byte) 0x00;
    private byte[] mOtaFile;
    private static final int OTA_CHUNK_SIZE = 16;
    private static final int OTA_NEW_IMAGE_TU_CONTENT_SIZE = 20;
    private final AtomicInteger mOtaSeqNb = new AtomicInteger(1); // sequence number for ota
    private final AtomicBoolean isOtaCompleted = new AtomicBoolean(false);
    // 5s
    private static final int DELAY_BETWEEN_X_PACKETS_OTA_MS = 30; // millisecondes
    private Handler mHandlerOtaTimeout;
    private final AtomicInteger mErrorCounter = new AtomicInteger(0);
    private static final int MAX_ERROR = 5;
    private final ConcurrentHashMap<String, Object> mExpectedImageTuSeqNbMap = new ConcurrentHashMap<>();
    private final static String EXPECTED_IMAGE_TU_SEQ_NB_KEY = "ExpectedImageTuSeqNbKey";
    private final AtomicBoolean mSendingOtaPackets = new AtomicBoolean(false);
    private int mPercentageUploadCompleted = 0;

    private Handler mHandlerOtaInit;
    private final EHBleManager mManager;

    private String mFileDir;
    private IOtaStateListener mListener;

    private boolean mOldOta = false;


    private OtaState mOtaState = OtaState.OTA_STOPPED;

    private enum OtaState {
        OTA_RUNNING,
        OTA_STOPPED
    }

    public FotaService(final EHBleManager manager) {
        super(manager, FOTA_SERVICE_UUID, "FOTA");

        mManager = manager;

        mImage.addCharacteristicCallback(mImageDataCallback);
        this.addCharacteristic(mImage);

        final NewImageCharacteristic newImage = new NewImageCharacteristic();
        this.addCharacteristic(newImage);

        final NewImageTuContentCharacteristic newImageTuContent = new NewImageTuContentCharacteristic();
        this.addCharacteristic(newImageTuContent);

        mFotaEvent = new BleCharacteristic<>(FOTA_EVENT_CHARACTERISTIC_UUID,
                "Image",
                false);
        mFotaEvent.addCharacteristicCallback(mFotaEventDataCallback);
        this.addCharacteristic(mFotaEvent);
    }

    public void setListener(IOtaStateListener listener){
        mListener = listener;
    }

    public void setFileDir(String fileDir){
        mFileDir = fileDir;
    }

    public void startOta(){
        reset();
        readOtaImageCharacteristic();
    }


    /**
     * Will check if max ota errors is reached, if so abort ota (ota failure)
     */
    private boolean checkMaxError() {
        Log.d(TAG, "checkMaxError()");
        if (!mOldOta) {
            return false;
        }
        if (mErrorCounter.get() > MAX_ERROR) {
            stopOtaTimeout(); //will set handler to null, the ota process will be finished
            Log.e(String.valueOf(LogEnum.SEO014), TAG);
            mOtaState = OtaState.OTA_STOPPED;
            mPercentageUploadCompleted = 0;
            mListener.onOtaCompleted(false, -1);
            return true;
        }
        return false;
    }

    private void treatOtaExpectedImageTuSequenceNumber(byte[] values) {
        Log.d(TAG, "treatOtaExpectedImageTuSequenceNumber()");
        //If ota state is stopped, then ignore
        if (mOtaState.equals(OtaState.OTA_STOPPED)) {
            return;
        }

        // Log.d(TAG, "treatOtaExpectedImageTuSequenceNumber()");
        Log.d(TAG, "treatOtaExpectedImageTuSequenceNumber : " + Converters.getHexValue(values));

        if(!mOldOta) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }



        if (checkMaxError()) {
            return;
        }
        if (values.length != 4) {
            Log.e(TAG, "Value received from OtaExpectedImageTuSequenceNumber does not match spec");
            return;
        }

        //Retrieve sequence number
        mOtaSeqNb.set(unsignedBytesToInt(values[0], values[1]));
        Log.d(TAG, "Seq number received : " + unsignedBytesToInt(values[0], values[1]));

        //Retrieve errorCode and do actions consequently
        byte[] errorCode = new byte[]{values[2], values[3]};
        if (Arrays.equals(errorCode, OtaUtils.Companion.getOTA_SUCCESS())) {
            //Reset error counter to 0;
            mErrorCounter.set(0);

            Log.d(TAG, "OtaExpectedImageTuSequenceNumber success");
            //If success code and sequence number 0 received then Ota is succesfully started
            if (mOtaSeqNb.get() == 0) {
                stopOtaInitTimeout();
                mListener.onOtaStarted(true);

            }

            //If success code and ota completed, then notify that the ota is completed
            if (isOtaCompleted.get()) {
                //everything is completed, stop the ota timeout
                Log.d(TAG, "OTA COMPLETED");
                stopOtaTimeout();
                mOtaState = OtaState.OTA_STOPPED;
                mPercentageUploadCompleted = 0;
                mListener.onOtaCompleted(true, OtaUtils.SUCCESS);
                return;
            }

        } else {
            Log.e(TAG, "OtaExpectedImageTuSequenceNumber error : " + Converters.getHexValue(errorCode));
            //Increase error counter
            mErrorCounter.set(mErrorCounter.get() + 1);
            //If max errors is reached then stop ota
            if (checkMaxError()) {
                return;
            }
        }
        Log.d(TAG, "OTA");

        //handler null means ota is not started or finished because an timeout/error
        if (mHandlerOtaTimeout == null) {
            Log.d(TAG, "onOtaExpectedImageTuSequenceNumberReceived: not started");
            // if ota is not started.
            return;
        }

        //Reset expectedImageTuSeqNb
        setExpectedImageTuSeqNb(null);
        //Continue to write ota (seq numbed is actualized)
        mSendingOtaPackets.set(true);
        Log.d(TAG, "GOING TO WRITE OTA");
        writeOta();
    }

    public void onCharacteristicWriteNewImageTuContent() {
        //Log.d(TAG, "onCharacteristicWriteNewImageTuContent()");
        if (mOtaState.equals(OtaState.OTA_RUNNING)) {
            int otaFileOffset = mOtaSeqNb.get() * OTA_CHUNK_SIZE;
            //write ota  until last packet is reached

            //Compute percentage of upload
            //Log.d(TAG, "otFileOffset : " + otaFileOffset);
            //Log.d(TAG, "mOtaFile : " + mOtaFile.length);
            //Log.d(TAG, "Percentage of upload : " + (((float) otaFileOffset / (float) mOtaFile.length) * 100));
            int percentageUploadCompleted = (int) (((float) otaFileOffset / (float) mOtaFile.length) * 100);
            //If percentage increased then notify the ota progress dialog
            if (percentageUploadCompleted > mPercentageUploadCompleted) {
                mPercentageUploadCompleted = percentageUploadCompleted;
                mListener.onOtaPercentageChanged(percentageUploadCompleted);
            }

            if (otaFileOffset < mOtaFile.length) {
                byte[] expectedImageTuSeqNb = getExpectedImageTuSeqNb();
                if (mOldOta && mOtaSeqNb.get() % NOTIFICATION_RANGE != 0) {
                    writeOta();
                }
                //If we are in the last ota process then continue writing (no notification windows)
                else if (!mOldOta) {
                    //If we received an expectedImageTuSeqNumber notification treat it (an error)
                    if (expectedImageTuSeqNb != null) {
                        mSendingOtaPackets.set(false);
                        Log.d(TAG, "expectedImageTuSeqNb != null, treat error");
                        treatOtaExpectedImageTuSequenceNumber(expectedImageTuSeqNb);
                    } else {
                        //every x packets, do a small temporisation
                        if (mOtaSeqNb.get() % NB_PACKETS_PAUSE == 0) {
                            try {
                                Thread.sleep(DELAY_BETWEEN_X_PACKETS_OTA_MS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //Otherwise continue writing
                        writeOta();
                    }
                }
                //Notification reached and old ota
                else {
                    mSendingOtaPackets.set(false);
                    //If we received an expectedImageTuSeqNumber notification treat it
                    if (expectedImageTuSeqNb != null) {
                        Log.d(TAG, "expectedImageTuSeqNb != null, treat error");
                        treatOtaExpectedImageTuSequenceNumber(expectedImageTuSeqNb);
                    }
                }
            } else if (otaFileOffset >= mOtaFile.length) {
                //Last packet
                Log.d(TAG, "Last packet sended, otaFileOffset = " + otaFileOffset + ", FileLength = " + mOtaFile.length);
                //Last packet was written set isOtaCompleted to true
                isOtaCompleted.set(true);
                mSendingOtaPackets.set(false);
            }
        }
    }

    public void onCharacteristicWriteNewImage() {
        Log.d(TAG, "onCharacteristicWriteNewImage()");
        Log.d(TAG, "onCharacteristicWriteNewImage: write ota new image, going to enable notification for ExpectedImageTuSeqNumber characteristic ");
        if (mOtaState.equals(OtaState.OTA_RUNNING)) {
            mManager.enableNotificationForOta();
        }
    }

    public void onOtaExpectedImageTuSequenceNumberReceived(final byte[] data) {
        Log.d(TAG, "onOtaExpectedImageTuSequenceNumberReceived()");
        //If ota state is stopped, then ignore
        if (mOtaState.equals(OtaState.OTA_STOPPED)) {
            return;
        }
        Log.d(TAG, "onOtaExpectedImageTuSequenceNumberReceived : " + Converters.getHexValue(data));
        //Clear previous value
        setExpectedImageTuSeqNb(null);

        Log.d(TAG, "mSendingOtaPackets : " + mSendingOtaPackets.get());
        //If we are sending packets  then store the values of the notifications and treat it after (see onCharacteristicWrite)
        if (mSendingOtaPackets.get()) {
            Log.d(TAG, "Sending packets, store notification on Ota Expected Image");
            setExpectedImageTuSeqNb(data);
        }
        //If not sending then treat the notification directly
        else {
            treatOtaExpectedImageTuSequenceNumber(data);
        }
    }

    private void setExpectedImageTuSeqNb(byte[] byteArray) {
        Log.d(TAG, "setExpectedImageTuSeqNb()");
        if (byteArray == null) {
            mExpectedImageTuSeqNbMap.clear();
        } else {
            mExpectedImageTuSeqNbMap.put(EXPECTED_IMAGE_TU_SEQ_NB_KEY, byteArray.clone());
        }
    }

    private byte[] getExpectedImageTuSeqNb() {
        Log.d(TAG, "getExpectedImageTuSeqNb()");
        Object result = mExpectedImageTuSeqNbMap.get(EXPECTED_IMAGE_TU_SEQ_NB_KEY);
        if (result == null) {
            Log.d(TAG, "mExpectedImageTuSeqNbMap is null");
            return null;
        } else {
            Log.d(TAG, "mExpectedImageTuSeqNbMap is not null");
            return ((byte[]) result).clone();
        }
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private int unsignedBytesToInt(byte b0, byte b1) {
        Log.d(TAG, "unsignedBytesToInt()");
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private int unsignedByteToInt(byte b) {
        Log.d(TAG, "unsignedByteToInt()");
        return b & 0xFF;
    }

    /**
     * Called when an error of ota occured OR when the activity is destroyed, notify the glasses to enable bluetooth command for trip and local taps
     */
    public void notifyGlassesOtaError() {
        Log.d(TAG, "notifyGlassesOtaError()");
        reset();
    }

    /**
     * Init of an OTA, reading ota image characteristic to know in which slot we can write an ota
     */
    private void readOtaImageCharacteristic() {
        Log.d(TAG, "readOtaImageCharacteristic()");
        //Process ota is starting, reset boolean to false and error counter
        mHandlerOtaTimeout = new Handler(Looper.getMainLooper());
        mOtaState = OtaState.OTA_RUNNING; //set ota state to running
        mPercentageUploadCompleted = 0;
        isOtaCompleted.set(false);
        mErrorCounter.set(0);
        mSendingOtaPackets.set(false);
        mOtaSeqNb.set(0);
        mExpectedImageTuSeqNbMap.clear();
        mOldOta = false;

        mManager.readOtaImage();
    }




    private void writeOta() {
        Log.d(TAG, "writeOta()");
        //If ota state is stopped, then ignore
        if (mOtaState.equals(OtaState.OTA_STOPPED)) {
            Log.d(TAG, "writeOta: ota stopped.");
            return;
        }

        byte[] newImageTuContent = new byte[OTA_NEW_IMAGE_TU_CONTENT_SIZE];

        int otaFileOffset = mOtaSeqNb.get() * OTA_CHUNK_SIZE;
        int j = 1; //Used to write data, counter from 1 to 16



        if (otaFileOffset + OTA_CHUNK_SIZE >= mOtaFile.length) {
            Log.d(TAG, "writeOta: Ota, last packet, set isOtaCompleted to true");
            Log.d(TAG, "writeOta: Next sequence number received with an error code = 0 will mean the end of the OTA procedure");
            //Last packet
            for (int i = otaFileOffset; i < mOtaFile.length; i++) {
                newImageTuContent[j] = mOtaFile[i];
                j++;
            }
            for (int i = j; i < 17; i++) {
                //Write 0 in last bytes
                newImageTuContent[i] = (byte) 0x00;
            }
            //Last packet was written set isOtaCompleted to true
            isOtaCompleted.set(true);

        } else {

            for (int i = otaFileOffset; i < otaFileOffset + OTA_CHUNK_SIZE; i++) {
                newImageTuContent[j] = mOtaFile[i];
                j++;
            }
        }

        //Add reserved byte
        newImageTuContent[17] = OTA_RESERVED_BYTE;

        //Add sequence number
        final int seqNb = mOtaSeqNb.get();
        newImageTuContent[18] = (byte) (seqNb & 0xFF);
        newImageTuContent[19] = (byte) ((seqNb >> 8) & 0xFF);

        //Compute Checksum
        //newImageTuContent[0] = computeChecksum(newImageTuContent);
        newImageTuContent[0] = ChecksumUtils.computeChecksum(newImageTuContent);

        //Write the array in the characteristic
        //mOtaNewImageTuContentC.setValue(newImageTuContent);
        Log.d(TAG, "writeOta: otaOffset % 4800: " + otaFileOffset % 4800);
        boolean enableDelayBtwBursts = true;
        if (enableDelayBtwBursts && otaFileOffset % 4800 == 0) {
            Log.d(TAG, "writeOta: wait...");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                Log.d(TAG, "writeOta: run");
                mManager.writeOtaNewImageTuContent(newImageTuContent, response -> onCharacteristicWriteNewImageTuContent());

                //Increment sequence number
                //Using compareAndSet prevent to increment the sequence number if a notification ExpectedImageTuSequenceNumber is received at the same time
                mOtaSeqNb.compareAndSet(seqNb, seqNb + 1);
            }, 1500);

        } else {
            mManager.writeOtaNewImageTuContent(newImageTuContent, response -> onCharacteristicWriteNewImageTuContent());

            //Increment sequence number
            //Using compareAndSet prevent to increment the sequence number if a notification ExpectedImageTuSequenceNumber is received at the same time
            mOtaSeqNb.compareAndSet(seqNb, seqNb + 1);
        }
    }




    /**
     * Convert an int to a byte[] (little endian)
     */
    private byte[] convertIntToBytes(int value) {
        Log.d(TAG, "convertIntToBytes()");
        byte[] result = new byte[4];
        result[0] = (byte) (value & 0xFF);
        result[1] = (byte) ((value >> 8) & 0xFF);
        result[2] = (byte) ((value >> 16) & 0xFF);
        result[3] = (byte) ((value >> 24) & 0xFF);
        return result;
    }

    /**
     * This attribute is used to set the address and the size of image to flash.
     * This must fit in the address range returned by "Image Characteristic".
     * Any addresses may be used, but keep in mind that are going to be used to erase
     * the flash, and so, it may erase before the "Base Address" and after
     * "Base Address" + "Image Size".
     * <p>
     * The "Notification Range" defines the number of write to
     * "New Image TU Content Characteristic" required before to emit a notification
     * (using "ExpectedImageTUSeqNumber Characteristic").
     * A too small number may cause a big overhead, while a too big will increase
     * the time required to detect and handle errors.
     */

    private void setOtaNewImageCharacteristic(byte[] imageSize, byte[] baseAddress) throws IOException {
        Log.d(TAG, "setOtaNewImageCharacteristic()");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write((byte) 8);
        outputStream.write(imageSize);
        outputStream.write(baseAddress);

        mManager.writeOtaNewImage(outputStream.toByteArray(), response -> onCharacteristicWriteNewImage());
    }

    /**
     * This attribute is used to get a range of free memory, that could be flashed.
     * This must be used to get which partition (A or B) is going to be flashed,
     * and then select the firmware built for that partition.
     */
    public void onOtaImageCharacteristicReceived(final byte[] receivedData) {
        Log.d(TAG, "onOtaImageCharacteristicReceived()");
        Log.d(TAG, "onOtaImageCharacteristicReceived : " + Converters.getHexValue(receivedData));

        if (receivedData.length != 8) {
            Log.e(TAG, "OTA image value does not match specifications");
            return;
        }
        //Retrieve start address (big endian)
        byte[] startAddress = Arrays.copyOfRange(receivedData, 0, 4);
        Log.d(TAG, "Address (big endian) : " + Converters.getHexValue(startAddress));

        //Select the right firmware file and retrieve its size
        int size;
        if (Arrays.equals(startAddress, OtaUtils.Companion.getOTA_SLOT_0_ADDRESS())) {
            Log.d(TAG, "Adress corresponds to slot 0");
            size = readFirmwareFile(mFileDir + "/" + OtaUtils.FILE_OTA_SLOT0);
            //size = readFirmwareFile(mOtaSlot0Path);
        } else {
            Log.d(TAG, "Adress corresponds to slot 1");
            size = readFirmwareFile(mFileDir + "/" + OtaUtils.FILE_OTA_SLOT1);
            // size = readFirmwareFile(mOtaSlot1Path);
        }

        //Compare firmware size to available size to see
        byte[] endAddress = Arrays.copyOfRange(receivedData, 4, 8);
        int availableSpace = ByteBuffer.wrap(endAddress).getInt() - ByteBuffer.wrap(startAddress).getInt();
        Log.d(TAG, "Available space : " + availableSpace);
        if (size > availableSpace) {
            Log.e(TAG, "There is not enough available space to flash the firmware");
            return;
        }

        //noinspection ResultOfMethodCallIgnored
        Arrays.copyOfRange(receivedData, 0, 4);
        //Reverse the start address to send it in little endian
        ArrayUtils.reverse(startAddress);
        Log.d(TAG, "Address (little endian) : " + Converters.getHexValue(startAddress));

        //Set the address and the size of image to flash
        try {
            setOtaNewImageCharacteristic(convertIntToBytes(size), startAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read firmware file : copy the content of the file in a bye[] and retrieve the size of the file
     *
     */
    private int readFirmwareFile(String otaFilePath) {
        Log.d(TAG, "readFirmwareFile()");
        mOtaFile = null;
        File file = new File(otaFilePath);
        if (!file.exists()) {
            Log.d(TAG, "OTA File does not exist with the folowing path : " + otaFilePath);
            return 0;
        }
        Log.d(TAG, "OTA File path : " + otaFilePath);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            int size = fileInputStream.available();

            byte[] temp = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            fileInputStream.read(temp);
            fileInputStream.close();
            mOtaFile = temp;
            Log.d(TAG, "Size of OTA file : " + mOtaFile.length);
            int i;

            StringBuilder content = new StringBuilder();

            for (i = 0; i < mOtaFile.length; i++) {

                if (i % 16 == 0) {
                    content.append("\n");
                }
                content.append(Converters.getHexValue(mOtaFile[i])).append("-");
            }
            Log.d(TAG, "Content of OTA file : " + content.toString());
            return mOtaFile.length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void stopOtaInitTimeout() {
        Log.d(TAG, "stopOtaInitTimeout()");
        if (mHandlerOtaInit != null) {
            mHandlerOtaInit.removeCallbacksAndMessages(null);
            mHandlerOtaInit = null;
        }
    }

    /**
     * Timeout when the init process of an ota is launched.
     * If timeout is reached, we consider that the init did not work
     */
   /* private void startOtaInitTimeout() {
        Log.d(TAG, "startOtaInitTimeout()");
        mHandlerOtaInit = new Handler(Looper.getMainLooper());
        mHandlerOtaInit.postDelayed(mRunnableOtaInitTimeout, TIMEOUT_INIT_OTA);
    }*/

    public void stopOtaTimeout() {
        Log.d(TAG, "stopOtaTimeout()");
        if (mHandlerOtaTimeout != null) {
            mHandlerOtaTimeout.removeCallbacksAndMessages(null);
            mHandlerOtaTimeout = null;
        }
    }


    private void reset() {
        Log.d(TAG, "reset()");
        mOtaFile = new byte[1];
        mOtaSeqNb.set(1);
        isOtaCompleted.set(false);
        stopOtaTimeout(); // kill timeout for ota

        mErrorCounter.set(0);
        mExpectedImageTuSeqNbMap.clear();
        mSendingOtaPackets.set(false);
        mPercentageUploadCompleted = 0;
        stopOtaInitTimeout(); // kill timeout for ota init

        mOtaState = OtaState.OTA_STOPPED;
        mManager.setRequestConnectionPriorityBalanced();
    }


    public UUID getOtaImage(){
        return IMAGE_CHARACTERISTIC_UUID;
    }

    public UUID getFotaEvent() { return FOTA_EVENT_CHARACTERISTIC_UUID; }


    public BleCharacteristic<ImageDataCallback> getOtaImageCharacteristic(){
        return mImage;

    }

    public int getPercentageUploadCompleted(){
        return mPercentageUploadCompleted;
    }

    private static class NewImageCharacteristic extends BleWriteCharacteristic {
        public NewImageCharacteristic() {
            super(NEW_IMAGE_CHARACTERISTIC_UUID, "New Image");
        }
    }

    private static class NewImageTuContentCharacteristic extends BleWriteCharacteristic {
        public NewImageTuContentCharacteristic() {
            super(NEW_IMAGE_TU_CONTENT_CHARACTERISTIC_UUID, "New Image TU Content");
        }
    }
}
