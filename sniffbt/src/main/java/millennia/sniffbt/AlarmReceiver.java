package millennia.sniffbt;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothA2dp;
import android.bluetooth.IBluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import millennia.sniffbt.customRowWithCB.RowItem;

public class AlarmReceiver extends BroadcastReceiver {

    final String TAG = "SniffBT Alarm Receiver";
    BTActions btActions;
    ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    RowItem[] arrPairedDevicesList;
    CommonFunctions cf = new CommonFunctions();
    Context context;

    // Initialize the variables for Bluetooth Profiles
    IBluetoothHeadset ibth;

    BluetoothProfileServiceListener slA2DP;
    BluetoothProfileServiceListener slHS;
    BluetoothA2dp a2dp;
    BluetoothHeadset hs;
    List<BluetoothDevice> connectedA2DPDevices;
    List<BluetoothDevice> connectedHSDevices;

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

        // Instantiate A2DP listener service
        BluetoothAdapter ba = btActions.getBTAdapter();
        slA2DP = new BluetoothProfileServiceListener();
        slHS = new BluetoothProfileServiceListener();
        ba.getProfileProxy(context, slA2DP, BluetoothProfile.A2DP);
        ba.getProfileProxy(context, slHS, BluetoothProfile.HEADSET);

        // Register Bluetooth Broadcast Receiver
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

        context.getApplicationContext().registerReceiver(btBroadcastReceiver, filter);

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
        Log.i(TAG, "Verify if any nearby non-connected device is known");
        boolean blnPairedDeviceToBeConnected = false;

        if(arrDiscoveredDevicesList != null) {
            for(BluetoothDevice nearbyDevice : arrDiscoveredDevicesList ) {
                for(RowItem pairedDevice : arrPairedDevicesList) {
                    if(pairedDevice.getName().equals(nearbyDevice.getName()) && pairedDevice.isCBChecked()){
                        blnPairedDeviceToBeConnected = true;
                        Log.i(TAG, "Paired device '" + pairedDevice.getName() + "' found");
                        cf.displayNotification(context, "Device Found",
                                               pairedDevice.getName(), MainActivity.class);
                        // Connect the device
                        actionOnBTDevice("CONNECT", nearbyDevice, hs);
                        actionOnBTDevice("CONNECT", nearbyDevice, a2dp);
                    }
                }
            }

            if(!blnPairedDeviceToBeConnected && !isAnyDeviceCurrentlyConnected()) {
                cf.displayNotification(context, "No Device Found",
                        "Turning off bluetooth... TATA ba bye lol", MainActivity.class);
                //cf.displayNotification(context, "Adhaan edhuvum illa nu theriyudhu la",
                //                                "Poi kozhandaya padika vei...", MainActivity.class);
                btActions.turnOffBluetooth();
            }
        }

        // Check if already connected devices need to remain the same way
        checkIfDeviceConnectionIsRequired(connectedA2DPDevices);
        checkIfDeviceConnectionIsRequired(connectedHSDevices);

