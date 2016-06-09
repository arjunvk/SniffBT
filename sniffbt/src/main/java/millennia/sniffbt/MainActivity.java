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

public class MainActivity extends AppCompatActivity{

    // Initialize variables
    final String TAG = "Main Activity";
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
        Log.i(TAG, "Begin render of Main Activity...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define variables
        appPrefs = getPreferences(Context.MODE_PRIVATE);
        final FloatingActionButton mSniffBT = (FloatingActionButton) findViewById(R.id.sniffBT);
        final FloatingActionButton mBTOnOrOff = (FloatingActionButton) findViewById(R.id.btOnOff);

        Log.i(TAG, "Setting up 3 fragments on Main Activity...");
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        
        Log.i(TAG, "Setting up tabs on Main Activity...");
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        //getSupportFragmentManager().getFragments()
        //discFragment = (DiscoveredDevice) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mViewPager.getId(),DISCOVERED_DEVICE_POSITION));

        vTabPair = getLayoutInflater().inflate(R.layout.custom_tab, null);
        vTabSearch = getLayoutInflater().inflate(R.layout.custom_tab, null);
        vTabSettings = getLayoutInflater().inflate(R.layout.custom_tab, null);

        Log.i(TAG, "Setting tab icons ...");
        setupTabIcons(0);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setupTabIcons(tab.getPosition());
                switch (tab.getPosition()) {
                    case PAIRED_DEVICE_POSITION:
                        mSniffBT.show();
                        mBTOnOrOff.hide();
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
                        break;

                    case SETTINGS_POSITION:
                        mSniffBT.hide();
                        mBTOnOrOff.hide();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        mSniffBT.show();

        mSniffBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Obtain the list of Paired Devices settings
                //if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Paired_Devices), Row[].class) != null) {
                if(cf.getSharedPreferences(appPrefs, getString(R.string.SH_PREF_Scan_Frequency_In_Seconds), Integer.class) != null) {
                    arrPairedDevicesList = (Row[]) cf.getSharedPreferences(appPrefs,
                                                   getString(R.string.SH_PREF_Paired_Devices), Row[].class);

                    // Start service to listen to BT
                    intentListenBT = new Intent(getApplicationContext(), ListenBTIntentService.class);
                    Log.i(TAG, "Starting Intent Service...");

                    intentListenBT.putExtra("PairedDevicesList", arrPairedDevicesList);
                    getApplicationContext().startService(intentListenBT);
                }
                else {
                    cf.showSnackBar(v, "Set the frequency of scan", Snackbar.LENGTH_SHORT);
                }
            }
        });

        mBTOnOrOff.hide();

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

        mTabLayout.getTabAt(0).setCustomView(vTabPair);
        mTabLayout.getTabAt(1).setCustomView(vTabSearch);
        mTabLayout.getTabAt(2).setCustomView(vTabSettings);
    }

    private void setupViewPager(ViewPager mViewPager) {
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new PairedDevice(), "");
        mViewPagerAdapter.addFragment(new DiscoveredDevice(), "");
        mViewPagerAdapter.addFragment(new Settings(), "");
        //int temp = mViewPagerAdapter.getItem(1).getId();
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    private String getFragmentTag(int viewPagerId, int fragmentPosition) {
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFragment(Fragment fragment, String title) {
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
}