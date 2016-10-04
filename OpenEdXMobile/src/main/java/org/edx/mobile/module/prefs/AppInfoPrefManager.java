package org.edx.mobile.module.prefs;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import org.edx.mobile.module.prefs.PrefManager.Key;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppInfoPrefManager {

    @NonNull
    private final PrefManager pref;
    private Boolean isFirstSession;

    @Inject
    public AppInfoPrefManager(Context context) {
        pref = new PrefManager(context, PrefManager.Pref.APP_INFO);
    }

    /**
     * @return True if this app session is the first after installation
     */
    public boolean isFirstSession() {
        if (isFirstSession==null) {
            isFirstSession=pref.getBoolean(Key.FIRST_SESSION, true);
        }
        return isFirstSession;
    }

    /**
     * Persistent memory will remember that at least one session after installation was run
     */
    public void setAtLeastOneSession() {
        if (isFirstSession==null) {
            isFirstSession=pref.getBoolean(Key.FIRST_SESSION, true);
        }
        pref.put(Key.FIRST_SESSION, false);
    }

    public long getAppVersionCode() {
        return pref.getLong(Key.APP_VERSION_CODE);
    }

    public void setAppVersionCode(long code) {
        pref.put(Key.APP_VERSION_CODE, code);
    }

    public String getPrevNotificationHashKey() {
        return pref.getString(Key.AppNotificationPushHash);
    }

    public void setPrevNotificationHashKey(String code) {
        pref.put(Key.AppNotificationPushHash, code);
    }

    public Set<String> getWhatsNewUrlsShown() {
        return pref.getStringSet(Key.WHATS_NEW_URLS_SHOWN);
    }

    public void setWhatsNewUrlsShown(Set<String> urlsShown) {
        pref.putStringSet(Key.WHATS_NEW_URLS_SHOWN, urlsShown);
    }

    public void appendWhatsNewUrlsShown(@NonNull Set<String> toAppend) {
        Set<String> shown = getWhatsNewUrlsShown();
        if (shown==null) {
            shown = new HashSet<>();
        }
        shown.addAll(toAppend);
        setWhatsNewUrlsShown(shown);
    }
}
