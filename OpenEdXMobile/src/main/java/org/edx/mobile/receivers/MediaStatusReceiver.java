package org.edx.mobile.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Class containing the Broadcast receiver to detect the status of Removable media
 */
public class MediaStatusReceiver extends BroadcastReceiver{

    public MediaStatusReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TEST RECEIVER", "Media UPDATE!!!");
    }
}
