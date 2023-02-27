package com.aviral.apinsta.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.Models.User;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.ViewCommentsFragment;
import com.aviral.apinsta.Utils.ViewPostFragment;
import com.aviral.apinsta.Utils.ViewProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectionListener,
        ViewProfileFragment.OnGridImageSelectedListener{


    private static final String TAG = "AviralKauhsik";

    @Override
    public void onCommentThreadSelectionListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectionListener: Selected a Common Thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_profile, fragment);
        transaction.addToBackStack(getString(R.string.view_comment_fragment));
        transaction.commit();

    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: Selected An Image From profile grid view: " + photo.toString());

        ViewPostFragment viewPostFragment = new ViewPostFragment();

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);

        viewPostFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_profile, viewPostFragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

    }

    private void init() {

        Log.d(TAG, "init: Intialising Profile Activity");

        Intent intent = getIntent();

        if(intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: Searching for user object in intent extra");

            if (intent.hasExtra(getString(R.string.intent_user))) {

                User user = intent.getParcelableExtra(getString(R.string.intent_user));

                if (!user.getUser_id().equals(Objects.requireNonNull(
                        FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                    Log.d(TAG, "init: Inflating View Profile");

                    ViewProfileFragment fragment = new ViewProfileFragment();

                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container_profile, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                } else {
                    Log.d(TAG, "init: Inflating Profile");

                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.container_profile, profileFragment);
                    transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "init: Inflating Profile");

            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.container_profile, profileFragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }

}
