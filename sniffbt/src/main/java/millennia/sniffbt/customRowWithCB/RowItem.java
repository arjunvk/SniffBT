package millennia.sniffbt.customRowWithCB;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class RowItem implements Serializable {
    private BluetoothDevice device;
    private String name;
    private boolean blnIsCBChecked;

    public RowItem(BluetoothDevice device, boolean blnCBChecked) {
        this.device = device;
        this.name = device.getName();
        this.blnIsCBChecked = blnCBChecked;
    }

    public BluetoothDevice getDevice() { return this.device;}

    public void setCB(boolean blnCBChecked) {
        this.blnIsCBChecked = blnCBChecked;
    }

    public String getDeviceName() { return this.name;}

    public boolean isCBChecked() {
        return this.blnIsCBChecked;
    }
}
