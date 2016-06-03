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
    SniffBTInterface pairedDevicesFragment;
    Context context;

    public CustomAdapter(SniffBTInterface fragmentActivity, Context context, Row[] resource) {
        super(context, R.layout.row_with_cb, resource);
        this.rowItems = resource;
        this.pairedDevicesFragment = fragmentActivity;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
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
                pairedDevicesFragment.pairedDeviceListSettingsChanged();
            }
        });

        return convertView;
    }
}
