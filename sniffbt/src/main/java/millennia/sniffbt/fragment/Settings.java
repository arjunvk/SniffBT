package millennia.sniffbt.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import millennia.sniffbt.CommonFunctions;
import millennia.sniffbt.ListenBTIntentService;
import millennia.sniffbt.R;

public class Settings extends Fragment {
    final String TAG = "Settings Fragment";
    private SharedPreferences appPrefs;
    private CommonFunctions cf;

    // UI Objects
    private TextView tvFreqOfScan;
    private RadioButton rbOpt1, rbOpt2;

    public Settings() {
        cf = new CommonFunctions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Begin render of Discovered Device fragment...");
        super.onCreate(savedInstanceState);

        // Define variables
        appPrefs = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.app_shared_pref_filename), Context.MODE_PRIVATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Define UI Objects
        defineUIObjects();

        // Set the Font
        tvFreqOfScan.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_path)));
        rbOpt1.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_path)));
        rbOpt2.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_path)));

        // Set the listeners
        rbOpt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), 60);

                if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("ON")) {
                    cf.showSnackBar(v, "SniffBT has been turned OFF", Snackbar.LENGTH_SHORT);
                    turnOffSniffBT();
                }
            }
        });

        rbOpt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), 120);

                if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("ON")) {
                    cf.showSnackBar(v, "SniffBT has been turned OFF", Snackbar.LENGTH_SHORT);
                    turnOffSniffBT();
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refreshFragment(isVisibleToUser);
    }

    private void defineUIObjects() {
        tvFreqOfScan = (TextView) getView().findViewById(R.id.tvFreqOfScan);
        rbOpt1 = (RadioButton) getView().findViewById(R.id.rb_opt1);
        rbOpt2 = (RadioButton) getView().findViewById(R.id.rb_opt2);
    }

    public void refreshFragment(boolean isVisibleToUser) {
        // Set the radio buttons according to the user setting
        if(isVisibleToUser) {
            if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class) != null) {
                if((Integer)cf.getSharedPreferences(appPrefs,
                    getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class) == 60) {
                    rbOpt1.setChecked(true);
                }
                else if((Integer)cf.getSharedPreferences(appPrefs,
                         getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class) == 120) {
                    rbOpt2.setChecked(true);
                }
            }
        }
    }

    private void turnOffSniffBT() {
        final FloatingActionButton mSniffBT = (FloatingActionButton) getView().findViewById(R.id.sniffBT);
        Intent intentListenBT = new Intent(getContext(), ListenBTIntentService.class);

        // Turn off Sniff BT
        Log.i(TAG, "Starting Intent Service to stop SniffBT scheduler...");
        getContext().startService(intentListenBT);
    }

}
