package arjunvijayakumar.smellbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class SmellBTBroadcastReceiver extends BroadcastReceiver {

    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    private boolean blnIsDiscoveryFinished = false;
    private boolean blnDisplayScanDialogWhileScanningBT = false;
    private SmellBTInterface activity;

    public SmellBTBroadcastReceiver(SmellBTInterface activity, boolean blnDisplayScanDialogWhileScanningBT) {
        //super("SmellBT Broadcast Receiver");
        this.activity = activity;
        this.blnDisplayScanDialogWhileScanningBT = blnDisplayScanDialogWhileScanningBT;
    }

    public ArrayList<BluetoothDevice> getDiscoveredDevicesList() {
        return this.arrDiscoveredDevicesList;
    }

    public boolean isDiscoveryFinished() {
        return this.blnIsDiscoveryFinished;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String TAG = "Smell BT Broadcast Receiver";
        String action = intent.getAction();

        if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            arrDiscoveredDevicesList = new ArrayList<>();
        }
        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if(this.blnDisplayScanDialogWhileScanningBT) {
                blnIsDiscoveryFinished = true;
                this.activity.updateDiscoveredList();
            }
            blnIsDiscoveryFinished = true;
        }
        else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device.getName() != null) {
                arrDiscoveredDevicesList.add(device);
            }
        }
        else if(context.getString(R.string.intent_Broadcast).equals(action)){
            Log.i(TAG, "Received");
        }
    }
}
