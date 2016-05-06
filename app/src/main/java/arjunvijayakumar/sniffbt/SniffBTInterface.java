package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public interface SniffBTInterface {
    void displayDiscoveredList();
    SniffBT getSniffBTObj();
    void initiateBTScan();
    void endBTScan();
    void verifyIfAnyNearbyDeviceIsKnown(ArrayList<BluetoothDevice> arrCurrentNearbyDevices);
}
