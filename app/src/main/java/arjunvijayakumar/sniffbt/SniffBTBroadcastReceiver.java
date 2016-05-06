package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;

import arjunvijayakumar.sniffbt.customRowWithCB.RowItem;

public class SniffBTBroadcastReceiver extends BroadcastReceiver implements SniffBTInterface {
    final String TAG = "SniffBT Broadcast Receiver";
    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    private boolean blnIsDiscoveryFinished = false;
    private SniffBTInterface srcInterface = null;
    RowItem[] arrPairedDevicesList;
    SniffBTBroadcastReceiver btScan;
    BTActions btActions;
    Context context;

    public SniffBTBroadcastReceiver() {}

    public SniffBTBroadcastReceiver(SniffBTInterface srcInterface) {
        this.srcInterface = srcInterface;
    }

    public ArrayList<BluetoothDevice> getDiscoveredDevicesList() {return this.arrDiscoveredDevicesList; }

    public boolean isDiscoveryFinished() {
        return this.blnIsDiscoveryFinished;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();

        if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            arrDiscoveredDevicesList = new ArrayList<>();
        }
        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if(this.srcInterface != null &&
               this.srcInterface.getSniffBTObj() != null &&
               this.srcInterface.getSniffBTObj().getDisplayDiscoveredListFlag()) {
                this.srcInterface.displayDiscoveredList();
            }
            else {
                //context.unregisterReceiver(btScan);
                this.srcInterface.verifyIfAnyNearbyDeviceIsKnown(arrDiscoveredDevicesList);
            }
            blnIsDiscoveryFinished = true;
        }
        else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device.getName() != null) {
                arrDiscoveredDevicesList.add(device);
            }
        }
        else if(context.getString(R.string.intent_reason_sniff_bt_devices).equals(intent.getStringExtra("IntentReason"))){
            // Sniff for known BT devices
            Log.i(TAG, intent.getStringExtra("IntentReason"));

            arrPairedDevicesList = (RowItem[])intent.getSerializableExtra("PairedDevicesList");

            initiateBTScan();
        }
    }

    /**
     * Method to list the Discovered Bluetooth devices
     */
    public void initiateBTScan(){
        IntentFilter filter = new IntentFilter();
        btScan = new SniffBTBroadcastReceiver(this);
        btActions = new BTActions();
        btActions.turnOnBluetooth();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        this.context.getApplicationContext().registerReceiver(btScan, filter);

        if(btActions.isDiscovering()) {
            btActions.cancelDiscovery();
        }

        // Start discovery
        btActions.startDiscovery();
    }

    /**
     * Method to check if any of the nearby devices is part of the Paired list
     */
    @Override
    public void verifyIfAnyNearbyDeviceIsKnown(ArrayList<BluetoothDevice> arrCurrentNearbyDevices) {
        for(BluetoothDevice nearbyDevice : arrCurrentNearbyDevices ) {
            for(RowItem pairedDevice : arrPairedDevicesList) {
                if(pairedDevice.getName().equals(nearbyDevice.getName())){
                    Log.i(TAG, "Paired device '" + pairedDevice.getName() + "' found");

                    // Turn on Bluetooth
                    btActions.turnOnBluetooth();
                }
            }
        }

        endBTScan();
    }

    @Override
    public void displayDiscoveredList() {

    }

    @Override
    public SniffBT getSniffBTObj() {
        return null;
    }

    @Override
    public RowItem[] getPairedDevicesList() {
        return new RowItem[0];
    }

    @Override
    public void endBTScan() {
        this.context.getApplicationContext().unregisterReceiver(btScan);
    }
}
