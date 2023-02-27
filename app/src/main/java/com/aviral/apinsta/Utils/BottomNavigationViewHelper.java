package com.aviral.apinsta.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.aviral.apinsta.Likes.LikesActivity;
import com.aviral.apinsta.Home.MainActivity;
import com.aviral.apinsta.Profile.ProfileActivity;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Search.SearchActivity;
import com.aviral.apinsta.Share.ShareActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationViewHelper {


    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {

                case R.id.ic_home:
                    Intent intentToMain = new Intent(context, MainActivity.class);
                    context.startActivity(intentToMain);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;

                case R.id.ic_search:
                    Intent intentToSearch = new Intent(context, SearchActivity.class);
                    context.startActivity(intentToSearch);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;

                case R.id.ic_share:
                    Intent intentToShare = new Intent(context, ShareActivity.class);
                    context.startActivity(intentToShare);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;

                case R.id.ic_activity:
                    Intent intentToLikes = new Intent(context, LikesActivity.class);
                    context.startActivity(intentToLikes);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;

                case R.id.ic_profile:
                    Intent intentToProfile = new Intent(context, ProfileActivity.class);
                    context.startActivity(intentToProfile);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;

            }

            return false;
        });
    }
}
