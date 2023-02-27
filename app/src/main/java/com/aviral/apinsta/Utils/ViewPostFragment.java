package com.aviral.apinsta.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Models.Comment;
import com.aviral.apinsta.Models.Like;
import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.Models.User;
import com.aviral.apinsta.Models.UserAccountSettings;
import com.aviral.apinsta.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "AviralKauhsik";

    public interface OnCommentThreadSelectionListener {
        void onCommentThreadSelectionListener(Photo photo);
    }
    OnCommentThreadSelectionListener mOnCommentThreadSelectionListener;

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    private SquareImageView postImageView;
    private BottomNavigationView bottomNavigationView;
    private TextView mCaption, mUsername, mTimestamp, mLikes, mComments;
    private ImageView mBackArrow, mHeartRed, mHeartWhite, mProfileImage, mComment;

    private Photo mPhoto;
    private int activityNumber = 0;
    private UserAccountSettings userAccountSettings;
    private GestureDetector gestureDetector;
    private Heart mHeart;
    private Boolean mLikedByTheCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";
    private User mCurrentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        postImageView = view.findViewById(R.id.post_image);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);

        mBackArrow = view.findViewById(R.id.imageBackArrow);
        mCaption = view.findViewById(R.id.image_caption);
        mUsername = view.findViewById(R.id.username);
        mTimestamp = view.findViewById(R.id.image_time_posted);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mHeartWhite = view.findViewById(R.id.image_heart);
        mProfileImage = view.findViewById(R.id.profile_photo);
        mLikes = view.findViewById(R.id.image_likes);
        mComment = view.findViewById(R.id.ic_bubble_speech);
        mComments = view.findViewById(R.id.image_comments_link);

        mHeart = new Heart(mHeartWhite, mHeartRed);

        gestureDetector = new GestureDetector(getActivity(), new GestureListener());

        setUpFirebase();
        setUpBottomNavigation();

        return view;

    }

    private void init() {
        try{
            //mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(Objects.requireNonNull(
                    getPhotoFromBundle()).getImage_path(),
                    postImageView,
                    null,
                    "");

            activityNumber = getActivityNumberFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.field_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        newPhoto.setCaption(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_caption))));
                        newPhoto.setDate_created(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_date_created))));
                        newPhoto.setTags(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_tags))));
                        newPhoto.setUser_id(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_user_id))));
                        newPhoto.setPhoto_id(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_photo_id))));
                        newPhoto.setImage_path(String.valueOf(Objects.requireNonNull(objectMap)
                                .get(getString(R.string.field_image_path))));


                        List<Comment> commentsList = new ArrayList<>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();

                            comment.setUser_id(Objects.requireNonNull(
                                    dSnapshot.getValue(Comment.class)).getUser_id());
                            comment.setComment(Objects.requireNonNull(
                                    dSnapshot.getValue(Comment.class)).getComment());
                            comment.setDate_created(Objects.requireNonNull(
                                    dSnapshot.getValue(Comment.class)).getDate_created());

                            commentsList.add(comment);
                        }
                        newPhoto.setComments(commentsList);

                        mPhoto = newPhoto;

                        getCurrentUser();
                        getPhotoDetails();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled.");
                }
            });

        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage() );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            init();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mOnCommentThreadSelectionListener = (OnCommentThreadSelectionListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: classCastException " + e.getMessage());
        }

    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: Getting Likes String");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.field_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUsers = new StringBuilder();

                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                    Query query = reference
                            .child(getString(R.string.user))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(Objects.requireNonNull(singleSnapshot.getValue(Like.class)).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: Likes Found: " +
                                        Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());

                                mUsers.append(Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");


                            mLikedByTheCurrentUser = String.valueOf(mUsers).contains(mCurrentUser.getUsername() + ",");

                            int length = splitUsers.length;

                            if (length == 1) {
                                mLikesString = "Liked by: " + splitUsers[0];
                            } else if (length == 2) {
                                mLikesString = "Liked by: " + splitUsers[0]
                                        + "and " + splitUsers[1];
                            } else if (length == 3) {
                                mLikesString = "Liked by: " + splitUsers[0]
                                        + " , " + splitUsers[1]
                                        + " and " + splitUsers[2];
                            } else if (length == 4) {
                                mLikesString = "Liked by: " + splitUsers[0]
                                        + " , " + splitUsers[1]
                                        + " , " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            } else if (length > 4) {
                                mLikesString = "Liked by: " + splitUsers[0]
                                        + " , " + splitUsers[1]
                                        + " , " + splitUsers[2]
                                        + " , " + splitUsers[3]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }

                            setupWidgets();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                if (!snapshot.exists()) {
                    mLikesString = "";
                    mLikedByTheCurrentUser = false;
                    setupWidgets();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.user))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {

                    mCurrentUser = singleSnapshot.getValue(User.class);

                }
                getLikesString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference
                    .child(getString(R.string.field_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        // Photo is already liked by the user => Dislike

                        String keyID = singleSnapshot.getKey();

                        if (mLikedByTheCurrentUser &&
                            Objects.requireNonNull(singleSnapshot.getValue(Like.class)).getUser_id()
                            .equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {

                            myRef.child(getString(R.string.field_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(Objects.requireNonNull(keyID))
                                    .removeValue();

                            myRef.child(getString(R.string.field_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(Objects.requireNonNull(keyID))
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();

                        }
                        // Photo is not liked by the user => Like
                        else if (!mLikedByTheCurrentUser) {
                            // add new like
                            addNewLike();
                            break;
                        }
                    }

                    if (!snapshot.exists()) {
                        // add new like
                        addNewLike();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            return true;
        }
    }

    private void addNewLike() {
        Log.d(TAG, "addNewLike: Adding New Like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        myRef.child(getString(R.string.field_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(Objects.requireNonNull(newLikeID))
                .setValue(like);

        myRef.child(getString(R.string.field_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(Objects.requireNonNull(newLikeID))
                .setValue(like);

        mHeart.toggleLike();

    }

    private void getPhotoDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    Log.d(TAG, "onDataChange: Date Accessed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupWidgets() {
        String timeStampDifference = getTimeStampDifference();

        if (!timeStampDifference.equals("0")) {
            String date = timeStampDifference + getString(R.string.days_ago);
            mTimestamp.setText(date);
        } else {
            mTimestamp.setText(getString(R.string.today));
        }

        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(userAccountSettings.getUsername());
        mLikes.setText(mLikesString);

        mCaption.setText(mPhoto.getCaption());

        if (mPhoto.getComments().size() > 0) {
            String commentsText = "view all " + mPhoto.getComments().size() + " comments";
            mComments.setText(commentsText);
        } else {
            mComments.setText("");
        }

        mComments.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: Navigating to on comments thread");

            mOnCommentThreadSelectionListener.onCommentThreadSelectionListener(mPhoto);

        });

        mBackArrow.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: Navigating back");
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        mComment.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: Navigating To Comments");
            mOnCommentThreadSelectionListener.onCommentThreadSelectionListener(mPhoto);
        });


        if (mLikedByTheCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);

            mHeartRed.setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        } else {
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setVisibility(View.VISIBLE);

            mHeartWhite.setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        }


    }


    private String getTimeStampDifference() {
        Log.d(TAG, "getTimeStampDifference: Getting Time Stamp Difference");

        String difference;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        Date today = c.getTime();
        sdf.format(today);

        Date timeStamp;
        final String photoTimeStamp = mPhoto.getDate_created();

        try {

            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(((today.getTime() - Objects.requireNonNull(timeStamp).getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }

    private int getActivityNumberFromBundle() {
        Log.d(TAG, "getActivityNumberFromBundle: Getting Activity Number from Bundle");
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: Arguments: " + getArguments());

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    private void setUpBottomNavigation() {
        BottomNavigationViewHelper.enableNavigation(requireActivity(), getActivity(), bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(activityNumber);
        menuItem.setChecked(true);
    }

    private void setUpFirebase() {

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                Log.d("AviralKaushik", "User Logged In:" + user.getUid());
            } else {
                Log.d("AviralKauhsik", "No User");
            }
        };
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
