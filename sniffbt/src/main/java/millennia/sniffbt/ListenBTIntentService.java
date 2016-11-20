package millennia.sniffbt;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import millennia.sniffbt.pairedDevice.Row;

public class ListenBTIntentService extends IntentService {
    final String TAG = "ListenBT IntentService";
    private CommonFunctions cf;
    private SharedPreferences appPrefs;
    private AlarmManager alarmMgr;
    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;

    public ListenBTIntentService() {
        super("ListenBTIntentService");
        cf = new CommonFunctions();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Begin ListenBt IntentService...");
        super.onCreate();
        appPrefs = getApplicationContext().getSharedPreferences(getString(R.string.app_shared_pref_filename), Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.i(TAG, "Inside Intent Service...");

        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmIntent = new Intent(this, AlarmReceiver.class);

        if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("ON")) {
            Row[] arrPairedDevicesList = (Row[]) cf.deserialize(workIntent.getByteArrayExtra("PairedDevicesList"));
            alarmIntent.putExtra("PairedDevicesList", cf.serialize(arrPairedDevicesList));
            alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            startAlarm();
        }
        else if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("OFF")) {
            alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            cancelAlarm();
        }

    }

    private void startAlarm() {
        //int interval = 1;
        try{
            int interval = (int)cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class);

            if(interval != 0) {
                //alarmMgr.setInexactRepeating();
                //Log.i(TAG, "Trigger SniffBT once...");
                //alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (interval * 1000), alarmPendingIntent);

                Log.i(TAG, "Start repeated scheduler for SniffBT...");
                alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (interval * 1000), alarmPendingIntent);
            }
        }
        catch (Exception ignored){}
    }

    private void cancelAlarm() {
        Log.i(TAG, "Stop SniffBT scheduler");
        alarmMgr.cancel(alarmPendingIntent);
    }
}
