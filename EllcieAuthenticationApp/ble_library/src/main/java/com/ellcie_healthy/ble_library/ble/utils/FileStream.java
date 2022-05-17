package com.ellcie_healthy.ble_library.ble.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidParameterException;

public class FileStream {
    private static final String TAG = FileStream.class.getSimpleName();
    private static final String MIME_TYPE = "text/comma-separated-values";

    private final Uri mFileUri;
    private final Context mContext;
    private final String mHeader;
    private BufferedWriter mBufferedWriter;

    public FileStream(@NonNull Context context,
                      @NonNull Uri root,
                      @NonNull String serial,
                      @IntRange(from = 1) int tripId,
                      @NonNull String name,
                      @NonNull String header) throws InvalidParameterException, FileNotFoundException {
        mContext = context;
        mHeader = header;

        DocumentFile rootf = DocumentFile.fromTreeUri(mContext, root);
        if (rootf == null || !rootf.isDirectory()) {
            throw new FileNotFoundException("unable to find " + root);
        }

        DocumentFile child = createDirectory(rootf, serial);
        child = createDirectory(child, Integer.toString(tripId));
        child = createFile(child, MIME_TYPE, name);
        mFileUri = child.getUri();
    }

    public synchronized boolean open() {
        close();

        try {
            Log.d(TAG, "use file: " + mFileUri);

//            Log.d(TAG, "get output stream");
            mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mContext.getContentResolver().openOutputStream(mFileUri, "wa")));

//            Log.d(TAG, "get input stream");
            BufferedReader reader = new BufferedReader(new InputStreamReader(mContext.getContentResolver().openInputStream(mFileUri)));

            try {
//                Log.d(TAG, "readLine");
                if (reader.readLine() == null) {
//                    Log.d(TAG, "writeLine");
                    writeLine(mHeader);
                    mBufferedWriter.flush();
                }

//                Log.d(TAG, "close reader");
                reader.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to readline " + e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to open " + mFileUri);
            return false;
        }

        return true;
    }

    public synchronized void close() {
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.flush();
                mBufferedWriter.close();
            } catch (IOException e) {
            }
            mBufferedWriter = null;
        }
    }

    public synchronized void flush() {
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.flush();
            } catch (IOException e) {
            }
        }
    }


    private void writeLine(String data) {
        if (mBufferedWriter == null) return;

        try {
            mBufferedWriter.write(data);
            mBufferedWriter.newLine();
        } catch (IOException e) {
        }
    }

    public synchronized void write(@NonNull SensorData<?> data) {
        writeLine(data.toString());
    }

    private DocumentFile createDirectory(DocumentFile rootf, String childName) throws FileNotFoundException {
        DocumentFile child = rootf.findFile(childName);
        if (child == null || !child.exists()) {
            child = rootf.createDirectory(childName);
        }
        if (child == null || !child.isDirectory()) {
            throw new FileNotFoundException(childName + " doesn't exist or is not a folder");
        }

        return child;
    }

    private DocumentFile createFile(DocumentFile rootf, String type, String childName) throws FileNotFoundException {
        String extension =  MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        String fullname = childName + "." + extension;
        Log.d(TAG, "looking for: " + fullname);

        DocumentFile child = rootf.findFile(fullname);
        if (child == null || !child.exists()) {
            Log.d(TAG, "child not existing: " + childName);
            child = rootf.createFile(type, childName);
        }

        if (child == null || !child.isFile()) {
            throw new FileNotFoundException(childName + " doesn't exist or is not a file");
        }

        Log.d(TAG, "child is: " + child.getUri());

        return child;
    }
}
