package org.edx.mobile.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.newrelic.agent.android.NewRelic;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.module.prefs.AppInfoPrefManager;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import roboguice.RoboGuice;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity {
    @Inject
    protected IEdxEnvironment environment;

    @Inject
    protected AppInfoPrefManager appInfoPrefManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appInfoPrefManager=RoboGuice.getInjector(this).getInstance(AppInfoPrefManager.class);
        NewRelic.setInteractionName("Display " + getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleWhatsNew();
    }

    private void handleWhatsNew() {
        Router router = environment.getRouter();
        if (!router.getWhatsNewHandled()) {
            LinkedHashSet<String> unshown = getUnshownWhatsNew();

            if (!unshown.isEmpty() && !appInfoPrefManager.isFirstSession()) {
                CharSequence title = ResourceUtil.getFormattedString(getResources(),
                        R.string.whats_new_title, "version",
                        BuildConfig.VERSION_NAME);
                router.showWhatsNew(this, unshown.iterator().next(), title.toString());
            }

            appInfoPrefManager.appendWhatsNewUrlsShown(unshown);
            router.setWhatsNewHandled(true);
        }
    }

    @NonNull
    private LinkedHashSet<String> getUnshownWhatsNew() {
        LinkedHashSet<String> unshown = new LinkedHashSet<>();

        String whatsNewAssetsFolder = getResources().getString(R.string.whats_new_assets_folder);

        String[] files = null;
        try {
            files = getAssets().list(whatsNewAssetsFolder);
        }
        catch (IOException e) {}

        if (files!=null) {
            Set<String> alreadyShown = appInfoPrefManager.getWhatsNewUrlsShown();

            String baseAssetsURL = getResources().getString(R.string.base_assets_url);
            for (String file : files) {

                String fullFileName = baseAssetsURL + File.separator + whatsNewAssetsFolder
                        + File.separator + file;

                if (alreadyShown==null || !alreadyShown.contains(fullFileName)) {
                    unshown.add(fullFileName);
                }
            }
        }

        return unshown;
    }
}
