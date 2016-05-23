package millennia.sniffbt;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import millennia.sniffbt.pairedDevice.Row;

public class ListenBTIntentService extends IntentService {

    public ListenBTIntentService() {
        super("ListenBTIntentService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        final String TAG = "SniffBT Intent Service";
        AlarmManager alarmMgr;
        PendingIntent alarmPendingIntent;
        Intent alarmIntent;
        Row[] arrPairedDevicesList = (Row[])workIntent.getSerializableExtra("PairedDevicesList");

        Log.i(TAG, "Inside Intent Service...");

        int interval = 1000 * 2;

        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmIntent = new Intent(this, AlarmReceiver.class);
        //alarmIntent.putExtra("IntentReason", getString(R.string.intent_reason_sniff_bt_devices));
        alarmIntent.putExtra("PairedDevicesList", arrPairedDevicesList);
        alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //alarmMgr.setInexactRepeating();
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, alarmPendingIntent);
        //alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, alarmPendingIntent);
    }
}
