package millennia.sniffbt.pairedDevice;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class Row implements Serializable {
    private String name;
    private String address;
    private boolean blnIsCBChecked;
    private boolean blnIsPersistent;
    private int intBluetoothMajorClass;

    public Row(BluetoothDevice device, boolean blnCBChecked) {
        this.name = device.getName();
        this.address = device.getAddress();
        this.blnIsCBChecked = blnCBChecked;
        this.blnIsPersistent = false;
        this.intBluetoothMajorClass = device.getBluetoothClass().getMajorDeviceClass();
    }

    public String getDeviceAddress() { return this.address;}

    public void setCB(boolean blnCBChecked) {
        this.blnIsCBChecked = blnCBChecked;
    }

    public String getDeviceName() { return this.name;}

    public boolean isCBChecked() {
        return this.blnIsCBChecked;
    }

    public void setDevicePersistence(boolean blnIsPersistent) {
        this.blnIsPersistent = blnIsPersistent;
    }

    public boolean getDevicePersistence() {
        return this.blnIsPersistent;
    }

    public int getDeviceMajorClass() { return this.intBluetoothMajorClass; }
}
