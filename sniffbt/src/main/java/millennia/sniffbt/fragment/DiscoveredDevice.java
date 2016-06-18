package millennia.sniffbt.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.DoubleBounce;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
    private TextView tvDiscoveredDevices;
    private TextView tvPairedDevices;
    private ListView lvDiscoveredList;
    private ListView lvPairedDevicesList;
    private ImageButton ibtnPair;
    private ImageButton ibtnUnPair;
    private ProgressBar pbDiscDevicesSpinner;
    private TextView tvSuggestBTOn;
    private ProgressBar pbLoading;

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
        defineUIObjects();

        // Position UI objects
        positionUIObjects();

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

    private void defineUIObjects() {
        tvDiscoveredDevices = (TextView) getView().findViewById(R.id.tvDiscoveredDevices);
        tvPairedDevices = (TextView) getView().findViewById(R.id.tvPairedDevices);
        lvDiscoveredList = (ListView) getView().findViewById(R.id.lstDiscoveredBTDevices);
        lvPairedDevicesList = (ListView) getView().findViewById(R.id.lstPairedBTDevices);
        ibtnPair = (ImageButton) getView().findViewById(R.id.pairBT);
        ibtnUnPair = (ImageButton) getView().findViewById(R.id.unpairBT);
        tvSuggestBTOn = (TextView) getView().findViewById(R.id.tvSuggestBTOn);
        pbDiscDevicesSpinner = (ProgressBar) getView().findViewById(R.id.pbDiscoveredDevices);

        pbLoading = (ProgressBar) getView().findViewById(R.id.spin_kit_progress);
        pbLoading.setIndeterminateDrawable(new DoubleBounce());
    }

    private void positionUIObjects() {
        final ViewGroup vgDiscDevice = (ViewGroup) getView().findViewById(R.id.rlDiscoveredDevice);
        final AtomicInteger aiLayoutHeight = new AtomicInteger();

        Rect rect = new Rect();

        // Get the window
        Window win = getActivity().getWindow();
        win.getDecorView().getWindowVisibleDisplayFrame(rect);

        // Get height of Status bar
        //int intStatusBarHeight = rect.top;

        /*
        // Get height of other contents
        int intContentViewHeight = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();

        // Calculate titleBarHeight by deducting statusBarHeight from contentViewTop
        int intTitleBarHeight = intContentViewHeight - intStatusBarHeight;
        //Log.i(TAG, "TitleHeight = " + intTitleBarHeight +
        //           " statusHeight = " + intStatusBarHeight +
        //           " contentHeight = " + intContentViewHeight);
        */

        // Find height of AppBarLayout
        //AppBarLayout appBar = (AppBarLayout) getActivity().findViewById(R.id.appBarLayout);
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        AppBarLayout.LayoutParams ablParams = (AppBarLayout.LayoutParams) tabLayout.getLayoutParams();
        //CoordinatorLayout.LayoutParams lpParams = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();

        // Obtain the screen height & width
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int intScreenHeight = metrics.heightPixels;
        int intScreenWidth = metrics.widthPixels;
        Log.i(TAG, "Actual Screen Height = " + intScreenHeight + " Width = " + intScreenWidth);

        // Now calculate the height that our layout can be set
        //aiLayoutHeight.set(intScreenHeight - (intTitleBarHeight + intStatusBarHeight));
        //Log.i(TAG, "Layout Height = " + aiLayoutHeight);

        // Set the height of the layout
        //ViewGroup.LayoutParams vgLayoutParams = vgDiscDevice.getLayoutParams();
        //vgLayoutParams.height = aiLayoutHeight.get();
        //vgDiscDevice.setLayoutParams(vgLayoutParams);

        // Set the height for Discovered Devices list
        RelativeLayout.LayoutParams rlParams;

        // Get height of Discovered Devices text view height
        rlParams = (RelativeLayout.LayoutParams) tvDiscoveredDevices.getLayoutParams();
        int intDiscoveredDevicesTextHeight = rlParams.height;
        //int intDiscoveredDevicesListHeight = (int)(Math.round(intScreenHeight * 0.45)/metrics.density);
        int intDiscoveredDevicesListHeight = (int)(Math.round(intScreenHeight * 0.45));
        Log.i(TAG, "Setting the height of Discovered Devices list as '" + intDiscoveredDevicesListHeight + "'");
        //aiLayoutHeight.set(intDiscoveredDevicesListHeight);
        //rlParams.height = aiLayoutHeight.get();
        rlParams = (RelativeLayout.LayoutParams) lvDiscoveredList.getLayoutParams();
        rlParams.height = intDiscoveredDevicesListHeight - rect.top - intDiscoveredDevicesTextHeight;
        //rlParams.height = intDiscoveredDevicesListHeight - rect.top;
        lvDiscoveredList.setLayoutParams(rlParams);


    }

    public void refreshFragment(boolean isVisibleToUser) {
        if(isVisibleToUser) {
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
                listDiscoveredBTDevices();
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
        final int intUnpairWaitTime = 15;

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
        lvDiscoveredList.setVisibility(intAction);
        lvPairedDevicesList.setVisibility(intAction);
        pbDiscDevicesSpinner.setVisibility(intAction);
        tvDiscoveredDevices.setVisibility(intAction);
        tvPairedDevices.setVisibility(intAction);
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

                pbDiscDevicesSpinner.clearAnimation();
                pbDiscDevicesSpinner.setVisibility(View.GONE);
                context.unregisterReceiver(btBroadcastReceiver);
            }
        }
    };
}
