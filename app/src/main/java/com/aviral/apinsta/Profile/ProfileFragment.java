package com.aviral.apinsta.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Models.Comment;
import com.aviral.apinsta.Models.Like;
import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.Models.UserAccountSettings;
import com.aviral.apinsta.Models.UserSettings;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.BottomNavigationViewHelper;
import com.aviral.apinsta.Utils.FirebaseMethods;
import com.aviral.apinsta.Utils.GridImageAdapter;
import com.aviral.apinsta.Utils.UniversalImageLoader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "AviralKaushik";

    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUMBER = 4;
    private static final int NUMBER_GRID_COLUMN = 3;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;

    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar progressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationView bottomNavigationView;
    private Context context;

    private int mFollowingCount = 0;
    private int mFollowersCount = 0;
    private int mPostsCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_image);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowing = view.findViewById(R.id.tvFollowings);
        mFollowers = view.findViewById(R.id.tvFollowers);
        progressBar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.grid_view);
        toolbar = view.findViewById(R.id.profileToolBar);
        profileMenu = view.findViewById(R.id.profile_settings);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        TextView editProfile = view.findViewById(R.id.tvEditYourProfile);

        context = getActivity();
        firebaseMethods = new FirebaseMethods(context);

        setUpToolBar();
        setUpBottomNavigation();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        setUpFirebase();
        setupGridView();

        editProfile.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AccountSettingsActivity.class);
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {

        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException " + e.getMessage());
        }

        super.onAttach(context);
    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: Setting up Profile Grid View");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.field_user_photos))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {

                    Photo photo = new Photo();

                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    try {
                        photo.setCaption(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_caption))));
                        photo.setDate_created(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_date_created))));
                        photo.setTags(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_tags))));
                        photo.setUser_id(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_user_id))));
                        photo.setPhoto_id(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_photo_id))));
                        photo.setImage_path(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_image_path))));

                        ArrayList<Comment> mComments = new ArrayList<>();

                        for (DataSnapshot dataSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {

                            Comment comment = new Comment();

                            comment.setUser_id(Objects.requireNonNull(
                                    dataSnapshot.getValue(Comment.class)).getUser_id());
                            comment.setComment(Objects.requireNonNull(
                                    dataSnapshot.getValue(Comment.class)).getComment());
                            comment.setDate_created(Objects.requireNonNull(
                                    dataSnapshot.getValue(Comment.class)).getDate_created());

                            mComments.add(comment);
                        }

                        photo.setComments(mComments);

                        List<Like> likeList = new ArrayList<>();

                        for (DataSnapshot dataSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {

                            Like like = new Like();
                            like.setUser_id(Objects.requireNonNull(dataSnapshot.getValue(Like.class)).getUser_id());

                            likeList.add(like);
                        }

                        photo.setLikes(likeList);

                        photos.add(photo);

                    } catch (NullPointerException e) {
                        Log.d(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                        Toast.makeText(context, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                    }
                }

                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUMBER_GRID_COLUMN;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();

                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }

                GridImageAdapter gridImageAdapter = new GridImageAdapter(context,
                        R.layout.layout_grid_image_view,
                        "", imgUrls);

                gridView.setAdapter(gridImageAdapter);

                gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                    Log.d(TAG, "onItemClick: Grid Image Selected: " + photos.get(i));
                    mOnGridImageSelectedListener.onGridImageSelected(photos.get(i), ACTIVITY_NUMBER);
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });

    }

    private void getFollowersCount() {
        Log.d(TAG, "getFollowers: Getting Followers from Firebase");

        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.field_followers))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ignored :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Found Followers from Database:");
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount() {
        Log.d(TAG, "getFollowers: Getting Following from Firebase");

        mFollowingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.field_following))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ignored :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Found Following from Database:");
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount() {
        Log.d(TAG, "getFollowers: Getting Post Count from Firebase");

        mPostsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.field_user_photos))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ignored :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Found Posts from Database:");
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {

        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();

        String imageUrl = userAccountSettings.getProfile_photo();
        UniversalImageLoader.setImage(imageUrl, mProfilePhoto, null, "");

        mDescription.setText(userAccountSettings.getDescription());
        mDisplayName.setText(userAccountSettings.getDisplay_name());
        mUsername.setText(userAccountSettings.getUsername());
        mWebsite.setText(userAccountSettings.getWebsite());
        progressBar.setVisibility(View.GONE);

    }

    private void setUpToolBar() {

        Objects.requireNonNull((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(view -> {
            Log.d(TAG, "setUpToolBar: Navigating to Account Settings Activity");

            Intent intent = new Intent(context, AccountSettingsActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        });
    }

    private void setUpBottomNavigation() {
        BottomNavigationViewHelper.enableNavigation(context, getActivity(), bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }

    private void setUpFirebase() {

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = firebaseDatabase.getReference();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                Log.d("AviralKaushik", "User Logged In:" + user.getUid());
            } else {
                Log.d("AviralKauhsik", "No User");
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setProfileWidgets(firebaseMethods.getUserAccountDetails(snapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
