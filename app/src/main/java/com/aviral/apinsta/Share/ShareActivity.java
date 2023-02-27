package com.aviral.apinsta.Share;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.Permissions;
import com.aviral.apinsta.Utils.SectionPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "AviralKaushik";

    private static final int VERIFY_PERMISSION_REQUEST = 1;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);


        setupViewPager();

        if (!checkPermissionArray(Permissions.PERMISSIONS)) {
            verifyPermission(Permissions.PERMISSIONS);
        }
    }

    public int getTask() {
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return  getIntent().getFlags();
    }

    private void setupViewPager() {
        SectionPagerAdapter sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        sectionPagerAdapter.addFragment(new GalleryFragment());
        sectionPagerAdapter.addFragment(new PhotoFragment());

        viewPager = findViewById(R.id.viewpager_container);
        viewPager.setAdapter(sectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(getString(R.string.gallery));
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(R.string.photo);

    }

    public int getCurrentTabNumber() {
        return viewPager.getCurrentItem();
    }

    public void verifyPermission(String[] permissions) {

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions, VERIFY_PERMISSION_REQUEST
        );

    }

    public boolean checkPermissionArray(String[] permissions) {

        for (String check : permissions) {
            if (checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission) {

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Not Granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "Permission Granted for: " + permission);
            return true;
        }

    }
}
