package millennia.sniffbt;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import millennia.sniffbt.customRowWithCB.RowItem;

public class AlarmReceiver extends BroadcastReceiver {

    final String TAG = "SniffBT Alarm Receiver";
    BTActions btActions;
    ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    RowItem[] arrPairedDevicesList;
    CommonFunctions cf = new CommonFunctions();
    Context context;
    BluetoothDevice btFoundPairedDevice = null;

    // Initialize the interfaces for Bluetooth Profiles
    IBluetoothHeadset ibth;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        arrPairedDevicesList = (RowItem[])intent.getSerializableExtra("PairedDevicesList");

        // Instantiate HSP binder service
        //Intent intentBTHeadset = new Intent(IBluetoothHeadset.class.getName());
        //if(!this.context.bindService(intentBTHeadset, scHeadsetProfile, Context.BIND_AUTO_CREATE)) {
        //    Log.i(TAG, "Could not bind to Bluetooth HFP Service");
        //}

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
                            connectUsingBluetoothA2dp(this.context,nearbyDevice);
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
     * Method to connect to an A2DP device
     * @param context - The {@link Context}
     * @param deviceToConnect - The {@link BluetoothDevice} to connect to
     */
    public void connectUsingBluetoothA2dp(Context context, final BluetoothDevice deviceToConnect) {
        try {
            Class<?> c2 = Class.forName("android.os.ServiceManager");
            Method m2 = c2.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m2.invoke(c2.newInstance(), "bluetooth_a2dp");
            if (b == null) {
                // For Android 4.2 Above Devices
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                        new BluetoothProfile.ServiceListener() {

                            @Override
                            public void onServiceDisconnected(int profile) {

                            }

                            @Override
                            public void onServiceConnected(int profile,
                                                           BluetoothProfile proxy) {
                                BluetoothA2dp a2dp = (BluetoothA2dp) proxy;
                                try {
                                    a2dp.getClass()
                                        .getMethod("connect", BluetoothDevice.class)
                                        .invoke(a2dp, deviceToConnect);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, BluetoothProfile.A2DP);
            }
            else {
                // For Android below 4.2 devices
                Class<?> c3 = Class.forName("android.bluetooth.IBluetoothA2dp");
                Class<?>[] s2 = c3.getDeclaredClasses();
                Class<?> c = s2[0];
                Method m = c.getDeclaredMethod("asInterface", IBinder.class);
                m.setAccessible(true);
                IBluetoothA2dp a2dp = (IBluetoothA2dp) m.invoke(null, b);
                a2dp.connect(deviceToConnect);
            }
        } catch (Exception e) {
            Log.i(TAG,e.toString());
        }
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
                verifyIfAnyNearbyDeviceIsKnown();
                if(btFoundPairedDevice != null) {
                    Log.i(TAG, "Bluetooth device '" + btFoundPairedDevice.getName() + "' connected successfully");
                }
                context.getApplicationContext().unregisterReceiver(btBroadcastReceiver);
            }
            else if("HEADSET_INTERFACE_CONNECTED".equals(action)) {
                if(ibth != null) {
                    try {
                        ibth.connect(device);
                    } catch (RemoteException e) {
                        Log.i(TAG, e.toString());
                    }
                }
            }
        }
    };
}
