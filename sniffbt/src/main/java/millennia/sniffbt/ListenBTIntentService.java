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
        final String TAG = "SniffBT Intent Service";
        AlarmManager alarmMgr;
        PendingIntent alarmPendingIntent;
        Intent alarmIntent;
        Row[] arrPairedDevicesList = (Row[])cf.deserialize(workIntent.getByteArrayExtra("PairedDevicesList"));

        Log.i(TAG, "Inside Intent Service...");

        int interval = 2;
        try{
            //int interval = (int)cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class);

            if(interval != 0) {
                alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarmIntent = new Intent(this, AlarmReceiver.class);
                alarmIntent.putExtra("PairedDevicesList", cf.serialize(arrPairedDevicesList));
                alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                //alarmMgr.setInexactRepeating();
                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (interval * 1000), alarmPendingIntent);
                //alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (interval * 1000), alarmPendingIntent);
            }
        }
        catch (Exception ignored){}
    }

    /*
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    */
}
