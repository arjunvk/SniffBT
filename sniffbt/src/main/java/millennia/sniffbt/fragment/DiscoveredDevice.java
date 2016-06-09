package millennia.sniffbt.fragment;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import millennia.sniffbt.BTActions;
import millennia.sniffbt.CommonFunctions;
import millennia.sniffbt.R;

public class DiscoveredDevice extends Fragment{
    final String TAG = "DiscoverDevice Fragment";
    private SharedPreferences appPrefs;
    private BTActions btActions;
    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    private Set<BluetoothDevice> arrPairedDevicesList;
    private ArrayAdapter<String> btDiscListArrayAdapter;
    private ArrayAdapter<String> btPairedListArrayAdapter;
    private String strDiscoveredListItemSelected = "";
    private String strPairedListItemSelected = "";
    private CommonFunctions cf;
    private boolean blnIsFragmentLoaded = false;

    // UI Objects
    private TextView tvAvailableDevices;
    private TextView tvPairedDevices;
    private ListView lvDiscoveredList;
    private ListView lvPairedDevicesList;
    private ImageButton ibtnPair;
    private ImageButton ibtnUnPair;
    private ProgressBar pbDiscDevicesSpinner;
    private ObjectAnimator pbAnimation;
    private TextView tvSuggestBTOn;

    public DiscoveredDevice() {
        btActions = new BTActions();
        cf = new CommonFunctions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Begin render of Discovered Device fragment...");
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

        // Define UI Objects
        tvAvailableDevices = (TextView) getView().findViewById(R.id.tvAvailableDevices);
        tvPairedDevices = (TextView) getView().findViewById(R.id.tvPairedDevices);
        lvDiscoveredList = (ListView) getView().findViewById(R.id.lstDiscoveredBTDevices);
        lvPairedDevicesList = (ListView) getView().findViewById(R.id.lstPairedBTDevices);
        ibtnPair = (ImageButton) getView().findViewById(R.id.pairBT);
        ibtnUnPair = (ImageButton) getView().findViewById(R.id.unpairBT);
        tvSuggestBTOn = (TextView) getView().findViewById(R.id.tvSuggestBTOn);

        pbDiscDevicesSpinner = (ProgressBar) getView().findViewById(R.id.pbDiscoveredDevices);
        pbAnimation = ObjectAnimator.ofInt (pbDiscDevicesSpinner, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        pbAnimation.setDuration (5000); //in milliseconds
        pbAnimation.setInterpolator (new DecelerateInterpolator());

        // Set the listeners
        Log.i(TAG, "Setting the listeners");

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

        blnIsFragmentLoaded = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refreshFragment(isVisibleToUser);
    }

    public void refreshFragment(boolean isVisibleToUser) {
        if(isVisibleToUser) {
            // Refresh the Discoverable devices if Bluetooth is on
            if(btActions.isBluetoothTurnedOn()) {
                Log.i(TAG, "Bluetooth is turned on. Display all objects on fragment");

                hideUnhideLists(View.VISIBLE);
                tvSuggestBTOn.setVisibility(View.GONE);

                // Clear both the lists
                Log.i(TAG, "Clearing both lists");
                btDiscListArrayAdapter.clear();
                lvDiscoveredList.setAdapter(btDiscListArrayAdapter);

                btPairedListArrayAdapter.clear();
                lvPairedDevicesList.setAdapter(btPairedListArrayAdapter);

                listPairedBTDevices();
                strDiscoveredListItemSelected = "";
                listDiscoveredBTDevices();
            }
            else {
                Log.i(TAG, "Bluetooth is turned off. Hiding all objects on this fragment");

                hideUnhideLists(View.GONE);
                tvSuggestBTOn.setVisibility(View.VISIBLE);
            }
        }
        else {
            // Cancel Bluetooth discovery if user moves to another tab
            if(btActions.isBluetoothTurnedOn()) {
                if(blnIsFragmentLoaded) {
                    hideUnhideLists(View.VISIBLE);
                    tvSuggestBTOn.setVisibility(View.GONE);
                }
                if(btActions.isDiscovering()) {
                    btActions.cancelDiscovery();
                }
            }
            else {
                if(blnIsFragmentLoaded) {
                    hideUnhideLists(View.GONE);
                    tvSuggestBTOn.setVisibility(View.VISIBLE);
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
        Log.i(TAG, "Starting Bluetooth discovery...");
        btActions.startDiscovery();
        pbDiscDevicesSpinner.setVisibility(View.VISIBLE);
        pbAnimation.start();
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
    private void displayDiscoveredDevices() {
        if (arrDiscoveredDevicesList != null) {
            for (BluetoothDevice nearbyDevice : arrDiscoveredDevicesList) {
                for (BluetoothDevice pairedDevice : arrPairedDevicesList) {
                    if (pairedDevice == nearbyDevice) {
                        arrDiscoveredDevicesList.remove(nearbyDevice);
                    }
                }
            }

            btDiscListArrayAdapter.clear();
            for (int iCnt = 0; iCnt < arrDiscoveredDevicesList.size(); iCnt++) {
                btDiscListArrayAdapter.add(arrDiscoveredDevicesList.get(iCnt).getName());
            }

            Log.i(TAG, "Displaying Discovered devices");
            lvDiscoveredList.setAdapter(btDiscListArrayAdapter);
        }
    }

    private void hideUnhideLists(int intAction) {
        ibtnPair.setVisibility(intAction);
        ibtnUnPair.setVisibility(intAction);
        lvDiscoveredList.setVisibility(intAction);
        lvPairedDevicesList.setVisibility(intAction);
        pbDiscDevicesSpinner.setVisibility(intAction);
        tvAvailableDevices.setVisibility(intAction);
        tvPairedDevices.setVisibility(intAction);
    }

    private final BroadcastReceiver btBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "Bluetooth discovery started...");
                arrDiscoveredDevicesList = new ArrayList<>();
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                if(device.getName() != null) {
                    Log.i(TAG, "Bluetooth device found - '" + device.getName() + "'");
                    arrDiscoveredDevicesList.add(device);
                    displayDiscoveredDevices();
                    //btDiscListArrayAdapter.notifyDataSetChanged();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Bluetooth discovery finished");

                displayDiscoveredDevices();

                pbDiscDevicesSpinner.clearAnimation();
                pbDiscDevicesSpinner.setVisibility(View.GONE);
                context.unregisterReceiver(btBroadcastReceiver);
            }
        }
    };
}
