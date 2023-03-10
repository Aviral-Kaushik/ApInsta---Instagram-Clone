package com.aviral.apinsta.Search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aviral.apinsta.Models.User;
import com.aviral.apinsta.Profile.ProfileActivity;
import com.aviral.apinsta.R;
import com.aviral.apinsta.Utils.BottomNavigationViewHelper;
import com.aviral.apinsta.Utils.UserListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "AviralKaushik";

    private static final int ACTIVITY_NUMBER = 1;

    private EditText mSearchParams;
    private ListView mListView;

    private List<User> mUserList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchParams = findViewById(R.id.search);
        mListView = findViewById(R.id.listView);
        Log.d(TAG, "onCreate: started.");

//        hideSoftKeyboard();
        setUpBottomNavigation();
        initTextListener();
    }

    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearchParams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = mSearchParams.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        //update the users list view
        if (keyword.length() != 0) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference.child(getString(R.string.user))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: User Found:"
                                + singleSnapshot.getValue(User.class));

                        mUserList.add(singleSnapshot.getValue(User.class));
                        //update the users list view
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list");

        UserListAdapter userListAdapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_list_view, mUserList);

        mListView.setAdapter(userListAdapter);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());

            //navigate to profile activity
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_avtivity));
            intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
            startActivity(intent);
        });
    }


//    private void hideSoftKeyboard(){
//        if(getCurrentFocus() != null){
//            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//    }


    /**
     * BottomNavigationView setup
     */
    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(this , this, bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }
}