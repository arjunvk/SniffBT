package millennia.sniffbt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import millennia.sniffbt.pairedDevice.Row;

public class BootCompleteIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CommonFunctions cf = new CommonFunctions();
        SharedPreferences appPrefs = context.getApplicationContext().getSharedPreferences(
                                     context.getApplicationContext().getResources().getString(R.string.app_shared_pref_filename),
                                     Context.MODE_PRIVATE);
        Intent intentListenBT = new Intent(context, ListenBTIntentService.class);

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            if(cf.getSharedPreferences(appPrefs,
                                       context.getApplicationContext().getResources().getString(R.string.SH_PREF_Sniff_BT_OnOff),
                                       String.class) != null) {
                if(((String) cf.getSharedPreferences(appPrefs,
                                                     context.getApplicationContext().getResources().
                                                     getString(R.string.SH_PREF_Sniff_BT_OnOff),
                                                     String.class)).equalsIgnoreCase("ON")) {
                    Row[] arrPairedDevicesList = (Row[]) cf.getSharedPreferences(appPrefs,
                                                 context.getApplicationContext().getResources().getString(
                                                 R.string.SH_PREF_Paired_Devices), Row[].class);

                    intentListenBT.putExtra("PairedDevicesList", cf.serialize(arrPairedDevicesList));
                    context.startService(intentListenBT);
                }
            }
        }
    }
}
