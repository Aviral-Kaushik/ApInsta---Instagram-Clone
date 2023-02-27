package com.aviral.apinsta.Share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.FirebaseMethods;
import com.aviral.apinsta.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "AviralKaushik";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;

    private int imageCount = 0;
    private Intent intent;
    private String imgURL;
    private Bitmap bitmap;

    private EditText captionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        firebaseMethods = new FirebaseMethods(this);

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(view -> finish());

        captionText = findViewById(R.id.caption);

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(view -> {
            Log.d(TAG, "onCreate: Attempting to upload new image To Firebase");
            Toast.makeText(this, "Attempting to upload new image To Firebase", Toast.LENGTH_SHORT).show();

            String caption = String.valueOf(captionText.getText());

            if (intent.hasExtra(getString(R.string.selected_image))) {
                imgURL = intent.getStringExtra(getString(R.string.selected_image));
                firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgURL, null);
            } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);
            }
        });

        setImage();
        setUpFirebase();
    }

    private void setImage() {
        intent = getIntent();
        ImageView image = findViewById(R.id.imgShare);

        String mAppend = "file:/";

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgURL = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: Get New Image url: " + imgURL);
            UniversalImageLoader.setImage(imgURL, image, null, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: Get New Image");
            image.setImageBitmap(bitmap);
        }
    }

    private void setUpFirebase() {

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = firebaseDatabase.getReference();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                Log.d("AviralKaushik", "User Logged In:" + user.getUid());
            }
            else {
                Log.d("AviralKauhsik", "No User");
            }
        };

        Log.d(TAG, "setUpFirebase: Image Count: " + imageCount);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                imageCount = firebaseMethods.getImageCount(snapshot);
                Log.d(TAG, "onDataChange: Image Count: " + imageCount);

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