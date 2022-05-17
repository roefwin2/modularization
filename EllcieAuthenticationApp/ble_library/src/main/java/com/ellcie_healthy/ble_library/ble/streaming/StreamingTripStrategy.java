package com.ellcie_healthy.ble_library.ble.streaming;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ellcie_healthy.ble_library.ble.service.EHBleForegroundService;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.streaming.StreamingFileStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class StreamingTripStrategy implements StreamingFileStrategy {

    private static final String STREAMING_TRIP_PATH = "SensorsData";
    private static final String TAG = "StreamingTripStrategy";
    private static final int TIME_PER_FILE = 10 * 60 * 1000; // 10 minutes

    private final Context mContext;
    private final String mTripId;
    private final String mUserId;
    private final String mSerialNumber;

    private final Handler mStreamingHandler = new Handler(Looper.getMainLooper());
    private final EHBleForegroundService.EHBinder mService;
    private final EllcieCommonCallbackGetGeneric<File> mCbPeriodic;

    private int mFilesCounter;
    private FileOutputStream mStreamingFos;
    private File mStreamingFile;

    private final Runnable streamingTripFileTimeout = new Runnable() {
        @Override
        public void run() {
            //Every TIME_PER_FILE close the stream to flush the file and send to it to firebase
            //Then create a new file and open the stream again

            Logger.d(TAG, "streamingTripFileTimeout");
            closeStreamingFile();
            createStreamingTripFile();

            mStreamingHandler.postDelayed(this, TIME_PER_FILE);
        }
    };

    public StreamingTripStrategy(@NonNull Context context,
                                 @NonNull String userId,
                                 @NonNull String serialNumber,
                                 @NonNull String tripId,
                                 @NonNull EHBleForegroundService.EHBinder service,
                                 @NonNull EllcieCommonCallbackGetGeneric<File> cbPeriodic) throws InvalidParameterException {
        mContext = context;
        mUserId = userId;
        mSerialNumber = serialNumber;
        mTripId = tripId;
        mFilesCounter = 0;
        mService = service;
        mCbPeriodic = cbPeriodic;

        if (serialNumber.isEmpty() || mUserId.isEmpty() || mTripId.isEmpty()) {
            throw new InvalidParameterException("a parameter is empty");
        }

        Logger.d(TAG, "StreamingTripStrategy()");
    }

    @Override
    public boolean isStreamingOnGoing() {
        return mStreamingFos != null;
    }

    private synchronized void closeStreamingFile() {
        //Close streaming file
        Logger.d(TAG, "closeSensorDataFos()");
        if (mStreamingFos != null) {
            Logger.d(TAG, "closeSensorDataFos != null");
            try {
                //Closing the stream will flush the file
                mStreamingFos.close();
                mStreamingFos = null;
            } catch (IOException e) {
                e.printStackTrace();
                mStreamingFos = null;
            }
        }

        if (mStreamingFile != null && mCbPeriodic != null) {
            final File f = mStreamingFile;
            //Call the callback to manage the upload to Firebase from the application part
            if (f.exists()) {
                mStreamingHandler.post(() -> mCbPeriodic.done(f));
            }
        }

        mStreamingFile = null;
    }

    @Override
    public void onStreamingStopped() {
        Logger.d(TAG, "onStreamingStopped()");
        //Stop timeout
        mStreamingHandler.removeCallbacks(streamingTripFileTimeout);

        //Close streaming file and upload to firebase
        closeStreamingFile();
        //mService.getMeasureService().setEllcieCallback(null);//Reset the EllcieCommonCallback to get datas from MeasureService
    }

    @Override
    public synchronized void initStreamingFile() {
        //create trip file with the related fos
        Logger.d(TAG, "initStreamingFile()");
        createStreamingTripFile();
        //start timeout
        mStreamingHandler.postDelayed(streamingTripFileTimeout, TIME_PER_FILE);
        //mService.getMeasureService().setEllcieCallback(done -> writeStreamingData(done.getValue()));
    }

    @SuppressLint("DefaultLocale")
    private synchronized void createStreamingTripFile() throws IndexOutOfBoundsException {
        Logger.d(TAG, "createStreamingTripFile");

        boolean exists = true;
        while (exists) {
            ++mFilesCounter;
            Logger.d(TAG, "generateStreamingTripName: " + mTripId + " - " + mUserId + " - " + mSerialNumber + " - " + mFilesCounter);
            String filename = mUserId + "_" + mSerialNumber + "_" + mTripId + "-" + String.format("%03d", mFilesCounter);
            mStreamingFile = StreamingUtils.createFileInternal(mContext, filename, STREAMING_TRIP_PATH);

            exists = mStreamingFile.exists();

            // for security
            if (mFilesCounter > 999) {
                mStreamingFile = null;
                throw new IndexOutOfBoundsException("file counter too huge!");
            }
        }
        mStreamingFos = StreamingUtils.createStreamingFos(mStreamingFile);
    }

    @Nullable
    public static ArrayList<File> hasAlreadyExistingFiles(@NonNull Context context, @Nullable String currentTripId) {
        Logger.d(TAG, "checkDataFilesPresence()");
        final File directory = StreamingUtils.getFileDirectoryInternal(context, STREAMING_TRIP_PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        final File[] files = directory.listFiles();
        Logger.d(TAG, "Files present in Directory path: " + directory.getAbsolutePath());

        if (files == null) {
            Logger.d(TAG, "no files");
            return null;
        }

        final ArrayList<File> existingFiles = new ArrayList<>();

        for (File file : files) {
            Logger.d(TAG, "FileName :" + file.getName());
            String [] parts = file.getName().split("_");
            if (currentTripId != null && !currentTripId.isEmpty() && parts.length > 0 && parts[parts.length - 1].startsWith(currentTripId + "-")) {
                // current trip in progress, do nothing
                Logger.d(TAG, "File " + file.getName() + " is related to current trip, do nothing");
                continue;
            }
            existingFiles.add(file);
        }

        return existingFiles;
    }

    @Override
    public void writeStreamingData(@Nullable byte[] data) {
        if (mStreamingFos != null && data != null) {
            try {
                mStreamingFos.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Logger.e(TAG, "File invalid data or streaming");
        }
    }
}
