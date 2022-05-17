package com.ellcie_healthy.ble_library.ble.fall;

import android.app.Activity;

import androidx.annotation.CallSuper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Remy on 20/02/2018.
 */

public abstract class Feature {

    private AtomicBoolean mFeatureEnabled;
    protected Activity mActivity;

    protected Feature() {
        mFeatureEnabled = new AtomicBoolean(false);
    }

    public final void setActivity(Activity activity) {
        mActivity = activity;
    }

    /**
     * Indicates if the feature is started successfully
     * @return true -> success, false -> fail
     */
    @CallSuper
    public final boolean start() {
        if (!mFeatureEnabled.compareAndSet(false, true)) {
            return false;
        } else {
            onStartFeature();
            return true;
        }
    }

    /**
     * Indicates if the feature is stopped successfully
     * @return true -> success, false -> fail
     */
    @CallSuper
    public final boolean stop() {
        if (mFeatureEnabled.compareAndSet(false, false)) {
            return false;
        } else {
            onStopFeature();
            mFeatureEnabled.set(false);
            return true;
        }
    }

    protected abstract void onStartFeature();
    protected abstract void onStopFeature();

    public final boolean isStarted() {
        return mFeatureEnabled.get();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessage(FeatureMessageEvent event) {

    }

    protected class FeatureMessageEvent {

    }

    @CallSuper
    public void onResume() {

    }

    @CallSuper
    public void onPause() {

    }

    @CallSuper
    public void onStop() {

    }

    @CallSuper
    public void onStart() {

    }

    @CallSuper
    public void onCreate() {

    }

    @CallSuper
    public void onDestroy(Activity activity) {
        stop();
    }
}

