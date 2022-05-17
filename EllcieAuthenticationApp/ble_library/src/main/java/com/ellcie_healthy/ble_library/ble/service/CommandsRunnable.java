package com.ellcie_healthy.ble_library.ble.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleWriteCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.command.CommandService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommandsRunnable implements Runnable {
    private static final String TAG = CommandsRunnable.class.getSimpleName();

    private final EHBleManager mBleManager;
    private final LinkedBlockingQueue<GlassCommand> mCommands = new LinkedBlockingQueue<>();
    private boolean mRunning = true;

    public CommandsRunnable(final EHBleManager manager) {
        mBleManager = manager;
    }

    public void stop() {
        mRunning = false;
    }

    public void clear() {
        this.mCommands.clear();
    }

    public boolean addCommand(@NonNull GlassCommand cmd) {
        return this.mCommands.offer(cmd);
    }

    @Override
    public void run() {
        Log.d(TAG, "Start CommandsRunnable");
        final CommandService cs = mBleManager.getCommandService();
        final BleWriteCharacteristic characteristic = cs.getCommandChar();

        while (mRunning) {
            GlassCommand cmd = null;
            try {
                cmd = mCommands.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }

            if (cmd == null) {
                continue;
            }

            try {
                GlassCommand.GlassCommandCallback cmdCallback = cmd.getCmdCallback();
                GlassCommand.PreGlassCommandCallback preCmdCallback = cmd.getPreCmdCallback();
                if (preCmdCallback != null) {
                    preCmdCallback.beforeCommandWrite();
                }
                mBleManager.writeCharacteristicWithResponse(characteristic, cmd.getCmd(), cmd.getTimeout());
                if (cmdCallback != null) {
                    cmdCallback.onCommandResponse(cmd.getCmd());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        Log.d(TAG, "End CommandsRunnable");
    }
}