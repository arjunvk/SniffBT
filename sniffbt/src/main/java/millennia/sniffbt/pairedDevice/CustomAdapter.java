package millennia.sniffbt.pairedDevice;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import millennia.sniffbt.R;
import millennia.sniffbt.SniffBTInterface;

public class CustomAdapter extends ArrayAdapter<Row> {
    Row[] rowItems = null;
    SniffBTInterface activity;

    public CustomAdapter(SniffBTInterface activity, Row[] resource) {
        super((Context) activity, R.layout.row_with_cb, resource);
        this.rowItems = resource;
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)activity).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row_with_cb, parent, false);
        TextView name = (TextView)convertView.findViewById(R.id.row_with_cb_Txt);
        CheckBox cb = (CheckBox)convertView.findViewById(R.id.row_with_cb_ChkBox);
        name.setText(rowItems[position].getDeviceName());

        cb.setChecked(rowItems[position].isCBChecked());

        // Create a onClickListener when a checkbox is selected
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                rowItems[position].setCB(cb.isChecked());
                activity.pairedDeviceListSettingsChanged();
            }
        });

        return convertView;
    }
}
