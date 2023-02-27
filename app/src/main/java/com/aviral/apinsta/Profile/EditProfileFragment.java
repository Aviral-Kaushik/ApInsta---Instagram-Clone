package com.aviral.apinsta.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Dialogs.ConfirmPasswordDialog;
import com.aviral.apinsta.Models.User;
import com.aviral.apinsta.Models.UserAccountSettings;
import com.aviral.apinsta.Models.UserSettings;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Share.ShareActivity;
import com.aviral.apinsta.Utils.FirebaseMethods;
import com.aviral.apinsta.Utils.UniversalImageLoader;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDialog.OnConfirmPasswordListener {

    private static final String TAG = "AviralKaushik";


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;

    private CircleImageView mProfilePhoto;
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;

    private UserSettings mUserSettings;


    @Override
    public void onConfirmPassword(String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()), password);

        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AviralKauhsik", "Re-authentication Successful");

                        mAuth.fetchSignInMethodsForEmail(String.valueOf(mEmail.getText()))
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (Objects.requireNonNull(task1.getResult().getSignInMethods()).size() == 1) {
                                            Toast.makeText(getActivity(), "This email is already in use", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.d("AviralKaushik", "This Email is Available ");

                                            mAuth.getCurrentUser().updateEmail(String.valueOf(mEmail))
                                                    .addOnCompleteListener(task11 -> {
                                                        Toast.makeText(getActivity(), "Email Updated!", Toast.LENGTH_SHORT).show();
                                                        firebaseMethods = new FirebaseMethods(getActivity());
                                                        firebaseMethods.updateEmail(String.valueOf(mEmail.getText()));
                                                    });
                                        }
                                    }
                                });

                    } else {
                        Log.d("AviralKauhsik", "Re-authentication Failed");
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mChangeProfilePhoto = view.findViewById(R.id.change_profile_photo);
        mUsername = view.findViewById(R.id.input_username);
        mDisplayName = view.findViewById(R.id.display_name);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phoneNumber);


        firebaseMethods = new FirebaseMethods(getActivity());

        setUpFirebase();

        ImageView back = view.findViewById(R.id.backArrow);
        back.setOnClickListener(view1 -> requireActivity().finish());

        ImageView saveChanges = view.findViewById(R.id.save_changes);
        saveChanges.setOnClickListener(view1 -> saveProfileSettings());

        return view;
    }

    private void saveProfileSettings() {
        final String displayName = String.valueOf(mDisplayName.getText());
        final String username = String.valueOf(mUsername.getText());
        final String website = String.valueOf(mWebsite.getText());
        final String description = String.valueOf(mDescription.getText());
        final String email = String.valueOf(mEmail.getText());
        final long phoneNumber = Long.parseLong(String.valueOf(mPhoneNumber.getText()));

        if (!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }
        if (!mUserSettings.getUser().getEmail().equals(email)) {
            ConfirmPasswordDialog confirmPasswordDialog = new ConfirmPasswordDialog();
            confirmPasswordDialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            confirmPasswordDialog.setTargetFragment(EditProfileFragment.this, 1);
        }

        if(!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName)) {
            firebaseMethods.updateUserSettings(displayName, null, null, 0);
        }
        if(!mUserSettings.getUserAccountSettings().getWebsite().equals(website)) {
            firebaseMethods.updateUserSettings(null, website, null, 0);
        }
        if(!mUserSettings.getUserAccountSettings().getDescription().equals(description)) {
            firebaseMethods.updateUserSettings(null, null, description, 0);
        }
        if(mUserSettings.getUser().getPhone_number() != phoneNumber) {
            firebaseMethods.updateUserSettings(null, null, null, phoneNumber);
        }

    }

    private void checkIfUsernameExists(final String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.user))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    firebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Username Added!", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Toast.makeText(getActivity(), "Username Already Exists!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {

        mUserSettings = userSettings;

        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();
        User user = userSettings.getUser();

        String imageUrl = userAccountSettings.getProfile_photo();
        UniversalImageLoader.setImage(imageUrl, mProfilePhoto, null, "");

        mUsername.setText(userAccountSettings.getUsername());
        mDisplayName.setText(userAccountSettings.getDisplay_name());
        mWebsite.setText(userAccountSettings.getWebsite());
        mDescription.setText(userAccountSettings.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(view1 -> {

            Log.d(TAG, "setProfileWidgets: Navigating to Share activity for changing profile photo");

            Intent intent = new Intent(getActivity(), ShareActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            requireActivity().startActivity(intent);
            requireActivity().finish();

        });

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
