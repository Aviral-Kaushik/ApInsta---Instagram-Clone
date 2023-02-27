package com.aviral.apinsta.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.aviral.apinsta.R;

import java.util.Objects;

public class ConfirmPasswordDialog extends DialogFragment {


    public interface OnConfirmPasswordListener{
        void onConfirmPassword(String password);
    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);

        EditText confirmPassword = view.findViewById(R.id.confirm_password);
        TextView cancelDialog = view.findViewById(R.id.dialogCancel);
        TextView confirmDialog = view.findViewById(R.id.dialogConfirm);


        cancelDialog.setOnClickListener(view1 -> Objects.requireNonNull(getDialog()).dismiss());
        confirmDialog.setOnClickListener(view1 -> {

            String password = String.valueOf(confirmPassword.getText());

            if (!password.equals("")) {

                mOnConfirmPasswordListener.onConfirmPassword(password);
                Objects.requireNonNull(getDialog()).dismiss();


            } else {
                Toast.makeText(getActivity(), "Please Enter Password!", Toast.LENGTH_SHORT).show();
            }
        });


        return view;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e("AviralKaushik", "onAttach: ClassCastException: " + e.getMessage() );
        }
    }

}


