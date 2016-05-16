package millennia.sniffbt.customRowWithCB;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import millennia.sniffbt.R;

public class CustomAdapter extends ArrayAdapter<RowItem> {
    RowItem[] rowItems = null;
    Context context;

    public CustomAdapter(Context context, RowItem[] resource) {
        super(context, R.layout.row_with_cb, resource);
        this.context = context;
        this.rowItems = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row_with_cb, parent, false);
        TextView name = (TextView)convertView.findViewById(R.id.row_with_cb_Txt);
        CheckBox cb = (CheckBox)convertView.findViewById(R.id.row_with_cb_ChkBox);
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
