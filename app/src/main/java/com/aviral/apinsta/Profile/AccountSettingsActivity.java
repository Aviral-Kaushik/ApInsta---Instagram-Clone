package com.aviral.apinsta.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.BottomNavigationViewHelper;
import com.aviral.apinsta.Utils.FirebaseMethods;
import com.aviral.apinsta.Utils.SectionStatePagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AviralKaushik";

    public SectionStatePagerAdapter sectionStatePagerAdapter;
    private ViewPager viewPager;
    private RelativeLayout relativeLayout;
    private static final int ACTIVITY_NUMBER = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        viewPager = findViewById(R.id.viewpager_container);
        relativeLayout = findViewById(R.id.relLayout1);

        setUpSettingsList();
        setUpFragments();
        setUpBottomNavigation();
        getIncomingIntent();

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view -> finish());

    }

    private void getIncomingIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))) {
            Log.d(TAG, "getIncomingIntent: New Incoming ImageUri");

            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {


                if (intent.hasExtra(getString(R.string.selected_image))) {

                    FirebaseMethods firebaseMethods = new FirebaseMethods(this);

                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),
                            null,
                            0,
                            intent.getStringExtra(getString(R.string.selected_image)),
                            null);

                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {

                    FirebaseMethods firebaseMethods = new FirebaseMethods(this);

                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),
                            null,
                            0,
                            null,
                             intent.getParcelableExtra(getString(R.string.selected_bitmap)));

                }

            }

        }


        if (intent.hasExtra(getString(R.string.calling_activity))) {
            setUpViewPager(sectionStatePagerAdapter.getFragmentNumber(getString(R.string.edit_profile)));
        }
    }

    private void setUpFragments() {

        sectionStatePagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        sectionStatePagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile));
        sectionStatePagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out));

    }

    public void setUpViewPager(int fragmentNumber) {
        relativeLayout.setVisibility(View.GONE);
        viewPager.setAdapter(sectionStatePagerAdapter);
        viewPager.setCurrentItem(fragmentNumber);
    }

    private void setUpSettingsList() {
        ListView listView = findViewById(R.id.lvAccountSettings);

        ArrayList<String> settingsOptions = new ArrayList<>();
        settingsOptions.add(getString(R.string.edit_profile));
        settingsOptions.add(getString(R.string.sign_out));

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, settingsOptions);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, i, l) -> setUpViewPager(i));
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }


}
