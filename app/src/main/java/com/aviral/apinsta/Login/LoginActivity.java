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

import androidx.appcompat.app.AppCompatActivity;

import com.aviral.apinsta.Home.MainActivity;
import com.aviral.apinsta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context context;
    private ProgressBar progressBar;
    private EditText emailText, passwordText;
    private TextView tvPleaseWait;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpFirebaseAuth();
        setUpActivityWidgets();
        init();

        progressBar.setVisibility(View.GONE);
        tvPleaseWait.setVisibility(View.GONE);
    }

    private void setUpActivityWidgets() {
        progressBar = findViewById(R.id.loginRequestLoadingProgressbar);
        context = LoginActivity.this;
        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        tvPleaseWait = findViewById(R.id.please_wait);
        TextView tvSignUp = findViewById(R.id.link_signup);

        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogin = findViewById(R.id.btn_login);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private void init() {
        btnLogin.setOnClickListener(view -> {

            String email = String.valueOf(emailText.getText());
            String password = String.valueOf(passwordText.getText());

            if (isEmailValid(email) && !password.equals("")) {
                progressBar.setVisibility(View.VISIBLE);
                tvPleaseWait.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            Log.d("AviralKaushik", "Log in Status:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                Log.d("AviralKaushik", "Log in Unsuccessful" + task.getException());

                                Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(this, "Log In Successful", Toast.LENGTH_SHORT).show();

                                try {
                                    Intent intent = new Intent(context, MainActivity.class);
                                    startActivity(intent);


                                } catch (NullPointerException e) {
                                    Log.d("AviralKaushik", "Null Pointer Exception: " + e.getMessage());
                                    progressBar.setVisibility(View.GONE);
                                    tvPleaseWait.setVisibility(View.GONE);
                                    mAuth.signOut();
                                }
                            }

                        });
            } else {
                Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }

        });

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();
        }
    }

    private void setUpFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                Log.d("AviralKaushik", "User Logged In:" + user.getUid());
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
