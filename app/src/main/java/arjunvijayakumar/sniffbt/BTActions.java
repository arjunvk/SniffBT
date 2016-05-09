package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

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
        try {
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(baBTAdapter, null);
            final UUID MY_UUID_SECURE =
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            //socket = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            socket.connect();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        catch (IOException connectException) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
