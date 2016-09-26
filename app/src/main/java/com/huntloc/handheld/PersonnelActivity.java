package com.huntloc.handheld;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class PersonnelActivity extends AppCompatActivity implements JournalFragment.OnJournalFragmentInteractionListener, ClearanceFragment.OnClearanceFragmentInteractionListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    String response;

    @Override
    public void onClearanceFragmentInteraction() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        response = intent.getStringExtra(HandheldFragment.PERSONNEL_MESSAGE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_personnel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJournalFragmentInteraction() {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private JournalFragment journalFragment;
        private ClearanceFragment clearanceFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (journalFragment == null) {
                    journalFragment = JournalFragment.newInstance(response);
                }
                fragment = journalFragment;
            } else if (position == 1) {
                if (clearanceFragment == null) {
                    clearanceFragment = new ClearanceFragment();
                    Bundle args = new Bundle();
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);
                    } catch (JSONException e) {
                    }
                    args.putString(ClearanceFragment.ARG_CREDENTIALID, jsonResponse.optString("CardID"));
                    clearanceFragment.setArguments(args);
                }
                fragment = clearanceFragment;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Personnel";
                case 1:
                    return "Clearance";

            }
            return null;
        }
    }
}
