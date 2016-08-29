package millennia.sniffbt;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

import millennia.sniffbt.pairedDevice.Row;

public class AlarmReceiver extends BroadcastReceiver {

    final String TAG = "SniffBT Alarm Receiver";
    BTActions btActions;
    Row[] arrPairedDevicesSettings;
    Context context;
    CommonFunctions cf = new CommonFunctions();

    // Initialize the variables for Bluetooth Profiles
    BluetoothProfileServiceListener slA2DP;
    BluetoothProfileServiceListener slHS;
    BluetoothA2dp a2dp;
    BluetoothHeadset hs;
    List<BluetoothDevice> connectedA2DPDevices;
    List<BluetoothDevice> connectedHSDevices;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        arrPairedDevicesSettings = (Row[])cf.deserialize(intent.getByteArrayExtra("PairedDevicesList"));

        // Turn On Bluetooth
        Log.i(TAG, "Turning on Bluetooth...");
        btActions = new BTActions();
        btActions.turnOnBluetooth();

        // Instantiate listener services (A2DP and HS)
        Log.i(TAG, "Instantiating A2DP and HS Service Listeners...");
        slA2DP = new BluetoothProfileServiceListener();
        slHS = new BluetoothProfileServiceListener();
        btActions.getBTAdapter().getProfileProxy(context, slA2DP, BluetoothProfile.A2DP);
        btActions.getBTAdapter().getProfileProxy(context, slHS, BluetoothProfile.HEADSET);
    }

    /**
     * Method to connect to Desired Paired devices.
     */
    private void connectDesiredDevices() {
        Log.i(TAG, "Connecting to Desired Paired devices...");

        if(btActions.isBluetoothTurnedOn()) {
            for(Row pairedDeviceSetting : arrPairedDevicesSettings) {
                for(BluetoothDevice pairedDevice : btActions.getPairedDevicesList()) {
                    if(pairedDeviceSetting.isCBChecked() &&
                            pairedDeviceSetting.getDeviceAddress().equals(pairedDevice.getAddress())){
                        Log.i(TAG, "Connecting device '" + pairedDevice.getName() + "'...");

                        // Connect the device
                        actionOnBTDevice("CONNECT", pairedDevice, hs);
                        actionOnBTDevice("CONNECT", pairedDevice, a2dp);
                    }
                }
            }
        }
    }

    /**
     * Method to disconnect undesired Paired devices
     */
    private void disconnectUndesiredDevices() {
        Log.i(TAG, "Disconnecting from undesired Paired devices...");

        if(btActions.isBluetoothTurnedOn()) {
            for(Row pairedDeviceSetting : arrPairedDevicesSettings) {
                for(BluetoothDevice pairedDevice : btActions.getPairedDevicesList()) {
                    if(!pairedDeviceSetting.isCBChecked() &&
                            pairedDeviceSetting.getDeviceAddress().equals(pairedDevice.getAddress())) {
                        Log.i(TAG, "Disconnecting device '" + pairedDevice.getName() + "'...");

                        //Disconnect the device
                        actionOnBTDevice("DISCONNECT", pairedDevice, hs);
                        actionOnBTDevice("DISCONNECT", pairedDevice, a2dp);
                    }
                }
            }
        }
    }

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
     * Method to verify if there are currently connected bluetooth devices
     * @return - True or False
     */
    private boolean isAnyDeviceCurrentlyConnected() {
        boolean blnIsDeviceConnected = false;

        if(connectedA2DPDevices != null) {
            if(connectedA2DPDevices.size() != 0) {
                blnIsDeviceConnected = true;
            }
        }

        if(connectedHSDevices != null) {
            if(connectedHSDevices.size() != 0) {
                blnIsDeviceConnected = true;
            }
        }

        return blnIsDeviceConnected;
    }

    private class BluetoothProfileServiceListener implements BluetoothProfile.ServiceListener {
        private final int[] states = {  BluetoothProfile.STATE_CONNECTED,
                                        BluetoothProfile.STATE_CONNECTING};
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

            if(a2dp != null && hs != null) {
                // Connect to checked devices
                connectDesiredDevices();

                // Disconnect from unchecked devices
                disconnectUndesiredDevices();

                // If bluetooth is 'already' connected or 'recently' connected, leave bluetooth on, else turn bluetooth off
                if(!isAnyDeviceCurrentlyConnected()) {
                    Log.i(TAG, "Turning off Bluetooth as no devices are currently connected...");
                    btActions.turnOffBluetooth();
                }

                // Release service listeners
                btActions.getBTAdapter().closeProfileProxy(BluetoothProfile.A2DP, a2dp);
                btActions.getBTAdapter().closeProfileProxy(BluetoothProfile.HEADSET, hs);
                slA2DP = null;
                slHS = null;
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    }

}
