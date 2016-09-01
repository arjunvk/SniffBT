package millennia.sniffbt.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.DoubleBounce;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import millennia.sniffbt.BTActions;
import millennia.sniffbt.CommonFunctions;
import millennia.sniffbt.R;

public class DiscoveredDevice extends Fragment{
    final String TAG = "DiscoverDevice Fragment";
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
    private RelativeLayout rlPairedDevices;
    private RelativeLayout rlDiscDevices;
    private TextView tvDiscoveredDevices;
    private TextView tvPairedDevices;
    private ListView lvDiscoveredList;
    private ListView lvPairedDevicesList;
    private ImageView ibtnPair;
    private ImageView ibtnUnPair;
    private TextView tvSuggestBTOn;
    private ProgressBar pbLoading;
    private FrameLayout flRefresh;
    private Button btnRefresh;

    public DiscoveredDevice() {
        btActions = new BTActions();
        cf = new CommonFunctions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Begin render of Discovered Device fragment...");
        super.onCreate(savedInstanceState);

        // Define variables
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
        defineUIObjects();

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

        // Listener for Refresh button
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserVisibleHint(true);
            }
        });

        blnIsFragmentLoaded = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refreshFragment(isVisibleToUser);
    }

    private void defineUIObjects() {
        rlPairedDevices = (RelativeLayout) getView().findViewById(R.id.rlPairedDevice);
        rlDiscDevices = (RelativeLayout) getView().findViewById(R.id.rlDiscoveredDevice);
        tvDiscoveredDevices = (TextView) getView().findViewById(R.id.tvDiscoveredDevices);
        tvPairedDevices = (TextView) getView().findViewById(R.id.tvPairedDevices);
        lvDiscoveredList = (ListView) getView().findViewById(R.id.lstDiscoveredBTDevices);
        lvPairedDevicesList = (ListView) getView().findViewById(R.id.lstPairedBTDevices);
        ibtnPair = (ImageView) getView().findViewById(R.id.pairBT);
        ibtnUnPair = (ImageView) getView().findViewById(R.id.unpairBT);
        tvSuggestBTOn = (TextView) getView().findViewById(R.id.tvSuggestBTOn);
        flRefresh = (FrameLayout) getView().findViewById(R.id.flRefreshDiscDevices);
        btnRefresh = (Button) getView().findViewById(R.id.btnRefreshDiscDevices);

        pbLoading = (ProgressBar) getView().findViewById(R.id.spin_kit_progress);
        pbLoading.setIndeterminateDrawable(new DoubleBounce());
    }

    public void refreshFragment(boolean isVisibleToUser) {
        if(isVisibleToUser) {
            blnIsFragmentLoaded = true;

            // Refresh the Discoverable devices if Bluetooth is on
            if(btActions.isBluetoothTurnedOn()) {
                Log.i(TAG, "Bluetooth is turned on. Display all objects on fragment");

                hideUnhideLists(View.VISIBLE);
                tvSuggestBTOn.setVisibility(View.GONE);
                pbLoading.setVisibility(View.GONE); // CAN BE REMOVED AFTER UPDATING XML

                // Clear both the lists
                Log.i(TAG, "Clearing both lists");
                btDiscListArrayAdapter.clear();
                lvDiscoveredList.setAdapter(btDiscListArrayAdapter);

                btPairedListArrayAdapter.clear();
                lvPairedDevicesList.setAdapter(btPairedListArrayAdapter);

                listPairedBTDevices();
                strDiscoveredListItemSelected = "";

                refreshDiscoveredDevices();
            }
            else {
                Log.i(TAG, "Bluetooth is turned off. Hiding all objects on this fragment");

                hideUnhideLists(View.GONE);
                tvSuggestBTOn.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE); // CAN BE REMOVED AFTER UPDATING XML
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
            blnIsFragmentLoaded = false;
        }
    }

    /**
     * Method to refresh discovered devices
     */
    private void refreshDiscoveredDevices() {
        Log.i(TAG, "Refreshing Discoverable devices...");

        // Disable the Refresh button
        btnRefresh.setEnabled(false);

        Animation animation = new RotateAnimation(0.0f, 360.0f,
                                  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                                  0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(2000);

        flRefresh.setAnimation(animation);

        listDiscoveredBTDevices();
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
    }

    /**
     * Method to display the list of paired BT devices. This function assumes BT is on.
     */
    private void listPairedBTDevices() {
        if(btActions.isBluetoothTurnedOn()) {
            updatePairedDevicesList();
            btPairedListArrayAdapter.clear();
            for(BluetoothDevice device : arrPairedDevicesList) {
                btPairedListArrayAdapter.add(device.getName());
            }

            lvPairedDevicesList.setAdapter(btPairedListArrayAdapter);
        }
    }

    /**
     * Method to update arrPairedDevices
     */
    private void updatePairedDevicesList() {
        boolean blnIsBtOn = true;
        if(!btActions.isBluetoothTurnedOn()) {
            blnIsBtOn = false;
            btActions.turnOnBluetooth();
        }

        arrPairedDevicesList = btActions.getPairedDevicesList();

        if(!blnIsBtOn) {
            btActions.turnOffBluetooth();
        }
    }

    /**
     * Method to pair a Bluetooth device
     * @param device - The {@link BluetoothDevice}
     */
    private void pairDevice(final BluetoothDevice device) {
        final Handler hPBHandler = new Handler();
        final int intPairWaitTime = 45;

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            // Run a separate thread to display/hide the Progress bar
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int intWaitLoad = 0;
                    boolean blnIsDevicePairedSuccessfully;

                    hPBHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pbLoading.setVisibility(View.VISIBLE);
                        }
                    });

                    do {
                        updatePairedDevicesList();
                        cf.sleepForNSeconds(1);
                        intWaitLoad++;
                        blnIsDevicePairedSuccessfully = isDevicePresentInPairedDevicesList(device);
                    }
                    while(!blnIsDevicePairedSuccessfully && intWaitLoad < intPairWaitTime);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbLoading.setVisibility(View.GONE);
                            listPairedBTDevices();
                            strDiscoveredListItemSelected = "";
                            listDiscoveredBTDevices();
                        }
                    });

                    if(blnIsDevicePairedSuccessfully && intWaitLoad < intPairWaitTime) {
                        cf.showSnackBar(getView(), "'" + device.getName() + "' has been paired.", Snackbar.LENGTH_SHORT);
                    }
                    else {
                        cf.showSnackBar(getView(), "'" + device.getName() + "' did not pair.", Snackbar.LENGTH_SHORT);
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to unpair a Bluetooth device
     * @param device - The {@link BluetoothDevice}
     */
    private void unpairDevice(final BluetoothDevice device) {
        final Handler hPBHandler = new Handler();
        final int intUnpairWaitTime = R.integer.unpair_wait_time;

        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            // Run a separate thread to display/hide the Progress bar
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int intWaitLoad = 0;
                    boolean blnIsDeviceUnpairedSuccessfully;

                    hPBHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pbLoading.setVisibility(View.VISIBLE);
                        }
                    });

                    do {
                        updatePairedDevicesList();
                        cf.sleepForNSeconds(1);
                        intWaitLoad++;
                        blnIsDeviceUnpairedSuccessfully = !isDevicePresentInPairedDevicesList(device);
                    }
                    while(!blnIsDeviceUnpairedSuccessfully && intWaitLoad < intUnpairWaitTime);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbLoading.setVisibility(View.GONE);
                            listPairedBTDevices();
                            strDiscoveredListItemSelected = "";
                            listDiscoveredBTDevices();
                        }
                    });

                    if(blnIsDeviceUnpairedSuccessfully && intWaitLoad < intUnpairWaitTime) {
                        cf.showSnackBar(getView(), "'" + device.getName() + "' has been unpaired.", Snackbar.LENGTH_SHORT);
                    }
                    else {
                        cf.showSnackBar(getView(), "'" + device.getName() + "' did not unpair.", Snackbar.LENGTH_SHORT);
                    }
                }
            }).start();

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
                    if (pairedDevice.getAddress().equals(nearbyDevice.getAddress())) {
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
        rlPairedDevices.setVisibility(intAction);
        rlDiscDevices.setVisibility(intAction);
        flRefresh.setVisibility(intAction);
    }

    private boolean isDevicePresentInPairedDevicesList(BluetoothDevice device) {
        boolean blnIsDevicePresent = false;
        for(BluetoothDevice eachDevice : arrPairedDevicesList) {
            if(eachDevice.getAddress().equals(device.getAddress())) {
                blnIsDevicePresent = true;
                break;
            }
        }

        return blnIsDevicePresent;
    }

    private String getBTMajorDeviceClass(int major){
        switch(major){
            case BluetoothClass.Device.Major.AUDIO_VIDEO    :   return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER       : 	return "COMPUTER";
            case BluetoothClass.Device.Major.HEALTH         :	return "HEALTH";
            case BluetoothClass.Device.Major.IMAGING        :   return "IMAGING";
            case BluetoothClass.Device.Major.MISC           :	return "MISC";
            case BluetoothClass.Device.Major.NETWORKING     :   return "NETWORKING";
            case BluetoothClass.Device.Major.PERIPHERAL     :   return "PERIPHERAL";
            case BluetoothClass.Device.Major.PHONE          :   return "PHONE";
            case BluetoothClass.Device.Major.TOY            :   return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED  :	return "UNCATEGORIZED";
            case BluetoothClass.Device.Major.WEARABLE       :   return "WEARABLE";
            default                                         :   return "unknown";
        }
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
                    Log.i(TAG, "Device '" + device.getName() + "' is of type '" +
                               getBTMajorDeviceClass(device.getBluetoothClass().getMajorDeviceClass()) + "'");
                    arrDiscoveredDevicesList.add(device);
                    displayDiscoveredDevices();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Bluetooth discovery finished");

                displayDiscoveredDevices();

                flRefresh.clearAnimation();
                context.unregisterReceiver(btBroadcastReceiver);

                // Enable the Refresh button
                btnRefresh.setEnabled(true);
            }
        }
    };
}
