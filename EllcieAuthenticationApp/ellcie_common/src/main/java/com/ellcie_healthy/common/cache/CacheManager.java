package com.ellcie_healthy.common.cache;

import android.content.Context;

import java.io.File;

/**
 * Created by Remy on 07/02/2018.
 */

public class CacheManager {
    /**
     * Clear the entire cache.
     */
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception ignored) {}
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if(children == null){
                return  false;
            }
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
