package arjunvijayakumar.smellbt.customRow;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import arjunvijayakumar.smellbt.R;

public class CustomAdapter extends ArrayAdapter<RowItem> {
    RowItem[] rowItems = null;
    Context context;

    public CustomAdapter(Context context, RowItem[] resource) {
        super(context, R.layout.rowWithCB, resource);
        this.context = context;
        this.rowItems = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.rowWithCB, parent, false);
        TextView name = (TextView)convertView.findViewById(R.id.rowWithCB_Txt);
        CheckBox cb = (CheckBox)convertView.findViewById(R.id.rowWithCB_ChkBox);
        name.setText(rowItems[position].getName());

        if(rowItems[position].getValue() == 1) {
            cb.setChecked(true);
        }
        else {
            cb.setChecked(false);
        }
        return convertView;
    }

}
