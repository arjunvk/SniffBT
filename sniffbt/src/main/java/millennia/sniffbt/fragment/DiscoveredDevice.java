package millennia.sniffbt.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import millennia.sniffbt.BTActions;
import millennia.sniffbt.CommonFunctions;
import millennia.sniffbt.R;
import millennia.sniffbt.SniffBTInterface;
import millennia.sniffbt.pairedDevice.CustomArrayAdapter;
import millennia.sniffbt.pairedDevice.Row;

public class DiscoveredDevice extends Fragment{

    private SharedPreferences appPrefs;
    private BTActions btActions;
    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    private Set<BluetoothDevice> arrPairedDevicesList;
    private ArrayAdapter<String> btDiscListArrayAdapter;
    private ArrayAdapter<String> btPairedListArrayAdapter;
    private ListView lvDiscoveredList;
    private ListView lvPairedDevicesList;
    private String strDiscoveredListItemSelected = "";
    private String strPairedListItemSelected = "";
    private CommonFunctions cf;
    private ProgressBar pbDiscDevicesSpinner;

    public DiscoveredDevice() {
        btActions = new BTActions();
        cf = new CommonFunctions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define variables
        appPrefs = this.getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discovered_device, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Define the lists on DiscoveredDevice fragment
        btDiscListArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.simple_row, R.id.simple_row_Txt);
        btPairedListArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.simple_row, R.id.simple_row_Txt);

        lvDiscoveredList = (ListView) getView().findViewById(R.id.lstDiscoveredBTDevices);
        lvPairedDevicesList = (ListView) getView().findViewById(R.id.lstPairedBTDevices);
        ImageButton ibtnPair = (ImageButton) getView().findViewById(R.id.pairBT);
        ImageButton ibtnUnPair = (ImageButton) getView().findViewById(R.id.unpairBT);
        pbDiscDevicesSpinner = (ProgressBar) getView().findViewById(R.id.pbDiscoveredDevices);

        // Set the listeners

        // Store the Discovered list item that is selected
        lvDiscoveredList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                strDiscoveredListItemSelected = (String) lvDiscoveredList.getItemAtPosition(position);
            }
        });

        // Store the Paired list item that is selected
        lvPairedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                strPairedListItemSelected = (String) lvPairedDevicesList.getItemAtPosition(position);
            }
        });

        // Listener for pairing action
        ibtnPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!strDiscoveredListItemSelected.equals("")) {
                    for(BluetoothDevice device : arrDiscoveredDevicesList) {
                        if(device.getName().equals(strDiscoveredListItemSelected)) {
                            pairDevice(device);
                        }
                    }
                }
                else {
                    cf.showSnackBar(getView(), "Please select a device to Pair", Snackbar.LENGTH_SHORT);
                }
            }
        });

        // Listener for unpairing action
        ibtnUnPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!strPairedListItemSelected.equals("")) {
                    if(btActions.isBluetoothTurnedOn()) {
                        Set<BluetoothDevice> setPairedBTDevices = btActions.getPairedDevicesList();
                        for(BluetoothDevice device : setPairedBTDevices) {
                            if(device.getName().equals(strPairedListItemSelected)) {
                                unpairDevice(device);
                            }
                        }
                    }
                    else {
                        cf.showSnackBar(getView(), "Please turn on Bluetooth", Snackbar.LENGTH_SHORT);
                    }
                }
                else {
                    cf.showSnackBar(getView(), "Please select a device to Unpair", Snackbar.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
            // Refresh the Discoverable devices if Bluetooth is on
            if(btActions.isBluetoothTurnedOn()) {
                listPairedBTDevices();
                strDiscoveredListItemSelected = "";
                listDiscoveredBTDevices();
            }
            else {
                // Clear both the lists
                btDiscListArrayAdapter.clear();
                lvDiscoveredList.setAdapter(btDiscListArrayAdapter);

                btPairedListArrayAdapter.clear();
                lvPairedDevicesList.setAdapter(btPairedListArrayAdapter);

            }
        }
        else {
            // Cancel Bluetooth discovery if user moves to another tab
            if(btActions.isBluetoothTurnedOn()) {
                if(btActions.isDiscovering()) {
                    btActions.cancelDiscovery();
                }
            }
        }
    }

    /**
     * Method to display the list of discovered BT devices
     */
    private void listDiscoveredBTDevices() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getContext().registerReceiver(btBroadcastReceiver, filter);

        if(btActions.isDiscovering()) {
            btActions.cancelDiscovery();
        }

        // Start discovery
        btActions.startDiscovery();
        pbDiscDevicesSpinner.setVisibility(View.VISIBLE);
    }

    /**
     * Method to display the list of paired BT devices. This function assumes BT is on.
     */
    private void listPairedBTDevices() {
        arrPairedDevicesList = btActions.getPairedDevicesList();
        btPairedListArrayAdapter.clear();
        for(BluetoothDevice device : arrPairedDevicesList) {
            btPairedListArrayAdapter.add(device.getName());
        }

        lvPairedDevicesList.setAdapter(btPairedListArrayAdapter);
    }

    /**
     * Method to pair a Bluetooth device
     * @param device - The {@link BluetoothDevice}
     */
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            cf.showSnackBar(getView(), "'" + device.getName() + "' has been paired.", Snackbar.LENGTH_SHORT);

            // Wait for a few seconds before making the following call

            listPairedBTDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to unpair a Bluetooth device
     * @param device - The {@link BluetoothDevice}
     */
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            cf.showSnackBar(getView(), "'" + device.getName() + "' has been unpaired.", Snackbar.LENGTH_SHORT);

            // Wait for a few seconds before making the following call

            listPairedBTDevices();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to check if any of the nearby devices is part of the Paired list
     */
    public void displayDiscoveredDevices() {
        if (arrDiscoveredDevicesList != null) {
            for (BluetoothDevice nearbyDevice : arrDiscoveredDevicesList) {
                for (BluetoothDevice pairedDevice : arrPairedDevicesList) {
                    if (pairedDevice == nearbyDevice) {
                        arrDiscoveredDevicesList.remove(nearbyDevice);
                    }
                }
            }

            lvDiscoveredList.setAdapter(btDiscListArrayAdapter);
        }
    }

    private final BroadcastReceiver btBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                arrDiscoveredDevicesList = new ArrayList<>();
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                if(device.getName() != null) {
                    arrDiscoveredDevicesList.add(device);
                    displayDiscoveredDevices();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btDiscListArrayAdapter.clear();
                for (int iCnt = 0; iCnt < arrDiscoveredDevicesList.size(); iCnt++) {
                    btDiscListArrayAdapter.add(arrDiscoveredDevicesList.get(iCnt).getName());
                }

                displayDiscoveredDevices();

                pbDiscDevicesSpinner.setVisibility(View.GONE);
                context.unregisterReceiver(btBroadcastReceiver);
            }
        }
    };
}
