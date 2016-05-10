package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Set;
import java.util.UUID;

public class BTActions {

    private BluetoothAdapter baBTAdapter;
    final int INT_SHORT_WAIT = 5;
    private CommonFunctions cf;
    private UUID appUUID = null;

    public BTActions(){
        baBTAdapter = null;
        cf = new CommonFunctions();
    }

    /**
     * Method to initialize the Bluetooth adapter
     */
    public void setBTAdapter() {
        baBTAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothAdapter getBTAdapter() {
        return this.baBTAdapter;
    }

    /**
     * Method to verify if Bluetooth is supported in the device
     * @return - The {@link boolean} value is Bluetooth is supported or not
     */
    public boolean isBluetoothSupported(){
        setBTAdapter();
        return !(baBTAdapter == null);
    }

    /**
     * Method to check if Bluetooth is turned on or not
     * @return - The {@link boolean} value that informs if Bluetooth is On or not
     */
    public boolean isBluetoothTurnedOn() {
        return isBluetoothSupported() && baBTAdapter.isEnabled();
    }

    /**
     * Method to turn on Bluetooth
     * @return - The {@link boolean} value based on whether Bluetooth is turned on or not
     */
    public boolean turnOnBluetooth(){
        int intWait = 0;
        if(isBluetoothSupported()){
            if(!baBTAdapter.isEnabled()){
                BluetoothAdapter.getDefaultAdapter().enable();
                do{
                    cf.sleepForNSeconds(1);
                    intWait++;
                }
                while(!baBTAdapter.isEnabled() && intWait <= INT_SHORT_WAIT);
            }
        }
        else{
            return false;
        }

        return !(!baBTAdapter.isEnabled() || intWait > INT_SHORT_WAIT);
    }

    /**
     * Method to turn off Bluetooth
     * @return - The {@link boolean} value based on whether Bluetooth is turned off or not
     */
    public boolean turnOffBluetooth(){
        int intWait = 0;
        if(isBluetoothSupported()){
            if(baBTAdapter.isEnabled()){
                BluetoothAdapter.getDefaultAdapter().disable();
                do{
                    cf.sleepForNSeconds(1);
                    intWait++;
                }
                while(baBTAdapter.isEnabled() && intWait <= INT_SHORT_WAIT);
            }
        }
        else{
            return false;
        }

        return !(baBTAdapter.isEnabled() || intWait > INT_SHORT_WAIT);
    }

    /**
     * Method to retrieve the list of Paired devices
     * @return - The list of paired devices
     */
    public Set<BluetoothDevice> getPairedDevicesList() {
        if (!isBluetoothSupported()) {
            return null;
        }

        return baBTAdapter.getBondedDevices();
    }

    /**
     * Method to start discovery
     */
    public void startDiscovery(){
        if(isBluetoothSupported()){
            baBTAdapter.startDiscovery();
        }
    }

    /**
     * Method to check if Bluetooth is still discovering devices
     * @return - The {@link boolean} vlaue to verify if devices are still being discovered or not
     */
    public boolean isDiscovering() {
        return baBTAdapter.isDiscovering();
    }

    /**
     * Method to cancel Bluetooth discovery
     */
    public void cancelDiscovery() {
        baBTAdapter.cancelDiscovery();
    }

    public void connectToDevice(BluetoothDevice device) {


        BluetoothSocket socket = null;
        ParcelUuid[] uuids = null;
        try {
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            //ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(baBTAdapter, null);
            uuids = device.getUuids();
            //device.fetchUuidsWithSdp();
            final UUID MY_UUID_SECURE = UUID.fromString("00000001-0000-1000-8000-00805F9B34FB");
            //final UUID MY_UUID_SECURE = UUID.randomUUID();

            socket = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            //socket = device.createInsecureRfcommSocketToServiceRecord(uuids[0].getUuid());
            //socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            //socket = device.createRfcommSocketToServiceRecord(uuids[1].getUuid());
            socket.connect();
        } catch (NoSuchMethodException ignored) {}
        catch (IOException connectException) {
            try {
                Class<?> clazz = socket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};
                //BluetoothSocket fallback = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                //BluetoothSocket fallback =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                BluetoothSocket fallback =(BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] {UUID.class}).invoke(device,uuids[0].getUuid());
                fallback.connect();
                socket.close();
            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to generate a UUID for the application
     * @return - The {@link UUID}
     */
    /*
    private UUID generateUUID() {
        String android_id = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        final TelephonyManager tm = (TelephonyManager) getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        //Log.i("System out", "tmDevice : " + tmDevice);
        tmSerial = "" + tm.getSimSerialNumber();
        //Log.i("System out", "tmSerial : " + tmSerial);
        androidId = "" + android.provider.Settings.Secure.getString(
                    getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice
                .hashCode() << 32)
                | tmSerial.hashCode());
        return deviceUuid.toString();
    }
    */
}
