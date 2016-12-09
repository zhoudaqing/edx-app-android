package org.edx.mobile.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.inject.Inject;
import com.google.repacked.antlr.v4.runtime.misc.NotNull;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.NetworkCheckDialogFragment;

import java.io.File;

public class SettingsFragment extends BaseFragment {

    public static final String TAG = SettingsFragment.class.getCanonicalName();

    private final Logger logger = new Logger(SettingsFragment.class);

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    ExtensionRegistry extensionRegistry;

    private Switch wifiSwitch;
    private Switch mSDCardSwitch;
    private LinearLayout mSDCardSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getSegment().trackScreenView(ISegment.Screens.SETTINGS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View layout = inflater.inflate(R.layout.fragment_settings, container, false);
        wifiSwitch = (Switch) layout.findViewById(R.id.wifi_setting);
        mSDCardSwitch = (Switch) layout.findViewById(R.id.download_location_switch);
        mSDCardSetting = (LinearLayout) layout.findViewById(R.id.sd_card_setting);

        updateWifiSwitch();
        updateSDCardSwitch();
        final LinearLayout settingsLayout = (LinearLayout) layout.findViewById(R.id.settings_layout);
        for (SettingsExtension extension : extensionRegistry.forType(SettingsExtension.class)) {
            extension.onCreateSettingsView(settingsLayout);
        }
        return layout;
    }

    private void updateWifiSwitch() {
        final PrefManager wifiPrefManager = new PrefManager(
                getActivity().getBaseContext(), PrefManager.Pref.WIFI);

        wifiSwitch.setOnCheckedChangeListener(null);
        wifiSwitch.setChecked(wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true));
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);
                } else {
                    showWifiDialog();
                }
            }
        });
    }

    private boolean isRemovableStorageAvailable(@NotNull Context context){
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            File[] fileList = context.getExternalFilesDirs("Android");
            for (File extFile : fileList){
                if (extFile != null && Environment.isExternalStorageRemovable(extFile)){
                    return true;
                }
            }
        }
        return false;
    }

    private void updateSDCardSwitch(){
        if (this.isRemovableStorageAvailable(getContext())) {
            final PrefManager prefManager =
                    new PrefManager(getActivity().getBaseContext(), PrefManager.Pref.SD_CARD);
            mSDCardSwitch.setOnCheckedChangeListener(null);
            mSDCardSwitch.setChecked(prefManager.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, true));
            mSDCardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    prefManager.put(PrefManager.Key.DOWNLOAD_TO_SDCARD, isChecked);
                }
            });
        } else {
            mSDCardSetting.setVisibility(View.INVISIBLE);
        }

    }

    protected void showWifiDialog() {
        final NetworkCheckDialogFragment newFragment = NetworkCheckDialogFragment.newInstance(getString(R.string.wifi_dialog_title_help),
                getString(R.string.wifi_dialog_message_help),
                new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        try {
                            PrefManager wifiPrefManager = new PrefManager
                                    (getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
                            updateWifiSwitch();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }

                    @Override
                    public void onNegativeClicked() {
                        try {
                            PrefManager wifiPrefManager = new PrefManager(
                                    getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);

                            updateWifiSwitch();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                });

        newFragment.setCancelable(false);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }
}
