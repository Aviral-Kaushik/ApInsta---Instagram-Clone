package com.aviral.apinsta.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Login.LoginActivity;
import com.aviral.apinsta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignOutFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar progressBar;
    private TextView textView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_out, container, false);

        progressBar = view.findViewById(R.id.sign_out_progress_bar);
        textView = view.findViewById(R.id.signing_out);
        AppCompatButton signOut = view.findViewById(R.id.sign_out);
        AppCompatButton cancel = view.findViewById(R.id.cancel_sign_out);

        setUpFirebaseAuth();

        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);


        signOut.setOnClickListener(view1 -> {

            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);

            mAuth.signOut();

            requireActivity().finish();
        });

        cancel.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
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

                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
