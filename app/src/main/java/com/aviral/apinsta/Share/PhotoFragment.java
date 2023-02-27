package com.aviral.apinsta.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Profile.AccountSettingsActivity;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.Permissions;

import java.util.Objects;

public class PhotoFragment extends Fragment {

    private static final String TAG = "AviralKaushik";

    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int CAMERA_REQUEST_CODE = 5;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);

        Button openCamera = view.findViewById(R.id.open_camera);

        openCamera.setOnClickListener(view1 -> {

            if ((Objects.requireNonNull((ShareActivity) getActivity())).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) {
                if ((Objects.requireNonNull((ShareActivity)getActivity())).checkPermissions(Permissions.CAMERA_PERMISSION[0])) {
                    Log.d(TAG, "Launching Camera");
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else {
                    Objects.requireNonNull((ShareActivity)getActivity()).verifyPermission(Permissions.CAMERA_PERMISSION);
                }
            }

        });
        return view;
    }

    private boolean isRootTask() {
        return (Objects.requireNonNull((ShareActivity) getActivity())).getTask() == 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "Done taking Photo");
            Log.d(TAG, "Navigating Back");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            if (isRootTask()) {

                try {
                    Log.d(TAG, "onActivityResult: Receive New Image for sharing from camera: " + bitmap);

                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);

                } catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }

            }
            else {

                try {
                    Log.d(TAG, "onActivityResult: Receive New Bitmap Data from camera: " + bitmap);


                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    requireActivity().finish();


                } catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }

            }

        }
    }
}
