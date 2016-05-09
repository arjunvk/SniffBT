package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

import arjunvijayakumar.sniffbt.customRowWithCB.RowItem;

public class AlarmReceiver extends BroadcastReceiver{

    final String TAG = "SniffBT Alarm Receiver";
    BTActions btActions;
    ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    RowItem[] arrPairedDevicesList;
    CommonFunctions cf = new CommonFunctions();
    Context context;

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

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

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
        BluetoothDevice btFoundDevice = null;
        for(BluetoothDevice nearbyDevice : arrDiscoveredDevicesList ) {
            for(RowItem pairedDevice : arrPairedDevicesList) {
                if(pairedDevice.getName().equals(nearbyDevice.getName())){
                    blnPairedDeviceFound = true;
                    btFoundDevice = nearbyDevice;
                    Log.i(TAG, "Paired device '" + pairedDevice.getName() + "' found");
                    cf.displayNotification(this.context, "Device Found",
                                           pairedDevice.getName(), MainActivity.class);
                    // Turn on Bluetooth
                    btActions.turnOnBluetooth();
                    break;
                }
            }
        }

        if(!blnPairedDeviceFound) {
            cf.displayNotification(this.context, "No Device Found",
                                   "Turning off bluetooth... TATA ba bye lol", MainActivity.class);
            btActions.turnOffBluetooth();
        }
        else {
            // Connect to the 'found' device
            btActions.connectToDevice(btFoundDevice);
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

        }
    };
}
