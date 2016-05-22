package millennia.sniffbt;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import millennia.sniffbt.customRowWithCB.CustomAdapter;
import millennia.sniffbt.customRowWithCB.RowItem;

public class MainActivity extends AppCompatActivity implements SniffBTInterface{

    // Initialize variables
    final String TAG = "Main Activity";
    private BTActions btActions;
    private RowItem[] arrPairedDevicesList;
    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    ArrayAdapter<String> btDiscListArrayAdapter;
    Intent intentListenBT;
    ListView lvPairedDevicesList;
    private ProgressDialog mProgressDlg;
    SharedPreferences appPrefs;
    CustomAdapter pairedDevicesCustomAdapter;
    CommonFunctions cf;


    // Initialize constructor
    public MainActivity(){
        btActions = new BTActions();
        cf = new CommonFunctions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define variables
        appPrefs = getPreferences(MODE_PRIVATE);

        // Define the lists on MainActivity
        btDiscListArrayAdapter = new ArrayAdapter<>(this, R.layout.simple_row, R.id.simple_row_Txt);

        // Define the Progress Dialog if scanning is being done
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        //mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface().OnClickListener() {
        //    @Override
        //    public void onClick(DialogInterface dialog, int which) {

        //    }
        //});

        // Set the custom ActionBar toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Display the paired devices on startup
        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), RowItem[].class) != null) {
            arrPairedDevicesList = (RowItem[]) cf.getSharedPreferences(appPrefs,
                                               getString(R.string.SH_PREF_Paired_Devices), RowItem[].class);
            lvPairedDevicesList = (ListView)findViewById(R.id.lstPairedBTDevices);
            pairedDevicesCustomAdapter = new CustomAdapter(this, arrPairedDevicesList);
            lvPairedDevicesList.setAdapter(pairedDevicesCustomAdapter);
        }
        else {
            listPairedBTDevices();
        }

        // Set the method for refreshing list of Paired devices
        final SwipeRefreshLayout refreshPairedDevices = (SwipeRefreshLayout)findViewById(R.id.swipePairedBTDevicesRefresh);
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

        // Set the method to refreshing list os Discovered devices
        final SwipeRefreshLayout refreshDiscoveredDevices = (SwipeRefreshLayout)findViewById(R.id.swipeDiscoveredBTDevicesRefresh);
        if(refreshDiscoveredDevices != null) {
            refreshDiscoveredDevices.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if(btActions.isBluetoothTurnedOn()) {
                                mProgressDlg.show();
                                listDiscoveredBTDevices();
                            }
                            else {
                                showToast("Please turn on Bluetooth");
                            }
                            refreshDiscoveredDevices.setRefreshing(false);
                        }
                    }
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);

        // Set the Bluetooth icon based on state
        if(btActions.isBluetoothTurnedOn()) {
            menu.findItem(R.id.actionbar_Bluetooth).setIcon(R.drawable.ic_action_bt_on);
        }
        else {
            menu.findItem(R.id.actionbar_Bluetooth).setIcon(R.drawable.ic_action_bt_off);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean blnToReturn;
        switch (item.getItemId()) {
            case R.id.actionbar_Bluetooth:
                if(btActions.isBluetoothTurnedOn()) {
                    btActions.turnOffBluetooth();
                    item.setIcon(R.drawable.ic_action_bt_off);
                    showToast(getString(R.string.text_bluetooth_off));
                }
                else {
                    btActions.turnOnBluetooth();
                    item.setIcon(R.drawable.ic_action_bt_on);
                    showToast(getString(R.string.text_bluetooth_on));
                }
                blnToReturn = true;
                break;

            case R.id.actionbar_SniffBT:
                // Start service to listen to BT
                intentListenBT = new Intent(getApplicationContext(), ListenBTIntentService.class);
                Log.i(TAG, "Starting Intent Service...");

                intentListenBT.putExtra("PairedDevicesList", arrPairedDevicesList);
                getApplicationContext().startService(intentListenBT);

                blnToReturn = true;
                break;

            default:
                blnToReturn = super.onOptionsItemSelected(item);
                break;
        }
        return blnToReturn;
    }

    /**
     * Method to display a message on the Toast widget
     * @param message - The {@link String} message to display
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to list the already paired Bluetooth devices
     */
    private void listPairedBTDevices(){
        boolean blnIsBTOn = false;

        // Store the current state of Bluetooth
        if(btActions.isBluetoothTurnedOn()) {
            blnIsBTOn = true;
        }

        btActions.turnOnBluetooth();

        arrPairedDevicesList = new RowItem[btActions.getPairedDevicesList().size()];

        lvPairedDevicesList = (ListView)findViewById(R.id.lstPairedBTDevices);

        // Find the list of paired devices
        Set<BluetoothDevice> pairedDevices = btActions.getPairedDevicesList();

        if(!(pairedDevices == null)){
            if(pairedDevices.size() > 0){
                // Populate the ListView with the paired devices
                try{
                    // Loop through paired devices
                    for(int iCnt = 0; iCnt < pairedDevices.size(); iCnt++){
                        BluetoothDevice device = (BluetoothDevice)pairedDevices.toArray()[iCnt];
                        arrPairedDevicesList[iCnt] = new RowItem(device, false);
                    }

                    pairedDevicesCustomAdapter = new CustomAdapter(this, arrPairedDevicesList);

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

    /**
     * Method to update app settings when Paired devices list is updated by user
     */
    public void pairedDeviceListSettingsChanged() {
        cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), arrPairedDevicesList);
    }

    /**
     * Method to display the list of discovered BT devices
     */
    private void listDiscoveredBTDevices() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(btBroadcastReceiver, filter);

        if(btActions.isDiscovering()) {
            btActions.cancelDiscovery();
        }

        // Start discovery
        btActions.startDiscovery();
    }

    /**
     * This method disables the scheduler when a checkbox is clicked
     * @param view - The {@link View}
     */
    public void onCheckBoxClicked(View view) {
        if(intentListenBT != null) {
            getApplicationContext().stopService(intentListenBT);
            intentListenBT = null;

            // Change the scheduler icon to 'not' turned on
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
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btDiscListArrayAdapter.clear();
                for (int iCnt = 0; iCnt < arrDiscoveredDevicesList.size(); iCnt++) {
                    btDiscListArrayAdapter.add(arrDiscoveredDevicesList.get(iCnt).getName());
                }

                final ListView lv = (ListView)findViewById(R.id.lstDiscoveredBTDevices);
                lv.setAdapter(btDiscListArrayAdapter);

                mProgressDlg.dismiss();
                showToast("Scan complete");
                unregisterReceiver(btBroadcastReceiver);
            }
        }
    };
}
