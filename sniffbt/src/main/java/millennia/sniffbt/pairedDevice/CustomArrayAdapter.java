package millennia.sniffbt.pairedDevice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import millennia.sniffbt.CommonFunctions;
import millennia.sniffbt.R;
import millennia.sniffbt.SniffBTInterface;

public class CustomArrayAdapter extends ArrayAdapter<Row> {
    Row[] rowItems = null;
    SniffBTInterface pairedDevicesFragment;
    Context context;
    CommonFunctions cf;
    SharedPreferences appPrefs;
    ArrayList<CheckBox> arrCheckBoxes = null;

    public CustomArrayAdapter(SniffBTInterface fragmentActivity, Context context, Row[] resource) {
        super(context, R.layout.row_with_cb, resource);
        this.rowItems = resource;
        this.pairedDevicesFragment = fragmentActivity;
        this.context = context;
        cf = new CommonFunctions();
        appPrefs = getContext().getSharedPreferences(getContext().getString(R.string.app_shared_pref_filename), Context.MODE_PRIVATE);
        arrCheckBoxes = new ArrayList<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row_with_cb, parent, false);
        TextView name = (TextView)convertView.findViewById(R.id.row_with_cb_Txt);
        CheckBox cb = (CheckBox)convertView.findViewById(R.id.row_with_cb_ChkBox);
        name.setText(rowItems[position].getDeviceName());

        cb.setChecked(rowItems[position].isCBChecked());

        if(arrCheckBoxes.size() < rowItems.length) {
            arrCheckBoxes.add(cb);
        }

        // Create a onClickListener when a checkbox is selected
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                rowItems[position].setCB(cb.isChecked());

                // Uncheck any devices of the same category
                if(cb.isChecked()) {
                    for(int iCnt = 0; iCnt < arrCheckBoxes.size(); iCnt++) {
                        if (rowItems[iCnt].isCBChecked() && !rowItems[iCnt].getDeviceAddress().equals(rowItems[position].getDeviceAddress())) {
                            if (rowItems[iCnt].getDeviceMajorClass() == rowItems[position].getDeviceMajorClass()) {
                                arrCheckBoxes.get(iCnt).setChecked(false);
                                rowItems[iCnt].setCB(false);
                                notifyDataSetChanged();
                                cf.showSnackBar(v, rowItems[iCnt].getDeviceName() +
                                                   " belongs to the same category as " +
                                                   rowItems[position].getDeviceName(), Snackbar.LENGTH_SHORT);
                            }
                        }
                    }
                }

                pairedDevicesFragment.pairedDeviceListSettingsChanged();
            }
        });

        return convertView;
    }
}
