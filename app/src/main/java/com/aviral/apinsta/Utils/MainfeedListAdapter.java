package com.aviral.apinsta.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aviral.apinsta.Home.MainActivity;
import com.aviral.apinsta.Models.Comment;
import com.aviral.apinsta.Models.Like;
import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.Models.User;
import com.aviral.apinsta.Models.UserAccountSettings;
import com.aviral.apinsta.Profile.ProfileActivity;
import com.aviral.apinsta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener {
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "AviralKaushik";

    private final LayoutInflater mInflater;
    private final int mLayoutResource;
    private final Context mContext;
    private final DatabaseReference mReference;
    private String currentUsername = "";

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        String likeString;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();

        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {

            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.username);
            holder.image =  convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.image_heart);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.caption =  convertView.findViewById(R.id.image_caption);
            holder.timeDelta =  convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage =  convertView.findViewById(R.id.profile_photo);

            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //get the current users username (need for checking likes string)
        getCurrentUsername();

        //get likes string
        getLikesString(holder);

        holder.caption.setText(getItem(position).getCaption());

        List<Comment> comments = getItem(position).getComments();
        String commentString = "View all " + comments.size() + " comments";
        holder.comments.setText(commentString);
        holder.comments.setOnClickListener(view -> {
            Log.d(TAG, "getView: Loading Comment Thread: " + getItem(position).getPhoto_id());

            ((MainActivity) mContext).onCommentThreadSelected(getItem(position),
                    mContext.getString(R.string.main_activity));

            ((MainActivity) mContext).hideLayout();
        });

        String timeStampDifference = getTimeStampDifference(getItem(position));

        String timeString;
        if(!timeStampDifference.equals("0")){
            timeString = timeStampDifference + " DAYS AGO";
        }else{
            timeString = "TODAY";
        }
        holder.timeDelta.setText(timeString);

        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(mContext.getString(R.string.user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: User Found:"
                            + Objects.requireNonNull(
                                    singleSnapshot.getValue(UserAccountSettings.class))
                            .getUser_id());

                    holder.username.setText(Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class)).getUsername());
                    holder.username.setOnClickListener(view -> {
                        Log.d(TAG, "onDataChange: Navigating to User Profile of: " + holder.user.getUsername());

                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra(mContext.getString(R.string.calling_activity),
                                mContext.getString(R.string.main_activity));
                        intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                        mContext.startActivity(intent);

                    });

                    imageLoader.displayImage(Objects.requireNonNull(
                            singleSnapshot.getValue(UserAccountSettings.class)).getProfile_photo(),
                            holder.mProfileImage);

                    holder.mProfileImage.setOnClickListener(view -> {
                        Log.d(TAG, "onClick: navigating to profile of: " +
                                holder.user.getUsername());

                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra(mContext.getString(R.string.calling_activity),
                                mContext.getString(R.string.main_activity));
                        intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                        mContext.startActivity(intent);
                    });

                    holder.userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);

                    holder.comment.setOnClickListener(view -> {
                        ((MainActivity) mContext).onCommentThreadSelected(getItem(position),
                                mContext.getString(R.string.main_activity));

                        ((MainActivity) mContext).hideLayout();
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // get the user object

        Query userQuery = mReference.child(mContext.getString(R.string.user))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (reachedEndOfList(position)) {
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == getCount() - 1;
    }

    private void loadMoreData() {

        try {
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        } catch (ClassCastException e) {
            Log.d(TAG, "loadMoreData: ClassCastException: " + e.getMessage());
        }

        try {
            mOnLoadMoreItemsListener.onLoadMoreItems();
        } catch (NullPointerException e) {
            Log.d(TAG, "loadMoreData: NullPointerException: " + e.getMessage());
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.field_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if(mHolder.likeByCurrentUser &&
                                Objects.requireNonNull(singleSnapshot.getValue(Like.class)).getUser_id()
                                        .equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){

                            assert keyID != null;
                            mReference.child(mContext.getString(R.string.field_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mReference.child(mContext.getString(R.string.field_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();

                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: Getting User Account Settings from firebase");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.user))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(Objects.requireNonNull(
                        FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    currentUsername = Objects.requireNonNull(
                            singleSnapshot.getValue(UserAccountSettings.class))
                            .getUsername();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNewLike(final ViewHolder holder) {
        Log.d(TAG, "addNewLike: Adding New Like");

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        mReference.child(mContext.getString(R.string.field_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(Objects.requireNonNull(newLikeID))
                .setValue(like);

        mReference.child(mContext.getString(R.string.field_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(Objects.requireNonNull(newLikeID))
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);

    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: Getting Likes String");

        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference
                    .child(mContext.getString(R.string.field_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    holder.users = new StringBuilder();

                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                        Query query = reference
                                .child(mContext.getString(R.string.user))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(Objects.requireNonNull(singleSnapshot.getValue(Like.class)).getUser_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: Likes Found: " +
                                            Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());

                                    holder.users.append(Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");


                                holder.likeByCurrentUser = String.valueOf(holder.users).contains(currentUsername + ",");

                                int length = splitUsers.length;

                                if (length == 1) {
                                    holder.mLikesString = "Liked by: " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.mLikesString = "Liked by: " + splitUsers[0]
                                            + "and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.mLikesString = "Liked by: " + splitUsers[0]
                                            + " , " + splitUsers[1]
                                            + " and " + splitUsers[2];
                                } else if (length == 4) {
                                    holder.mLikesString = "Liked by: " + splitUsers[0]
                                            + " , " + splitUsers[1]
                                            + " , " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    holder.mLikesString = "Liked by: " + splitUsers[0]
                                            + " , " + splitUsers[1]
                                            + " , " + splitUsers[2]
                                            + " , " + splitUsers[3]
                                            + " and " + (splitUsers.length - 3) + " others";
                                }

                                Log.d(TAG, "onDataChange: like string: " + holder.mLikesString);

                                setupLikesString(holder, holder.mLikesString);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    if (!snapshot.exists()) {
                        holder.mLikesString = "";
                        holder.likeByCurrentUser = false;

                        setupLikesString(holder, holder.likeString);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "getLikesString: NullPointerException: " + e.getMessage() );
            holder.likeString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, holder.likeString);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLikesString(final ViewHolder holder, String likesString) {
        Log.d(TAG, "setupLikesString: Like String: " + likesString);

        if (holder.likeByCurrentUser) {
            Log.d(TAG, "setupLikesString: photo is liked by current user");

            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartWhite.setVisibility(View.GONE);

            holder.heartRed.setOnTouchListener((view, motionEvent) -> holder.detector.onTouchEvent(motionEvent));

        } else {
            Log.d(TAG, "setupLikesString:  photo is not liked by current user");

            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);

            holder.heartWhite.setOnTouchListener(((view, motionEvent) -> holder.detector.onTouchEvent(motionEvent)));

        }
        holder.likes.setText(likesString);

    }

    private String getTimeStampDifference(Photo photo) {
        Log.d(TAG, "getTimeStampDifference: Getting Time Stamp Difference");

        String difference;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        Date today = c.getTime();
        sdf.format(today);

        Date timeStamp;
        final String photoTimeStamp = photo.getDate_created();

        try {

            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(((today.getTime() - Objects.requireNonNull(timeStamp).getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }


}
