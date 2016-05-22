package millennia.sniffbt.customRowWithCB;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class RowItem implements Serializable {
    private BluetoothDevice device;
    private boolean blnIsCBChecked;

    public RowItem(BluetoothDevice device, boolean blnCBChecked) {
        this.device = device;
        this.blnIsCBChecked = blnCBChecked;
    }

    public void setCB(boolean blnCBChecked) {
        this.blnIsCBChecked = blnCBChecked;
    }

    public String getDeviceName() {
        return this.device.getName();
    }

    public boolean isCBChecked() {
        return this.blnIsCBChecked;
    }
}
