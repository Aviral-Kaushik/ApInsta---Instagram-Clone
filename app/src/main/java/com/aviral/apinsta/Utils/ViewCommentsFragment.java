package com.aviral.apinsta.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Home.MainActivity;
import com.aviral.apinsta.Models.Comment;
import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment {

    private static final String TAG = "AviralKauhsik";

    public ViewCommentsFragment() {
        super();
        setArguments(new Bundle());
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);

        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment_comment);
        mListView = view.findViewById(R.id.listView);

        mContext = getActivity();

        mComments = new ArrayList<>();

        try {
            Log.d(TAG, "onCreateView: Getting Image From Bundle");
            mPhoto = getPhotoFromBundle();
            Log.d(TAG, "onCreateView: ");

        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointerException: Photo was null from Bundle From Profile Activity" + e.getMessage());
        }

        setUpFirebase();
        setupWidgets();


        return view;

    }

    private void setupWidgets() {

        CommentListAdapter commentListAdapter = new CommentListAdapter(mContext,
                R.layout.layout_comment,
                mComments);

        mListView.setAdapter(commentListAdapter);

        mCheckMark.setOnClickListener(view1 -> {

            if (!mComment.getText().toString().equals("")) {
                Log.d(TAG, "onCreateView: Attempting To Submit New Comment");

                addNewComment(String.valueOf(mComment.getText()));

                mComment.setText("");
                closeKeyboard();

            } else {
                Toast.makeText(mContext, "Cannot Post an Empty Comment", Toast.LENGTH_SHORT).show();
            }
        });

        mBackArrow.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: Navigating back to View Post Fragment");

            if (Objects.requireNonNull(getCallingActivityFromBundle()).equals(getString(R.string.main_activity))) {
                requireActivity().getSupportFragmentManager().popBackStack();

                ((MainActivity) mContext).showLayout();
            } else {
                requireActivity().getSupportFragmentManager().popBackStack();
            }


        });

    }

    private void closeKeyboard() {
        View view = requireActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: Adding New Comment: " + newComment);

        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setUser_id(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        comment.setDate_created(getTimeStamp());

        // Insert into Photos Node
        myRef.child(getString(R.string.field_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(Objects.requireNonNull(commentID))
                .setValue(comment);

        // Insert into User Photos Node
        myRef.child(getString(R.string.field_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(Objects.requireNonNull(commentID))
                .setValue(comment);

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
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

    private String getCallingActivityFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: Arguments: " + getArguments());

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            return bundle.getString(getString(R.string.main_activity));
        } else {
            return null;
        }
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

        if (mPhoto.getComments().size() == 0) {
            mComments.clear();

            Comment firstComment = new Comment();
            firstComment.setComment(Objects.requireNonNull(mPhoto).getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());

            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setupWidgets();
        }

        myRef.child(mContext.getString(R.string.field_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Query query = myRef
                                .child(mContext.getString(R.string.field_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {

                                    Photo photo = new Photo();

                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    photo.setCaption(String.valueOf(Objects.requireNonNull(objectMap)
                                            .get(mContext.getString(R.string.field_caption))));
                                    photo.setDate_created(String.valueOf(Objects.requireNonNull(objectMap)
                                            .get(mContext.getString(R.string.field_date_created))));
                                    photo.setTags(String.valueOf(Objects.requireNonNull(objectMap)
                                            .get(mContext.getString(R.string.field_tags))));
                                    photo.setUser_id(String.valueOf(Objects.requireNonNull(objectMap)
                                            .get(mContext.getString(R.string.field_user_id))));
                                    photo.setPhoto_id(String.valueOf(Objects.requireNonNull(objectMap)
                                            .get(mContext.getString(R.string.field_photo_id))));
                                    photo.setImage_path(String.valueOf(Objects.requireNonNull(objectMap)
                                            .get(mContext.getString(R.string.field_image_path))));

                                    mComments.clear();

                                    Comment firstComment = new Comment();
                                    firstComment.setComment(Objects.requireNonNull(mPhoto).getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());

                                    mComments.add(firstComment);

                                    for (DataSnapshot dataSnapshot : singleSnapshot
                                            .child(mContext.getString(R.string.field_comments)).getChildren()) {

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

                                    mPhoto = photo;

                                    setupWidgets();

//                    List<Like> likeList = new ArrayList<>();
//
//                    for (DataSnapshot dataSnapshot : singleSnapshot
//                            .child(getString(R.string.field_likes)).getChildren()) {
//
//                        Like like = new Like();
//                        like.setUser_id(Objects.requireNonNull(dataSnapshot.getValue(Like.class)).getUser_id());
//
//                        likeList.add(like);
//                    }
//
//                    photo.setLikes(likeList);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: Query Cancelled");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
