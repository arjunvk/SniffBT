package millennia.sniffbt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import millennia.sniffbt.fragment.DiscoveredDevice;
import millennia.sniffbt.fragment.PairedDevice;
import millennia.sniffbt.fragment.Settings;
import millennia.sniffbt.pairedDevice.Row;

import static millennia.sniffbt.CommonFunctions.isLocationServicesAvailable;

public class MainActivity extends AppCompatActivity{

    // Initialize variables
    final String TAG = "Main Activity";
    ViewPager mViewPager = null;
    final int PAIRED_DEVICE_POSITION = 0;
    final int DISCOVERED_DEVICE_POSITION = 1;
    final int SETTINGS_POSITION = 2;
    private TabLayout mTabLayout;
    private View vTabPair;
    private View vTabSearch;
    private View vTabSettings;
    private Intent intentListenBT;
    private BTActions btActions;
    private CommonFunctions cf;
    private SharedPreferences appPrefs;
    private Row[] arrPairedDevicesList;

    //private PairedDevice pairFragment;
    private DiscoveredDevice discFragment;
    //private Settings settingsFragment;

    public MainActivity() {
        btActions = new BTActions();
        cf = new CommonFunctions();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Begin render of Main Activity...");
        setContentView(R.layout.activity_main);

        // Define variables
        appPrefs = getSharedPreferences(getString(R.string.app_shared_pref_filename), Context.MODE_PRIVATE);
        intentListenBT = new Intent(getApplicationContext(), ListenBTIntentService.class);
        final FloatingActionButton mSniffBT = (FloatingActionButton) findViewById(R.id.sniffBT);
        final FloatingActionButton mBTOnOrOff = (FloatingActionButton) findViewById(R.id.btOnOff);
        final FloatingActionButton mSniffBTTutorial = (FloatingActionButton) findViewById(R.id.sniffBTTutorial);

        Log.i(TAG, "Setting up 3 fragments on Main Activity...");
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        
        Log.i(TAG, "Setting up tabs on Main Activity...");
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        vTabPair = getLayoutInflater().inflate(R.layout.custom_tab, null);
        vTabSearch = getLayoutInflater().inflate(R.layout.custom_tab, null);
        vTabSettings = getLayoutInflater().inflate(R.layout.custom_tab, null);

        Log.i(TAG, "Setting tab icons...");
        int intDefaultTabPosition = 0;

        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_SniffBT_Tutorial_FirstStart), String.class) == null) {
            cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_SniffBT_Tutorial_FirstStart), "true");
        }

        setupTabIcons(intDefaultTabPosition);

        // Setting the Sniff BT icon
        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class) != null) {
            if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("OFF")) {
                mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_off));
            }
            else if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("ON")) {
                mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_on));
            }
        }
        else {
            mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_off));
            cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), "OFF");
        }
        mSniffBTTutorial.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_tutorial));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                setupTabIcons(tab.getPosition());

                switch (tab.getPosition()) {
                    case PAIRED_DEVICE_POSITION:
                        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class) != null) {
                            if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("OFF")) {
                                mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_off));
                            }
                            else if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("ON")) {
                                mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_on));
                            }
                        }
                        else {
                            mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_off));
                        }
                        mSniffBTTutorial.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_tutorial));
                        mSniffBT.show();
                        mBTOnOrOff.hide();
                        mSniffBTTutorial.show();
                        break;

                    case DISCOVERED_DEVICE_POSITION:
                        mSniffBT.hide();
                        if(btActions.isBluetoothTurnedOn()) {
                            mBTOnOrOff.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_bt_on));
                        }
                        else {
                            mBTOnOrOff.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_bt_off));
                        }
                        mBTOnOrOff.show();
                        mSniffBTTutorial.hide();
                        break;

                    case SETTINGS_POSITION:
                        mSniffBT.hide();
                        mBTOnOrOff.hide();
                        mSniffBTTutorial.hide();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        switch (intDefaultTabPosition) {
            case PAIRED_DEVICE_POSITION:
                mSniffBT.show();
                mBTOnOrOff.hide();
                mSniffBTTutorial.show();
                break;

            case DISCOVERED_DEVICE_POSITION:
                mSniffBT.hide();
                mBTOnOrOff.show();
                mSniffBTTutorial.hide();
                break;

            case SETTINGS_POSITION:
                mSniffBT.hide();
                mBTOnOrOff.hide();
                mSniffBTTutorial.hide();
                break;

            default:
                mSniffBT.show();
                mBTOnOrOff.hide();
                mSniffBTTutorial.hide();
        }

        if(btActions.isBluetoothSupported()) {
            mSniffBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), int.class) != null) {
                        if((Integer)cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), int.class) != 0) {
                            if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("OFF")) {
                                // Turn on Sniff BT
                                arrPairedDevicesList = (Row[]) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), Row[].class);
                                cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), "ON");

                                // Start service to listen to BT
                                Log.i(TAG, "Starting Intent Service to start SniffBT scheduler...");
                                intentListenBT.putExtra("PairedDevicesList", cf.serialize(arrPairedDevicesList));
                                getApplicationContext().startService(intentListenBT);
                                mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_on));
                                cf.showSnackBar(v, "SniffBT has been turned ON", Snackbar.LENGTH_SHORT);
                            }
                            else if(((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), String.class)).equalsIgnoreCase("ON")) {
                                // Turn off Sniff BT
                                Log.i(TAG, "Starting Intent Service to stop SniffBT scheduler...");
                                cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_Sniff_BT_OnOff), "OFF");
                                getApplicationContext().startService(intentListenBT);

                                mSniffBT.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_sniff_bt_off));
                                cf.showSnackBar(v, "SniffBT has been turned OFF", Snackbar.LENGTH_SHORT);
                            }
                        }
                        else {
                            cf.showSnackBar(v, "Set the frequency of scan", Snackbar.LENGTH_SHORT);
                        }
                    }
                }
            });

            mBTOnOrOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(btActions.isBluetoothTurnedOn()) {
                        btActions.turnOffBluetooth();
                        mBTOnOrOff.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_bt_off));
                    }
                    else {
                        btActions.turnOnBluetooth();
                        mBTOnOrOff.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_bt_on));
                    }

                    discFragment.refreshFragment(true);
                }
            });
        }

        mSniffBTTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeIntroTutorial(true);
            }
        });

        // Display Intro fragment ,if invoked for the first time
        invokeIntroTutorial(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupTabIcons(int activeTabPosition) {
        switch (activeTabPosition) {
            case PAIRED_DEVICE_POSITION:
                vTabPair.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_pair_light);
                vTabSearch.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_search_dark);
                vTabSettings.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_settings_dark);
                break;

            case DISCOVERED_DEVICE_POSITION:
                vTabPair.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_pair_dark);
                vTabSearch.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_search_light);
                vTabSettings.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_settings_dark);
                break;

            case SETTINGS_POSITION:
                vTabPair.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_pair_dark);
                vTabSearch.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_search_dark);
                vTabSettings.findViewById(R.id.custom_tab_icon).setBackgroundResource(R.drawable.ic_action_settings_light);
                break;
        }

        mTabLayout.getTabAt(PAIRED_DEVICE_POSITION).setCustomView(vTabPair);
        mTabLayout.getTabAt(DISCOVERED_DEVICE_POSITION).setCustomView(vTabSearch);
        mTabLayout.getTabAt(SETTINGS_POSITION).setCustomView(vTabSettings);
    }

    private void setupViewPager(ViewPager mViewPager) {
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new PairedDevice(), "");
        mViewPagerAdapter.addFragment(new DiscoveredDevice(), "");
        mViewPagerAdapter.addFragment(new Settings(), "");
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case PAIRED_DEVICE_POSITION:
                    //pairFragment = (PairedDevice) createdFragment;
                    break;

                case DISCOVERED_DEVICE_POSITION:
                    discFragment = (DiscoveredDevice) createdFragment;
                    break;

                case SETTINGS_POSITION:
                    //settingsFragment = (Settings) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }

    private void invokeIntroTutorial(boolean blnIsInvokedFromPairTab) {
        boolean isFirstStart = false;

        if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_SniffBT_Tutorial_FirstStart), String.class) != null) {
            isFirstStart = ((String) cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_SniffBT_Tutorial_FirstStart), String.class)).equalsIgnoreCase("true");
        }

        //  If the activity has never started before or if Location services are not enabled
        if (isFirstStart || blnIsInvokedFromPairTab || !isLocationServicesAvailable(getApplicationContext())) {
            // Turn off Bluetooth for Intro tutorial
            btActions.turnOffBluetooth();

            // Launch app intro
            Intent i = new Intent(MainActivity.this, IntroTutorialActivity.class);
            startActivity(i);

            //  Edit preference to make it false because we don't want this to run again
            cf.setSharedPreferences(appPrefs, getString(R.string.SH_PREF_SniffBT_Tutorial_FirstStart), "false");
        }
    }
}