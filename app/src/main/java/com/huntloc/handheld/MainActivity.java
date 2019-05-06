package com.huntloc.handheld;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        HandheldFragment.OnHandheldFragmentInteractionListener,
        EntranceFragment.OnEntranceFragmentInteractionListener,
        ExitFragment.OnExitFragmentInteractionListener {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String EXTRA_MESSAGE = "com.huntloc.handheld.MESSAGE";
    private static long back_pressed;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private NfcAdapter mNfcAdapter;
    public static final String PREFS_NAME = "HandheldPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC.", Toast.LENGTH_LONG)
                    .show();
        }
        handleIntent(getIntent());
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        /**Puertas por defecto*/
        if (!settings.contains("door_id")) {
            editor.putString("door_id", "Sliding Gate");
        }
        if (!settings.contains("area_id")) {
            editor.putString("area_id", "Process");
        }
        if (!settings.contains("logEntry_id")) {
            editor.putString("logEntry_id", "EntrySlidingGate");
        }
        if (!settings.contains("logExit_id")) {
            editor.putString("logExit_id", "ExitSlidingGate");
        }
        /***/

        if (!settings.contains("descLogEntry_id")) {
            editor.putString("descLogEntry_id", "Entrance");
        }
        if (!settings.contains("descLogExit_id")) {
            editor.putString("descLogExit_id", "Exit");
        }
        editor.commit();


    }

    @Override
    public boolean onNavigateUpFromChild(Activity child) {
        setCredentialId("");
        return super.onNavigateUpFromChild(child);
    }

    private void setCredentialId(String id) {
        ((HandheldFragment) mSectionsPagerAdapter.getItem(0))
                .setCredentialId(id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(),
                activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                activity.getApplicationContext(), 0, intent, 0);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

        /*9filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }*/

       /*filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        techList = new String[][]{new String[]{NfcA.class.getName()}, new String[]{MifareClassic.class.getName()}, new String[]{NdefFormatable.class.getName()}};*/

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable parcelable = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tag = (Tag) parcelable;
            byte[] id = tag.getId();
            String code = getDec(id) + "";

            HandheldFragment handheldFragment = ((HandheldFragment) mSectionsPagerAdapter.getItem(0));
            if (handheldFragment != null) {
                handheldFragment.setCredentialId(code);
            }
        }
        /*if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                            NdefMessage ndefMessage = null;
                Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if ((rawMessages != null) && (rawMessages.length > 0)) {
                    ndefMessage = (NdefMessage) rawMessages[0];
                    String result = "";
                    byte[] payload = ndefMessage.getRecords()[0].getPayload();
                    String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                    int languageCodeLength = payload[0] & 0077;
                    //String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                    String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                    Log.d("Internal Code", text);
                    HandheldFragment handheldFragment = ((HandheldFragment) mSectionsPagerAdapter.getItem(0));
                    if (handheldFragment != null) {
                        handheldFragment.setCredentialId(text);
                    }
                }
        }*/

      /*if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Parcelable parcelable = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tag = (Tag) parcelable;
            byte[] id = tag.getId();
            String code = getDec(id) + "";
            Log.d("Internal Code", code);
            HandheldFragment handheldFragment = ((HandheldFragment) mSectionsPagerAdapter.getItem(0));
            if (handheldFragment != null) {
                handheldFragment.setCredentialId(code);
            }
        }*/
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        switch (item.getItemId()) {

		/*
		 * case R.id.action_list: listRecords(); return true;
		 */

            case R.id.door_sliding:
                editor.putString("door_id", "Sliding Gate");
                editor.putString("area_id", "Process");
                editor.putString("logEntry_id", "EntrySlidingGate");
                editor.putString("logExit_id", "ExitSlidingGate");
                editor.commit();
                break;
            case R.id.door_north:
                editor.putString("door_id", "North Entrance");
                editor.putString("area_id", "Process");
                editor.putString("logEntry_id", "EntryNorthEntrance");
                editor.putString("logExit_id", "ExitNorthEntrance");
                editor.commit();
                break;
            case R.id.door_south:
                editor.putString("door_id", "South Entrance");
                editor.putString("area_id", "Process");
                editor.putString("logEntry_id", "EntrySouthEntrance");
                editor.putString("logExit_id", "ExitSouthEntrance");
                editor.commit();
                break;
            case R.id.door_main:
                editor.putString("door_id", "Main Gate");
                editor.putString("area_id", "Plant");
                editor.putString("logEntry_id", "EntryMainGate");
                editor.putString("logExit_id", "ExitMainGate");
                editor.commit();
                break;
            case R.id.door_tlf:
                editor.putString("door_id", "TLF");
                editor.putString("area_id", "Tlf");
                editor.putString("logEntry_id", "EntryTlf");
                editor.putString("logExit_id", "ExitTlf");
                editor.commit();
                break;
            /*case R.id.door_temp:
                editor.putString("door_id", "Door 1");
                editor.putString("area_id", "Temp");
                editor.putString("logEntry_id", "EntryTemp");
                editor.putString("logExit_id", "ExitTemp");
                editor.commit();
                break;*/
            default:
                break;
        }
        try {
            ((TextView) findViewById(R.id.textView_DoorId))
                    .setText(getSharedPreferences(PREFS_NAME, 0).getString(
                            "door_id", "Main Gate"));
        } catch (Exception e) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    @Override
    public void onHandheldFragmentInteraction() {

    }

    @Override
    public void onEntranceFragmentInteraction() {

    }

    @Override
    public void onExitFragmentInteraction() {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private HandheldFragment handheldFragment;
        private EntranceFragment entranceFragment;
        private ExitFragment exitFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (handheldFragment == null) {
                    handheldFragment = new HandheldFragment();
                }
                fragment = handheldFragment;
            } else if (position == 1) {
                if (entranceFragment == null) {
                    entranceFragment = new EntranceFragment();
                }
                fragment = entranceFragment;
            } else if (position == 2) {
                if (exitFragment == null) {
                    exitFragment = new ExitFragment();
                }
                fragment = exitFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Handheld";
                case 1:
                    return "Entrance";
                case 2:
                    return "Exit";
            }
            return null;
        }
    }
}
