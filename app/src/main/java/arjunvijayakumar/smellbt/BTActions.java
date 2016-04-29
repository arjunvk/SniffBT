package arjunvijayakumar.smellbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public class BTActions {

    BluetoothAdapter baBTAdapter;
    final int INT_SHORT_WAIT = 5;
    private CommonFunctions cf;

    public BTActions(){
        baBTAdapter = null;
        cf = new CommonFunctions();
    }

    /**
     * Method to verify if Bluetooth is supported in the device
     * @return - The {@link boolean} value is Bluetooth is supported or not
     */
    public boolean isBluetoothSupported(){
        baBTAdapter = BluetoothAdapter.getDefaultAdapter();
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
    public Set<BluetoothDevice> getPairedDevicesList(){
        if(isBluetoothSupported()){
            if(!baBTAdapter.isEnabled()){
                turnOnBluetooth();
            }
        }
        else{
            return null;
        }

        return baBTAdapter.getBondedDevices();
    }
}
