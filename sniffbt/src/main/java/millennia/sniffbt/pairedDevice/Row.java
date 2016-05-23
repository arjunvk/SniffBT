package millennia.sniffbt.pairedDevice;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class Row implements Serializable {
    //private BluetoothDevice device;
    private String name;
    private String address;
    private boolean blnIsCBChecked;

    public Row(BluetoothDevice device, boolean blnCBChecked) {
        this.name = device.getName();
        this.address = device.getAddress();
        this.blnIsCBChecked = blnCBChecked;
    }

    public String getDeviceAddress() { return this.address;}

    public void setCB(boolean blnCBChecked) {
        this.blnIsCBChecked = blnCBChecked;
    }

    public String getDeviceName() { return this.name;}

    public boolean isCBChecked() {
        return this.blnIsCBChecked;
    }
}
