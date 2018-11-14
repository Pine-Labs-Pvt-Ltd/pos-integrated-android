package com.pinelabs.testbillingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author  Pine Labs
 * @details This activity is used to broadcast at the tab startup
 * it uses the broadcast receiver to check whether the device boot up
 * is complete and then launches the PADControllerActivity(main activity).
 * For launch of application at the startup
 *
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
        	Log.v("PADControllerActivity", "broadCast reciever true");
            Intent i = new Intent(context, TestBillingAppActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}