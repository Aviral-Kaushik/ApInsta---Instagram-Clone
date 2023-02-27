package com.aviral.apinsta.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.aviral.apinsta.Profile.AccountSettingsActivity;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.FilePath;
import com.aviral.apinsta.Utils.FileSearch;
import com.aviral.apinsta.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.Objects;

public class GalleryFragment extends Fragment {

    private static final String TAG = "AviralKaushik";
    private static final int GRID_COLUMN_NUM = 3;

    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar progressBar;
    private Spinner directorySpinner;

    private ArrayList<String> directories;
    private String selectedImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        gridView = view.findViewById(R.id.gridViewGallery);
        progressBar = view.findViewById(R.id.progressBarGallery);
        directorySpinner = view.findViewById(R.id.spinnerDirectory);
        galleryImage = view.findViewById(R.id.galleryImageView);
        progressBar.setVisibility(View.GONE);

        directories = new ArrayList<>();

        ImageView closeShare = view.findViewById(R.id.ivCloseShare);
        closeShare.setOnClickListener(view1 -> requireActivity().finish());

        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(view1 -> {

            if (isRootTask()) {
                Intent intent = new Intent(getActivity(), NextActivity.class);
                intent.putExtra(getString(R.string.selected_image), selectedImage);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.selected_image), selectedImage);
                intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                startActivity(intent);
                requireActivity().finish();
            }
        });

        init();

        return view;
    }

    private boolean isRootTask() {
        return (Objects.requireNonNull((ShareActivity) getActivity())).getTask() == 0;
    }
    private void init() {

        FilePath filePaths = new FilePath();

        //check for other folders indide "/storage/emulated/0/pictures"
        directories = FileSearch.getDirectoryPath(filePaths.PICTURES);
        directories.set(0, filePaths.CAMERA);


        ArrayList<String> directoryNames = new ArrayList<>();
        for(int i = 0; i < directories.size(); i++){

            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index).replace("/", "");
            directoryNames.add(string);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected: " + directories.get(position));

                //setup our image grid for the directory chosen
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePath(selectedDirectory);

        //set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/GRID_COLUMN_NUM;
        gridView.setColumnWidth(imageWidth);

        String mAppend = "file:/";

        //use the grid adapter to adapter the images to gridview
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_image_view, mAppend, imgURLs);
        gridView.setAdapter(adapter);
        
        try {

            setImage(imgURLs.get(0), galleryImage, mAppend);
            selectedImage = imgURLs.get(0);
            
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
        
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "onItemClick: selected an image: " + imgURLs.get(position));

            setImage(imgURLs.get(position), galleryImage, mAppend);
            selectedImage = imgURLs.get(position);
        });
    }

    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
