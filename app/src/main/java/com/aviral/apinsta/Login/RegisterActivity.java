package com.aviral.apinsta.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context context;
    private EditText emailText, nameText, passwordText;
    private String username, email;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvPleaseWait;


    private FirebaseMethods firebaseMethods;
    private DatabaseReference myRef;

    private String append = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setUpFirebaseAuth();
        setUpActivityWidgets();
        init();

        firebaseMethods = new FirebaseMethods(context);

        progressBar.setVisibility(View.GONE);
        tvPleaseWait.setVisibility(View.GONE);
    }

    private void setUpActivityWidgets() {

        context = RegisterActivity.this;
        emailText = findViewById(R.id.input_email_register);
        nameText = findViewById(R.id.input_username_register);
        passwordText = findViewById(R.id.input_password_register);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBarRegister);
        tvPleaseWait = findViewById(R.id.please_wait_register);
        TextView tvLogin = findViewById(R.id.link_signIn);
        tvLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private void init() {

        btnRegister.setOnClickListener(view -> {

            email = String.valueOf(emailText.getText());
            username = String.valueOf(nameText.getText());
            String password = String.valueOf(passwordText.getText());

            if (isEmailValid(email) && !username.equals(email) && !username.equals("") && !password.equals("")) {
//                progressBar.setVisibility(View.VISIBLE);
//                tvPleaseWait.setVisibility(View.VISIBLE);

                firebaseMethods.registerUserWithEmailAndPassword(email, password);

            } else {
                Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }

        });

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

                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        append = Objects.requireNonNull(myRef.push().getKey()).substring(3, 10);

                        Log.d("AviralKauhsik", "Username Already Exists: Changing username: " + append);
                    }
                }

                String mUsername = username + append;

                firebaseMethods.addNewUserData(email, mUsername.toLowerCase(), "", "", "");

                mAuth.signOut();

                Toast.makeText(context, "Account Created Successfully! Verification Email has been Send!", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUpFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                Log.d("AviralKaushik", "User Logged In:" + user.getUid());

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        checkIfUsernameExists(username);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                finish();

            }
            else {
                Log.d("AviralKauhsik", "No User");
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
