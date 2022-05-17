package com.ellcie_healthy.ble_library.ble.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;


import com.ellcie_healthy.ble_library.R;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final String DEFAULT_FIRMWARE_VERSION = "0.0.0";
    public static final String MIN_VERSION_SET_ALGO_SENSITIVITY = "7.1.0";

    private static boolean mLocationRequired = isMarshmallowOrAbove();

    public static boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public static boolean isLocationPermissionsGranted(@NonNull final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationPermissionDeniedForever(@NonNull final Activity activity) {
        final SharedPreferences preferences = activity.getSharedPreferences(activity.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        return !isLocationPermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(activity.getString(R.string.saved_permission_location_requested), false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION); // This method should return false
    }

    @SuppressWarnings("deprecation")
    public static boolean isLocationEnabled(@NonNull final Context context) {
        if (isPancakeOrAbove()) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else if (isMarshmallowOrAbove()) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (final Settings.SettingNotFoundException e) {
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return true;
    }

    public static boolean isLocationRequired() {
        return mLocationRequired;
    }

    public static void markLocationNotRequired() {
        mLocationRequired = false;
    }

    public static void markLocationRequired() {
        mLocationRequired = isMarshmallowOrAbove();
    }

    public static void markLocationPermissionRequested(@NonNull final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        preferences.edit().putBoolean(context.getString(R.string.saved_permission_location_requested), true).apply();
    }

    public static void markLastSuccessfulConnectedDevice(@NonNull final Context context, @NonNull final String address) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String prevAddress = preferences.getString(context.getString(R.string.saved_device_address), null);
        if (!address.equals(prevAddress)) {
            preferences.edit().putString(context.getString(R.string.saved_device_address), address).apply();
        }
    }

    public static String getLastSuccessfulConnectedDevice(@NonNull final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.saved_device_address), null);
    }

    public static void removeLastSuccessfulConnectedDevice(@NonNull final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        preferences.edit().remove(context.getString(R.string.saved_device_address)).apply();
    }

    public static boolean isMarshmallowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isPancakeOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }


    public static int bytesToInt(@NonNull final byte[] data, @IntRange(from = 0) int offset) {
        if (offset > data.length) return 0;
        if ((offset + 4) > data.length) return 0;
        return ByteBuffer.wrap(Arrays.copyOfRange(data, offset, offset + 4)).getInt();
    }

    public static int bytesToShort(@NonNull final byte[] data, @IntRange(from = 0) int offset) {
        if (offset > data.length) return 0;
        if ((offset + 2) > data.length) return 0;

        return ByteBuffer.wrap(Arrays.copyOfRange(data, offset, offset + 2)).getShort();
    }

    public static int bytesToUnsignedShort(@NonNull final byte[] data, @IntRange(from = 0) int offset) {
        if (offset > data.length) return 0;
        if ((offset + 2) > data.length) return 0;

        return (ByteBuffer.wrap(Arrays.copyOfRange(data, offset, offset + 2)).getShort() & 0xFFFF);
    }

    public static int byteToUnsigned(final byte data) {
        return data & 0xFF;
    }

    public static Uri getSavedDirectoryStreamingPath(@NonNull final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String uri = preferences.getString(context.getString(R.string.saved_streaming_directory_path), null);

        if (uri == null) {
            return null;
        }

        return Uri.parse(uri);
    }

    public static void setSavedDirectoryStreamingPath(@NonNull final Context context, Uri uri) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (uri == null) {
            preferences.edit().remove(context.getString(R.string.saved_streaming_directory_path)).apply();
        } else {
            preferences.edit().putString(context.getString(R.string.saved_streaming_directory_path), uri.toString()).apply();
        }
    }

    public static boolean isSaveStreamingEnable(@NonNull final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getBoolean(context.getString(R.string.saved_streaming_enabled), false);
    }

    public static void setSaveStreamingEnable(@NonNull final Context context, boolean enable) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        preferences.edit().putBoolean(context.getString(R.string.saved_streaming_enabled), enable).apply();
    }

    public static boolean checkStoragePermissions(@NonNull Activity activity, Uri uri) {
        if (uri == null) return false;

        List<UriPermission> list = activity.getContentResolver().getPersistedUriPermissions();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUri().equals(uri) && list.get(i).isWritePermission()) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkStoragePermissionsAndExistence(@NonNull Activity activity, Uri uri) {
        return checkStoragePermissions(activity, uri) && DocumentFile.fromTreeUri(activity, uri).exists();
    }

    public static int compareVersion(String v1, String v2) throws InvalidParameterException {
        if (v1 == null || v2 == null) {
            throw new InvalidParameterException();
        }
        Pattern p = Pattern.compile("^\\d+\\.\\d+\\.\\d+(\\..*)?$");
        if (!p.matcher(v1).matches() || !p.matcher(v2).matches()) {
            throw new InvalidParameterException();
        }

        final String[] aV1 = v1.split("\\.");
        final String[] aV2 = v2.split("\\.");

        if (Integer.parseInt(aV1[0]) < Integer.parseInt(aV2[0])) {
            return -1;
        }
        if (Integer.parseInt(aV1[0]) > Integer.parseInt(aV2[0])) {
            return 1;
        }

        if (Integer.parseInt(aV1[1]) < Integer.parseInt(aV2[1])) {
            return -1;
        }
        if (Integer.parseInt(aV1[1]) > Integer.parseInt(aV2[1])) {
            return 1;
        }

        return Integer.compare(Integer.parseInt(aV1[2]), Integer.parseInt(aV2[2]));
    }
}
