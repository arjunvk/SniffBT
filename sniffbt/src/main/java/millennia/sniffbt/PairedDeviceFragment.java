package millennia.sniffbt;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Set;

import millennia.sniffbt.pairedDevice.*;

public class PairedDeviceFragment extends Fragment implements SniffBTInterface{

    Row[] arrPairedDevicesList;
    BTActions btActions;
    CommonFunctions cf;
    SharedPreferences appPrefs;
    ListView lvPairedDevicesList;
    CustomAdapter pairedDevicesCustomAdapter;

    public PairedDeviceFragment() {
        btActions = new BTActions();
        cf = new CommonFunctions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Define variables
        appPrefs = this.getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paired_device, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Display the paired devices on startup
        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), Row[].class) != null) {
            arrPairedDevicesList = (Row[]) cf.getSharedPreferences(appPrefs,
                                           getString(R.string.SH_PREF_Paired_Devices), Row[].class);
            lvPairedDevicesList = (ListView) getView().findViewById(R.id.lstPairedBTDevices);
            pairedDevicesCustomAdapter = new CustomAdapter(this, this.getContext(), arrPairedDevicesList);
            lvPairedDevicesList.setAdapter(pairedDevicesCustomAdapter);
        }
        else {
            listPairedBTDevices();
        }

        // Set the method for refreshing list of Paired devices
        final SwipeRefreshLayout refreshPairedDevices = (SwipeRefreshLayout) getView().findViewById(R.id.swipePairedBTDevicesRefresh);
        if (refreshPairedDevices != null) {
            refreshPairedDevices.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            listPairedBTDevices();
                            refreshPairedDevices.setRefreshing(false);
                        }
                    }
            );
        }
    }

    /**
     * Method to list the already paired Bluetooth devices
     */
    private void listPairedBTDevices(){
        boolean blnIsBTOn = false;
        Row[] storedPairedDevices = null;

        // Store the current state of Bluetooth
        if(btActions.isBluetoothTurnedOn()) {
            blnIsBTOn = true;
        }

        btActions.turnOnBluetooth();

        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), Row[].class) != null) {
            storedPairedDevices = (Row[]) cf.getSharedPreferences(appPrefs,
                    getString(R.string.SH_PREF_Paired_Devices), Row[].class);
        }

        arrPairedDevicesList = new Row[btActions.getPairedDevicesList().size()];

        lvPairedDevicesList = (ListView) getView().findViewById(R.id.lstPairedBTDevices);

        // Find the list of paired devices
        Set<BluetoothDevice> pairedDevices = btActions.getPairedDevicesList();

        if(!(pairedDevices == null)){
            if(pairedDevices.size() > 0){
                // Populate the ListView with the paired devices
                try{
                    // Loop through paired devices
                    for(int iCnt = 0; iCnt < pairedDevices.size(); iCnt++){
                        BluetoothDevice device = (BluetoothDevice)pairedDevices.toArray()[iCnt];

                        // Check for existing Paired devices setting
                        if (storedPairedDevices != null) {
                            boolean blnIsExistingDevice = false;
                            int iStoredDeviceCnt;
                            for(iStoredDeviceCnt = 0; iStoredDeviceCnt < storedPairedDevices.length; iStoredDeviceCnt++) {
                                if(device.getAddress().equals(storedPairedDevices[iStoredDeviceCnt].getDeviceAddress())) {
                                    blnIsExistingDevice = true;
                                    break;
                                }
                            }

                            if(blnIsExistingDevice) {
                                arrPairedDevicesList[iCnt] = new Row(device, storedPairedDevices[iStoredDeviceCnt].isCBChecked());
                            }
                            else {
                                arrPairedDevicesList[iCnt] = new Row(device, false);
                            }
                        }
                        else {
                            arrPairedDevicesList[iCnt] = new Row(device, false);
                        }
                    }

                    pairedDevicesCustomAdapter = new CustomAdapter(this, this.getContext(), arrPairedDevicesList);

                    lvPairedDevicesList.setAdapter(pairedDevicesCustomAdapter);
                }
                catch(NullPointerException npe){
                    System.out.println("List view is null");
                }
            }
        }

        // Leave Bluetooth in the same state as it was before entering this function
        if(!blnIsBTOn) {
            btActions.turnOffBluetooth();
        }

        // Save the Paired devices to Shared Preferences object
        cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), arrPairedDevicesList);
    }

    @Override
    public void pairedDeviceListSettingsChanged() {
        cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), arrPairedDevicesList);
    }
}
