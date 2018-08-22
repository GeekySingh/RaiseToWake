package com.gagan.raise.to.wake.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gagan.raise.to.wake.persistant.AppPref;
import com.gagan.raise.to.wake.service.RaiseToWakeService;

/**
 * Start Raise to wake service when device reboots.
 * Created by gaganpreetsingh on 2/28/2017.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(AppPref.getInstance(context).isAutoStart()) {
            Toast.makeText(context, "Raise to Wake has started!", Toast.LENGTH_SHORT).show();
            context.startService(new Intent(context, RaiseToWakeService.class));
        }
    }
}
