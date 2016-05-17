package millennia.sniffbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import java.util.ArrayList;

import millennia.sniffbt.customRowWithCB.RowItem;

public class AlarmReceiver extends BroadcastReceiver {

    final String TAG = "SniffBT Alarm Receiver";
    BTActions btActions;
    ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    RowItem[] arrPairedDevicesList;
    CommonFunctions cf = new CommonFunctions();
    Context context;
    BluetoothDevice btFoundPairedDevice = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        arrPairedDevicesList = (RowItem[])intent.getSerializableExtra("PairedDevicesList");

        searchBTDevices();
    }

    /**
     * Method to search for nearby Bluetooth devices
     */
    public void searchBTDevices(){
        IntentFilter filter = new IntentFilter();
        btActions = new BTActions();
        btActions.turnOnBluetooth();

        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

        this.context.getApplicationContext().registerReceiver(btBroadcastReceiver, filter);

        if(btActions.isDiscovering()) {
            btActions.cancelDiscovery();
        }

        // Start discovery
        btActions.startDiscovery();
    }

    /**
     * Method to check if any of the nearby devices is part of the Paired list
     */
    public void verifyIfAnyNearbyDeviceIsKnown() {
        boolean blnPairedDeviceFound = false;
        if(arrDiscoveredDevicesList != null) {
            for(BluetoothDevice nearbyDevice : arrDiscoveredDevicesList ) {
                for(RowItem pairedDevice : arrPairedDevicesList) {
                    if(pairedDevice.getName().equals(nearbyDevice.getName())){
                        if(pairedDevice.isCBChecked()){
                            blnPairedDeviceFound = true;
                            btFoundPairedDevice = nearbyDevice;
                            Log.i(TAG, "Paired device '" + pairedDevice.getName() + "' found");
                            cf.displayNotification(this.context, "Device Found",
                                    pairedDevice.getName(), MainActivity.class);
                            // Turn on Bluetooth
                            btActions.turnOnBluetooth();
                            break;
                        }
                    }
                }
            }

            if(!blnPairedDeviceFound) {
                cf.displayNotification(this.context, "No Device Found",
                        "Turning off bluetooth... TATA ba bye lol", MainActivity.class);
                btActions.turnOffBluetooth();
            }
        }

        this.context.getApplicationContext().unregisterReceiver(btBroadcastReceiver);
    }

    private final BroadcastReceiver btBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                arrDiscoveredDevicesList = new ArrayList<>();
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                if(device.getName() != null) {
                    arrDiscoveredDevicesList.add(device);
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                verifyIfAnyNearbyDeviceIsKnown();
            }
            else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                if(btFoundPairedDevice != null) {
                    Log.i(TAG, "Bluetooth device '" + btFoundPairedDevice.getName() + "' connected successfully");
                }
                context.getApplicationContext().unregisterReceiver(btBroadcastReceiver);
            }
        }
    };
}
