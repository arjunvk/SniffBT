package arjunvijayakumar.sniffbt;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

import arjunvijayakumar.sniffbt.customRowWithCB.RowItem;

public interface SniffBTInterface {
    void displayDiscoveredList();
    SniffBT getSniffBTObj();
    RowItem[] getPairedDevicesList();
    void initiateBTScan();
    void endBTScan();
    void verifyIfAnyNearbyDeviceIsKnown(ArrayList<BluetoothDevice> arrCurrentNearbyDevices);
}
