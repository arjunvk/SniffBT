package android.bluetooth;

interface IBluetooth {
    String getRemoteAlias(in String address);
    boolean setRemoteAlias(in String address, in String name);
}
