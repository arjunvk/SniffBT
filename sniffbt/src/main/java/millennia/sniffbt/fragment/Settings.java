package millennia.sniffbt.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import millennia.sniffbt.CommonFunctions;
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
        appPrefs = this.getActivity().getPreferences(Context.MODE_PRIVATE);
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
                cf.setSharedPreferences(appPrefs,
                                        getString(R.string.SH_PREF_Scan_Frequency_In_Seconds),
                                        R.integer.one_minute_in_seconds);
            }
        });

        rbOpt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cf.setSharedPreferences(appPrefs,
                                        getString(R.string.SH_PREF_Scan_Frequency_In_Seconds),
                                        R.integer.two_minutes_in_seconds);
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
                        getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class) ==
                        R.integer.one_minute_in_seconds) {
                    rbOpt1.setChecked(true);
                }
                else if((Integer)cf.getSharedPreferences(appPrefs,
                        getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class) ==
                        R.integer.two_minutes_in_seconds) {
                    rbOpt2.setChecked(true);
                }
            }
        }
    }

}
