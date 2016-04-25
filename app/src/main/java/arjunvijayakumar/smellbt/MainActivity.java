package arjunvijayakumar.smellbt;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listPairedBTDevices();
    }

    private BTActions btActions = null;

    public MainActivity(){
        btActions = new BTActions();
    }

    /**
     * Method to list the already paired Bluetooth devices
     */
    private void listPairedBTDevices(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.activity_main, R.id.txtBTList);
        ListView listView = (ListView)findViewById(R.id.lstPairedBTDevices);

        btActions.turnOnBluetooth();

        // Find the list of paired devices
        Set<BluetoothDevice> pairedDevices = btActions.getPairedDevicesList();

        if(!(pairedDevices == null)){
            if(pairedDevices.size() > 0){
                // Populate the ListView with the paired devices
                try{
                    // Loop through paired devices
                    for(BluetoothDevice device : pairedDevices){
                        adapter.add(device.getName() + "\n" + device.getAddress());
                    }

                    listView.setAdapter(adapter);
                }
                catch(NullPointerException npe){
                    System.out.println("List view is null");
                }
            }
        }

        btActions.turnOffBluetooth();
    }
}
