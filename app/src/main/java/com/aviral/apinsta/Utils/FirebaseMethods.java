package com.aviral.apinsta.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.aviral.apinsta.Home.MainActivity;
import com.aviral.apinsta.Models.Photo;
import com.aviral.apinsta.Models.User;
import com.aviral.apinsta.Models.UserAccountSettings;
import com.aviral.apinsta.Models.UserSettings;
import com.aviral.apinsta.Profile.AccountSettingsActivity;
import com.aviral.apinsta.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "AviralKaushik";

    private final FirebaseAuth mAuth;
    private final DatabaseReference myRef;
    private final StorageReference mStorageReference;

    private String userID;
    private final Context context;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context mContext) {
        context = mContext;

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, final String caption, final int imageCount, String imgURL, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: Attempting to upload new image to storage");

        FilePath filePath = new FilePath();

        if (photoType.equals(context.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: Uploading new Photo");

            String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE_LOCATION + user_id + "/photo" + (imageCount + 1));

            if (bm == null) {
                bm = ImageManager.getBitmap(imgURL);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(taskSnapshot -> {

                Toast.makeText(context, "New Photo Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "uploadNewPhoto: New Photo Uploaded Successfully!");

                Task<Uri> task = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata())
                        .getReference()).getDownloadUrl();

                task.addOnSuccessListener(uri -> {
                    String firebaseUri = uri.toString();
                    addPhotoToDatabase(caption, firebaseUri);

                    context.startActivity(new Intent(context, MainActivity.class));

                });


            }).addOnFailureListener(e -> {
                Log.d(TAG, "onFailure: Failed to Upload Photo " + e.getMessage());

                Toast.makeText(context, "Failed to Upload Photo", Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(snapshot -> {
                double progress = (double) (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                if (progress - 15 > mPhotoUploadProgress) {
                    Toast.makeText(context, "Photo Upload Progress: " + progress + "%", Toast.LENGTH_SHORT).show();

                    mPhotoUploadProgress = progress;
                }

                Log.d(TAG, "onProgress: Photo upload progress: " + progress + "% done");

            });

        } else if (photoType.equals(context.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: Uploading new profile photo");

            String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE_LOCATION + user_id + "/profile_photo");

            if (bm == null) {
                bm = ImageManager.getBitmap(imgURL);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(taskSnapshot -> {

                Toast.makeText(context, "New Profile Photo Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "uploadNewPhoto: New Profile Photo Uploaded Successfully!");

                Task<Uri> task = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata())
                        .getReference()).getDownloadUrl();

                task.addOnSuccessListener(uri -> {
                    String firebaseUri = uri.toString();

                    setProfilePhoto(firebaseUri);

                    ((AccountSettingsActivity)context).setUpViewPager(
                            ((AccountSettingsActivity)context).sectionStatePagerAdapter
                                    .getFragmentNumber(context.getString(R.string.edit_profile_fragment))
                    );

                });


            }).addOnFailureListener(e -> {
                Log.d(TAG, "onFailure: Failed to Upload Profile Photo " + e.getMessage());

                Toast.makeText(context, "Failed to Upload Profile Photo", Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(snapshot -> {
                double progress = (double) (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                if (progress - 15 > mPhotoUploadProgress) {
                    Toast.makeText(context, "Profile Photo Upload Progress: " + progress + "%", Toast.LENGTH_SHORT).show();

                    mPhotoUploadProgress = progress;
                }

                Log.d(TAG, "onProgress: Profile Photo upload progress: " + progress + "% done");

            });

        }

    }

    private void setProfilePhoto(String url) {

        Log.d(TAG, "setProfilePhoto: Setting New Profile Photo and adding it to user_account_settings node: "+ url);

        myRef.child(context.getString(R.string.user_account_settings))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(context.getString(R.string.profile_photo))
                .setValue(url);

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String url) {
        Log.d(TAG, "addPhotoToDatabase: Adding Photo Data To Database");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(context.getString(R.string.field_photos)).push().getKey();
        Photo photo = new Photo();

        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        photo.setPhoto_id(newPhotoKey);

        myRef.child(context.getString(R.string.field_user_photos))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(Objects.requireNonNull(newPhotoKey))
                .setValue(photo);

        myRef.child(context.getString(R.string.field_photos))
                .child(Objects.requireNonNull(newPhotoKey))
                .setValue(photo);


    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ignored : dataSnapshot
                .child(context.getString(R.string.field_user_photos))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .getChildren()) {
            count++;
        }

        return count;
    }

    public void updateUserSettings(String displayName, String website, String description, long phoneNumber) {


        if (displayName != null) {
            myRef.child(context.getString(R.string.user_account_settings))
                    .child(userID)
                    .child(context.getString(R.string.field_display_name))
                    .setValue(displayName);
        }


        if (website != null) {
            myRef.child(context.getString(R.string.user_account_settings))
                    .child(userID)
                    .child(context.getString(R.string.field_website))
                    .setValue(website);
        }

        if (description != null) {
            myRef.child(context.getString(R.string.user_account_settings))
                    .child(userID)
                    .child(context.getString(R.string.field_description))
                    .setValue(description);
        }

        if (phoneNumber != 0) {
            myRef.child(context.getString(R.string.user))
                    .child(userID)
                    .child(context.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }

    }

    public void updateUsername(String username) {
        myRef.child(context.getString(R.string.user))
                .child(userID)
                .child(context.getString(R.string.field_username))
                .setValue(username);

        myRef.child(context.getString(R.string.user_account_settings))
                .child(userID)
                .child(context.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateEmail(String email) {
        myRef.child(context.getString(R.string.user))
                .child(userID)
                .child(context.getString(R.string.field_email))
                .setValue(email);
    }


    public void registerUserWithEmailAndPassword(final String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.d("AviralKaushik", "Register Status: " + task.isSuccessful());

                    if (!task.isSuccessful()) {
                        Toast.makeText(context, "Registration Failed!, Please Try Again", Toast.LENGTH_SHORT).show();

                        Log.d("AviralKaushik", "Authentication Failed: " + task.getException());
                    } else if (task.isSuccessful()) {

                        sendVerificationEmail();

                        Toast.makeText(context, "Registration Succesfull", Toast.LENGTH_SHORT).show();
                        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    }
                });

    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Couldn't Send Verification Email", Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }

    public void addNewUserData(String email, String username, String description, String website, String profile_photo) {

        User newUser = new User(userID, 1, email, StringManipulation.compressUsername(username));

        myRef.child(context.getString(R.string.user)).child(userID).setValue(newUser);

        UserAccountSettings userAccountSettings = new UserAccountSettings(
                description,
                username,
                0, 0, 0,
                profile_photo,
                StringManipulation.compressUsername(username),
                website,
                userID
        );

        myRef.child(context.getString(R.string.user_account_settings)).child(userID).setValue(userAccountSettings);
    }

    public UserSettings getUserAccountDetails(DataSnapshot dataSnapshot) {

        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (Objects.requireNonNull(ds.getKey()).equals(context.getString(R.string.user_account_settings))) {
                Log.d("AviralKaushik", "FirebaseMethods: getUserAccountDetails" + ds);

                try {
                    userAccountSettings.setDisplay_name(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getDisplay_name()
                    );
                    userAccountSettings.setUsername(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getUsername()
                    );
                    userAccountSettings.setWebsite(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getWebsite()
                    );
                    userAccountSettings.setDescription(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getDescription()
                    );
                    userAccountSettings.setFollowers(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getFollowers()
                    );
                    userAccountSettings.setFollowing(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getFollowing()
                    );
                    userAccountSettings.setPosts(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getPosts()
                    );
                    userAccountSettings.setProfile_photo(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(UserAccountSettings.class))
                                    .getProfile_photo()
                    );

                } catch (NullPointerException e) {
                    Log.d("AviralKauhsik", "FirebaseMethods NullPointerException: " + e.getMessage());
                    Toast.makeText(context, "Cannot Fetch User Account Details this moment!", Toast.LENGTH_SHORT).show();
                }
            }

            if (ds.getKey().equals(context.getString(R.string.user))) {
                Log.d("AviralKaushik", "FirebaseMethods: getUserDetails" + ds);

                try {
                    user.setUser_id(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(User.class))
                                    .getUser_id()
                    );
                    user.setPhone_number(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(User.class))
                                    .getPhone_number()
                    );
                    user.setEmail(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(User.class))
                                    .getEmail()
                    );
                    user.setUsername(
                            Objects.requireNonNull(ds.child(userID)
                                    .getValue(User.class))
                                    .getUsername()
                    );
                } catch (NullPointerException e) {
                    Log.d("AviralKauhsik", "FirebaseMethods NullPointerException: " + e.getMessage());
                    Toast.makeText(context, "Cannot Fetch User Details this moment!", Toast.LENGTH_SHORT).show();
                }


            }
        }
        return new UserSettings(user, userAccountSettings);
    }
}
