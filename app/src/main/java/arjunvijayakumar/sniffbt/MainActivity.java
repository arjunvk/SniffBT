package arjunvijayakumar.sniffbt;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import arjunvijayakumar.sniffbt.customRowWithCB.CustomAdapter;
import arjunvijayakumar.sniffbt.customRowWithCB.RowItem;

public class MainActivity extends AppCompatActivity implements SniffBTInterface {

    // Initialize variables
    final String TAG = "Main Activity";
    private BTActions btActions = new BTActions();
    private RowItem[] arrPairedDevicesList;
    private ArrayList<BluetoothDevice> arrDiscoveredDevicesList;
    ArrayAdapter<String> btListAdapter;
    SniffBTBroadcastReceiver btaBR;
    private ProgressDialog mProgressDlg;

    // Initialize constructor
    //public MainActivity(){
        //btActions = new BTActions();
    //}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        //unregisterReceiver(smellBTBRReceiveBTList);
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
                Intent intentListenBT = new Intent(getApplicationContext(), ListenBTIntentService.class);
                Log.i(TAG, "Starting Intent Service...");
                getApplicationContext().startService(intentListenBT);

                // Register receiver to receive the BT list
                IntentFilter intentFilterReceiveBT = new IntentFilter(getString(R.string.intent_Broadcast));
                SniffBTBroadcastReceiver sniffBTBRReceiveBTList = new SniffBTBroadcastReceiver(this, false);
                registerReceiver(sniffBTBRReceiveBTList, intentFilterReceiveBT);
                //ReceiveBTBroadcastReceiver receiveBT = new ReceiveBTBroadcastReceiver();
                //registerReceiver(receiveRT, intentFilterReceiveBT);


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
    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to list the already paired Bluetooth devices
     */
    private void listPairedBTDevices(){
        CustomAdapter customAdapter;
        boolean blnIsBTOn = false;

        // Store the current state of Bluetooth
        if(btActions.isBluetoothTurnedOn()) {
            blnIsBTOn = true;
        }

        btActions.turnOnBluetooth();

        arrPairedDevicesList = new RowItem[btActions.getPairedDevicesList().size()];

        ListView listView = (ListView)findViewById(R.id.lstPairedBTDevices);

        // Find the list of paired devices
        Set<BluetoothDevice> pairedDevices = btActions.getPairedDevicesList();

        if(!(pairedDevices == null)){
            if(pairedDevices.size() > 0){
                // Populate the ListView with the paired devices
                try{
                    // Loop through paired devices
                    for(int iCnt = 0; iCnt < pairedDevices.size(); iCnt++){
                        BluetoothDevice device = (BluetoothDevice)pairedDevices.toArray()[iCnt];
                        arrPairedDevicesList[iCnt] = new RowItem(device.getName(),0);
                    }

                    customAdapter = new CustomAdapter(this, arrPairedDevicesList);

                    listView.setAdapter(customAdapter);
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
    }

    /**
     * Method to list the Discovered Bluetooth devices
     */
    private void listDiscoveredBTDevices(){
        IntentFilter filter = new IntentFilter();
        btListAdapter = new ArrayAdapter<>(this, R.layout.simple_row, R.id.simple_row_Txt);

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        btaBR = new SniffBTBroadcastReceiver(this, true);
        registerReceiver(btaBR, filter);

        if(btActions.isDiscovering()) {
            btActions.cancelDiscovery();
        }

        // Start discovery
        mProgressDlg.show();
        btActions.startDiscovery();
    }

    /**
     * Method to update the Discovered BT list
     */
    public void updateDiscoveredList() {
        btListAdapter.clear();
        arrDiscoveredDevicesList = btaBR.getDiscoveredDevicesList();
        for (int iCnt = 0; iCnt < arrDiscoveredDevicesList.size(); iCnt++) {
            btListAdapter.add(arrDiscoveredDevicesList.get(iCnt).getName());
        }

        final ListView lv = (ListView)findViewById(R.id.lstDiscoveredBTDevices);
        lv.setAdapter(btListAdapter);

        showToast("Scan complete");
        mProgressDlg.dismiss();
        unregisterReceiver(btaBR);
    }

}