        // Unregister broadcast receiver and service listeners
        context.getApplicationContext().unregisterReceiver(btBroadcastReceiver);
        slA2DP = null;
        slHS = null;
    }

    /**
     * Method to bind service for HeadsetProfile
     */
    public ServiceConnection scHeadsetProfile = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ibth = IBluetoothHeadset.Stub.asInterface(service);
            Intent intent = new Intent();
            intent.setAction("HEADSET_INTERFACE_CONNECTED");
            context.sendBroadcast(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ibth = null;
        }
    };

    /**
     * Method to connect a Bluetooth device based on it profile
     * @param strAction - CONNECT or DISCONNECT
     * @param device - The {@link BluetoothDevice} to connect to
     * @param profile - The {@link BluetoothProfile} to perform the action
     */
    public void actionOnBTDevice(String strAction, final BluetoothDevice device, BluetoothProfile profile) {
        try {
            Class<?> c2 = Class.forName("android.os.ServiceManager");
            Method m2 = c2.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m2.invoke(c2.newInstance(), "bluetooth_a2dp");
            if (b == null) {
                // For Android 4.2 Above Devices
                try {
                    if(strAction.equalsIgnoreCase("CONNECT")) {
                        //a2dp.getClass().getMethod("connect", BluetoothDevice.class).invoke(a2dp, deviceToConnect);
                        profile.getClass().getMethod("connect", BluetoothDevice.class).invoke(profile, device);
                    }
                    else if(strAction.equalsIgnoreCase("DISCONNECT")) {
                        //a2dp.getClass().getMethod("disconnect", BluetoothDevice.class).invoke(a2dp,deviceToConnect);
                        profile.getClass().getMethod("disconnect", BluetoothDevice.class).invoke(profile, device);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                // For Android below 4.2 devices
                // RUNS ONLY FOR A2DP
                Class<?> c3 = Class.forName("android.bluetooth.IBluetoothA2dp");
                Class<?>[] s2 = c3.getDeclaredClasses();
                Class<?> c = s2[0];
                Method m = c.getDeclaredMethod("asInterface", IBinder.class);
                m.setAccessible(true);
                IBluetoothA2dp a2dp = (IBluetoothA2dp) m.invoke(null, b);
                if(strAction.equalsIgnoreCase("CONNECT")) {
                    a2dp.connect(device);
                }
                else if(strAction.equalsIgnoreCase("DISCONNECT")) {
                    a2dp.disconnect(device);
                }
            }
        }
        catch (Exception e) {
            Log.i(TAG,e.toString());
        }
    }

    /**
     * Method to check if a bluetooth connection is 'desired'. This device is compared with the checked Paired devices
     * @param device - The {@link BluetoothDevice} device
     */
    private void checkIfDeviceConnectionIsRequired(BluetoothDevice device) {
        for(RowItem pairedDevice : arrPairedDevicesList) {
            if(pairedDevice.getName().equals(device.getName()) && !pairedDevice.isCBChecked()){
                //Disconnect the device
                actionOnBTDevice("DISCONNECT", device, hs);
                actionOnBTDevice("DISCONNECT", device, a2dp);
            }
        }
    }

    /**
     * Method to check if a bluetooth connection is 'desired'. This is for devices that are already connected
     * @param devices the {@link List} of connected Bluetooth devices
     */
    private void checkIfDeviceConnectionIsRequired(List<BluetoothDevice> devices) {
        for(RowItem pairedDevice : arrPairedDevicesList) {
            for(BluetoothDevice device : devices) {
                if(pairedDevice.getName().equals(device.getName()) && !pairedDevice.isCBChecked()){
                    //Disconnect the device
                    actionOnBTDevice("DISCONNECT", device, hs);
                    actionOnBTDevice("DISCONNECT", device, a2dp);
                }
            }
        }
    }

    /**
     * Method to verify if there are currently connected bluetooth devices
     * @return - True or False
     */
    private boolean isAnyDeviceCurrentlyConnected() {
        return connectedA2DPDevices.size() != 0 || connectedHSDevices.size() != 0;
        //return connectedA2DPDevices.size() != 0;
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
                    Log.i(TAG, "Bluetooth device '" + device.getName() + "' found");
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                verifyIfAnyNearbyDeviceIsKnown();
            }
            else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                checkIfDeviceConnectionIsRequired(device);
                //if(btFoundPairedDevice != null) {
                    Log.i(TAG, "Bluetooth device '" + device.getName() + "' connected successfully");
                //}
                //context.getApplicationContext().unregisterReceiver(btBroadcastReceiver);
            }
            else if("HEADSET_INTERFACE_CONNECTED".equals(action)) {
                if(ibth != null) {
                    try {
                        ibth.connect(device);
                    }
                    catch (RemoteException e) {
                        Log.i(TAG, e.toString());
                    }
                }
            }
        }
    };

    private class BluetoothProfileServiceListener implements BluetoothProfile.ServiceListener {
        private final int[] states = { BluetoothProfile.STATE_CONNECTED};
                                       //BluetoothProfile.STATE_CONNECTING,
                                       //BluetoothProfile.STATE_DISCONNECTED,
                                       //BluetoothProfile.STATE_DISCONNECTING};

        @Override
        public void onServiceConnected(int profile, BluetoothProfile bluetoothProfile) {
            if(bluetoothProfile instanceof BluetoothA2dp) {
                a2dp = (BluetoothA2dp) bluetoothProfile;
                connectedA2DPDevices = bluetoothProfile.getDevicesMatchingConnectionStates(states);
            }
            else if(bluetoothProfile instanceof BluetoothHeadset) {
                hs = (BluetoothHeadset) bluetoothProfile;
                connectedHSDevices = bluetoothProfile.getDevicesMatchingConnectionStates(states);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    }

}
