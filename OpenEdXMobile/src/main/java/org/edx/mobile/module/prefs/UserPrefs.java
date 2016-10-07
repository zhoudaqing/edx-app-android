package org.edx.mobile.module.prefs;


import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;

import java.io.File;
import java.io.IOException;

@Singleton
public class UserPrefs {

    private Context context;
    private final Logger logger = new Logger(getClass().getName());

    @NonNull
    private final LoginPrefs loginPrefs;

    @Inject
    public UserPrefs(Context context, @NonNull LoginPrefs loginPrefs) {
        this.context = context;
        this.loginPrefs = loginPrefs;
    }

    /**
     * Returns true if the "download over wifi only" is turned ON, false otherwise.
     *
     * @return
     */
    public boolean isDownloadOverWifiOnly() {
        // check if download is only allowed over wifi
        final PrefManager wifiPrefManager = new PrefManager(context,
                PrefManager.Pref.WIFI);
        boolean onlyWifi = wifiPrefManager.getBoolean(
                PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
        return onlyWifi;
    }

    /**
     * Returns user storage directory under /Android/data/ folder for the currently logged in user.
     * This is the folder where all video downloads should be kept.
     *
     * @return
     */
    public File getDownloadFolder() {
        ProfileModel profile = getProfile();

        // Retrieves a list of external storage directories on the Device.
        // 4.4+ Android devices will return a list of external locations if the
        // SD card is available. Otherwise it will return a single location to store data.
        File[] externalDirs = ContextCompat.getExternalFilesDirs(context, "Android");
        File android;
        //Android Preferred location will be either the internal partition or an SDCard
        android = externalDirs[0];

        PrefManager prefManager = new PrefManager(context, PrefManager.Pref.SD_CARD);
        boolean downloadToSDCard = prefManager.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, false);

        if( downloadToSDCard && externalDirs.length > 1 && externalDirs[1] != null){
            // If a secondary location is in the list it is the SD Card location we
            // would like to use.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    Environment.isExternalStorageRemovable(externalDirs[1])){
                // Verify that the storage location is an external storage location
                // and only use this path if it is.
                android = externalDirs[1];
            } else {
                android = externalDirs[1];
            }
        }

        File downloadsDir = new File(android, "data");
        File edxDir = new File(downloadsDir, profile.username);
        edxDir.mkdirs();
        try {
            File noMediaFile = new File(edxDir, ".nomedia");
            noMediaFile.createNewFile();
        } catch (IOException ioException) {
            logger.error(ioException);
        }

        return edxDir;
    }

    @Nullable
    public ProfileModel getProfile() {
        return loginPrefs.getCurrentUserProfile();
    }
}
