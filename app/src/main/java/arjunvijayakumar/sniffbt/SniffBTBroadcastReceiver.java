package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class SniffBTBroadcastReceiver extends BroadcastReceiver {

    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    private boolean blnIsDiscoveryFinished = false;
    private SniffBTInterface activityInterface;

    public SniffBTBroadcastReceiver() {}

    public SniffBTBroadcastReceiver(SniffBTInterface activityInterface) {
        this.activityInterface = activityInterface;
    }

    public ArrayList<BluetoothDevice> getDiscoveredDevicesList() {return this.arrDiscoveredDevicesList; }

    public boolean isDiscoveryFinished() {
        return this.blnIsDiscoveryFinished;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String TAG = "SniffBT Broadcast Receiver";
        String action = intent.getAction();

        if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            arrDiscoveredDevicesList = new ArrayList<>();
        }
        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if(this.activityInterface.getSniffBTObj().getDisplayDiscoveredListFlag()) {
                this.activityInterface.displayDiscoveredList();
            }
            else{
                this.activityInterface.verifyIfAnyNearbyDeviceIsKnown(arrDiscoveredDevicesList);
            }
            blnIsDiscoveryFinished = true;
        }
        else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device.getName() != null) {
                arrDiscoveredDevicesList.add(device);
            }
        }
        else if(context.getString(R.string.intent_reason_alarm_receiver).equals(intent.getStringExtra("IntentReason"))){
            // Alarm has been received to initiate scan
            Log.i(TAG, intent.getStringExtra("IntentReason"));

            this.activityInterface.initiateBTScan();
        }
    }
}
