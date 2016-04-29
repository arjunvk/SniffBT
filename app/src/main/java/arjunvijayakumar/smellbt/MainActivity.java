package arjunvijayakumar.smellbt;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import arjunvijayakumar.smellbt.customRow.CustomAdapter;
import arjunvijayakumar.smellbt.customRow.RowItem;

public class MainActivity extends AppCompatActivity {

    // Initialize variables
    private BTActions btActions = null;

    // Initialize constructor
    public MainActivity(){
        btActions = new BTActions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the custom ActionBar toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Add headers to List view
        //ListView lvBTPairedDevices = (ListView)findViewById(R.id.lstPairedBTDevices);
        //lvBTPairedDevices.addHeaderView(new TextView(this.).setText(getString(R.string.text_list_paired_devices)));

        // Set the method for refreshing list of Paired devices
        final SwipeRefreshLayout refreshPairedDevices = (SwipeRefreshLayout)findViewById(R.id.lstPairedBTDevicesRefresh);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        RowItem[] pairedBTDevices;
        CustomAdapter customAdapter;

        pairedBTDevices = new RowItem[btActions.getPairedDevicesList().size()];

        ListView listView = (ListView)findViewById(R.id.lstPairedBTDevices);

        btActions.turnOnBluetooth();

        // Find the list of paired devices
        Set<BluetoothDevice> pairedDevices = btActions.getPairedDevicesList();

        if(!(pairedDevices == null)){
            if(pairedDevices.size() > 0){
                // Populate the ListView with the paired devices
                try{
                    // Loop through paired devices
                    for(int iCnt = 0; iCnt < pairedDevices.size(); iCnt++){
                        BluetoothDevice device = (BluetoothDevice)pairedDevices.toArray()[iCnt];
                        pairedBTDevices[iCnt] = new RowItem(device.getName(),0);
                    }

                    customAdapter = new CustomAdapter(this, pairedBTDevices);

                    listView.setAdapter(customAdapter);
                }
                catch(NullPointerException npe){
                    System.out.println("List view is null");
                }
            }
        }

        btActions.turnOffBluetooth();
    }
}
