package com.aviral.apinsta.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Models.Comment;
import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.MainfeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final String TAG = "AviralKaushik";

    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;

    private int mResult;
    private ArrayList<Photo> mPaginatedPhoto;
    private MainfeedListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();

        return view;
    }

    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.field_following))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(String.valueOf(singleSnapshot.child(getString(R.string.field_user_id)).getValue()));
                }

                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

                //get the photos
                getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: Getting Photos");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0; i < mFollowing.size(); i++) {
            final int count  = i;

            Query query = reference.child(getString(R.string.field_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {

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

                        mPhotos.add(newPhoto);

                    }

                    if (count >= mFollowing.size() - 1) {
                        displayPhoto();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void displayPhoto() {

        mPaginatedPhoto = new ArrayList<>();

        if (mPhotos != null) {

            try {

                //Sorting photos in terms of date
                Collections.sort(mPhotos, (photo, t1) -> t1.getDate_created().compareTo(photo.getDate_created()));

                int iteration = mPhotos.size();

                if (iteration > 10) {
                    iteration = 10;
                }

                mResult = 10;

                for (int i = 0; i < iteration; i++) {
                    mPaginatedPhoto.add(mPhotos.get(i));
                }

                mAdapter = new MainfeedListAdapter(requireActivity(),
                        R.layout.layout_mainfeed_list_item,
                        mPaginatedPhoto);
                mListView.setAdapter(mAdapter);

            } catch (NullPointerException e) {
                Log.d(TAG, "displayPhoto: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "displayPhoto: IndexOutOfBoundsException: " + e.getMessage());
            }

        }
    }

    public void displayMorePhotos() {
        Log.d(TAG, "getMorePhotos: Displaying More Photos");

        try {

            if (mPhotos.size() > mResult && mPhotos.size() > 0) {

                int iteration;

                if (mPhotos.size() > (mResult + 10)) {
                    Log.d(TAG, "displayMorePhotos: There are greater than 10 photos");
                    iteration = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: There are less than 10 photos");
                    iteration = mPhotos.size() - mResult;
                }

                // add the new photos to the paginated result
                for (int i = mResult; i < mResult + iteration; i++) {
                    mPaginatedPhoto.add(mPhotos.get(i));
                }

                mResult = mResult + iteration;

                mAdapter.notifyDataSetChanged();
            }

        } catch (NullPointerException e) {
            Log.d(TAG, "displayPhoto: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "displayPhoto: IndexOutOfBoundsException: " + e.getMessage());
        }
    }

}
