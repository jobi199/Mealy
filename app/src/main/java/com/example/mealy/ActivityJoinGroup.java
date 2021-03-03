package com.example.mealy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.listeners.ItemRemovedListener;
import com.raycoarana.codeinputview.CodeInputView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityJoinGroup extends AppCompatActivity {

    //General
    private static final String TAG = "JOIN_LOBBY_ACTIVITY";
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    //Public
    public static List<Integer> mStackIDs;
    public static List<Object> mResolvers;
    public static List<Integer> mLikedIDs;
    public static List<Integer> mDislikedIDs;

    //Classes
    private CodeInputHandler mCodeInputHandler;
    private SwipePlaceHolderViewHandlerJoinGroup mSwipePlaceHolderViewHandlerJoinGroup;
    private SwipeHandler mSwipeHandler;
    private PageHandler mPageHandler;

    //Pages
    private LinearLayout mPage1;
    private LinearLayout mPage2;
    private LinearLayout mPage3;
    private LinearLayout mLoadScreen;
    private LinearLayout mTutorial;

    //Lists
    private List<Recipe> mAllRecipesList;

    //Views
    private BottomNavigationView mBottomNavigationView;
    private SwipePlaceHolderView mSwipePlaceHolderView;
    private CodeInputView mCodeInputView;
    private TextView mTextViewCodeInputStatus;
    private TextView mTextViewJoinGroupButton;
    private TextView mTextViewLeaveGroupButton;
    private TextView mTextViewResultPageHeader;
    private TextView mTextViewGroupCode2;
    private TextView mTextViewGroupCode3;

    //Result
    private LinearLayout mLinearLayoutResultTable;
    private ListView mListView;

    //Database
    private DatabaseReference mDatabaseReference;
    private DataSnapshot mDataSnapshot;

    //Layouts
    private LinearLayout mLinearLayoutPlaceholderResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        setupElements();
    }

    @Override
    protected void onPause() {
        mSwipeHandler.saveLikedIndices(mLikedIDs);
        mSwipeHandler.saveDislikedIndices(mDislikedIDs);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(0,0);
    }

    private void generateUserID() {
        if (mSharedPreferences.getString("UserID", "").equals("")) {
            RandomGenerator randomStringBuilder = new RandomGenerator(mSharedPreferences);
            randomStringBuilder.generateRandomUserID(30);
        }
    }

    private void uploadRatings() {
        String code = mSharedPreferences.getString("JoinGroupCode", "");
        DatabaseHandler mDatabaseHandler = new DatabaseHandler(mSharedPreferences);
        mDatabaseHandler.updateGroupCounter(mDataSnapshot, mLikedIDs, mDatabaseReference, code);
        mDatabaseHandler.updateGroupCompletedUserList(mDataSnapshot, mDatabaseReference, code);
        mDatabaseHandler.updateGroupPeopleNumber(mDataSnapshot, mDatabaseReference, code);
        switchToPage3();
    }

    private void checkUserInputCode(String code) {
        mCodeInputHandler.checkStatus(code, mDataSnapshot, mTextViewCodeInputStatus, mCodeInputView, mTextViewJoinGroupButton);
    }

    private void checkIfGroupIsCompleted() {
        if (mDataSnapshot != null) {
            String code = mSharedPreferences.getString("JoinGroupCode", "");
            String status = (String) mDataSnapshot.child(code).child("group_status").getValue();
            if (status != null && status.equals("closed")) {
                mLinearLayoutPlaceholderResults.setVisibility(View.GONE);
                mLinearLayoutResultTable.setVisibility(View.VISIBLE);
                mTextViewResultPageHeader.setText("Teilnehmer: "+String.valueOf(mDataSnapshot.child(code).child("people_number").getValue()));
                loadResults();
            }
        }
    }

    private void loadResults() {
        if (mAllRecipesList == null) {
            mAllRecipesList = JsonLoader.loadRecipies(mContext);
        }
        mLinearLayoutPlaceholderResults.setVisibility(View.GONE);
        mSwipeHandler.loadOnlineResults(mDataSnapshot, mAllRecipesList, "JoinGroupCode");
        RecipeListAdapter adapter = new RecipeListAdapter(this, R.layout.list_view_apdapter_layout, mSwipeHandler.mOnlineResults);
        mListView.setAdapter(adapter);
        mTextViewLeaveGroupButton.setText("Gruppe verlassen");
        mTextViewLeaveGroupButton.setClickable(true);
    }

    public void leaveGroup(View v) {
        deleteSavedOnlineData();
        mLikedIDs = new ArrayList<>();
        mDislikedIDs = new ArrayList<>();
        restartActivity();
    }

    public void switchToPage2(View v) {
        mPageHandler.savePage(2);
        loadCorrectPage();
    }

    private void switchToPage3() {
        mPageHandler.savePage(3);
        loadCorrectPage();
    }

    private void loadCorrectPage() {
        mPageHandler.loadCorrectPage();
        Integer currentPage = mSharedPreferences.getInt("PageJoin", 1);
        if (currentPage == 2) {
            mTextViewGroupCode2.setText("Gruppe: "+mSharedPreferences.getString("JoinGroupCode", ""));
            setupLists();
            setupSwipePlaceholderView();
        }
        else if (currentPage == 3) {
            mTextViewGroupCode3.setText("Gruppe: "+mSharedPreferences.getString("JoinGroupCode", ""));
            checkIfGroupIsCompleted();
        }
    }

    private void setupElements() {
        mSharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        mContext = getApplicationContext();
        //resetSharedPreferences();

        setupPages();
        generateUserID();
        setupClasses();
        setupViews();
        setupLayouts();
        setupBottomNavigationBar();
        loadCorrectPage();
        setupDatabase();
    }

    private void setupLayouts() {
        mLinearLayoutPlaceholderResults = findViewById(R.id.linear_layout_result_page_placeholder);
        mLinearLayoutResultTable = findViewById(R.id.linear_layout_result_table);
        mLinearLayoutResultTable.setVisibility(View.GONE);
    }

    private void setupSwipePlaceholderView() {
        mCodeInputHandler.loadSelectedIDs();
        mSwipePlaceHolderView = findViewById(R.id.swipeView);
        mSwipePlaceHolderViewHandlerJoinGroup.setSwipePlaceHolderViewBuilder(mSwipePlaceHolderView);
        mSwipePlaceHolderViewHandlerJoinGroup.loadSwipePlaceholderView(mCodeInputHandler.mSelectedIDs, mAllRecipesList, mSwipePlaceHolderView);
        mStackIDs = mSwipePlaceHolderViewHandlerJoinGroup.mStackIDs;
        mResolvers = mSwipePlaceHolderViewHandlerJoinGroup.mResolvers;
        mSwipePlaceHolderView.addItemRemoveListener(new ItemRemovedListener() {
            @Override
            public void onItemRemoved(int count) {
                if (mSwipePlaceHolderView.getAllResolvers().size() == 0) {
                    uploadRatings();
                }
            }
        });
    }

    private void setupClasses() {
        mCodeInputHandler = new CodeInputHandler(mContext, mSharedPreferences);
        mSwipePlaceHolderViewHandlerJoinGroup = new SwipePlaceHolderViewHandlerJoinGroup(mContext);
        mSwipeHandler = new SwipeHandler("Join", mSharedPreferences);
        mPageHandler = new PageHandler(mPage1, mPage2, mPage3, mTutorial, mLoadScreen, mSharedPreferences, "Join");
    }

    private void setupLists() {
        mSwipeHandler.loadLikedIndices();
        mSwipeHandler.loadDislikedIndices();
        mAllRecipesList = JsonLoader.loadRecipies(mContext);
        mLikedIDs = mSwipeHandler.mLikedIDs;
        mDislikedIDs = mSwipeHandler.mDislikedIDs;
    }

    private void setupPages() {
        mPage1 = findViewById(R.id.page_1);
        mPage2 = findViewById(R.id.page_2);
        mPage3 = findViewById(R.id.page_3);
        mLoadScreen = findViewById(R.id.load_screen);
        mTutorial = findViewById(R.id.tutorial);
        mTutorial.setVisibility(View.GONE);
        mTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorial.setVisibility(View.GONE);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("JoinTutorial", true);
                editor.commit();
            }
        });
    }

    private void setupViews() {
        mListView = findViewById(R.id.list_view);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setSelectedItemId(R.id.join_group);

        mTextViewGroupCode2 = findViewById(R.id.text_view_group_code_2);
        mTextViewGroupCode3 = findViewById(R.id.text_view_group_code_3);

        mTextViewCodeInputStatus = findViewById(R.id.text_view_code_status);
        mTextViewJoinGroupButton = findViewById(R.id.text_view_join_group);
        mTextViewJoinGroupButton.setClickable(false);

        mTextViewLeaveGroupButton = findViewById(R.id.text_view_leave_group);
        mTextViewLeaveGroupButton.setClickable(false);

        mTextViewResultPageHeader = findViewById(R.id.text_view_result_page_top);

        mCodeInputView = findViewById(R.id.code_input_view);
        mCodeInputView.addOnCompleteListener(code -> checkUserInputCode(code));
    }

    private void setupDatabase() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDataSnapshot = dataSnapshot;
                checkIfGroupIsCompleted();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TAG", "DATABASE ERROR: ", databaseError.toException());
            }
        });
    }

    private void setupBottomNavigationBar() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.play_alone:
                        mPageHandler.showLoadScreen();
                        startActivity(new Intent(getApplicationContext(), ActivityPlayAlone.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.join_group:
                        return true;
                    case R.id.create_group:
                        mPageHandler.showLoadScreen();
                        startActivity(new Intent(getApplicationContext(), ActivityCreateGroup.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void restartActivity() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0,0);
    }

    private void resetSharedPreferences() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    private void deleteSavedOnlineData() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("PageJoin", 1);
        editor.putString("JoinGroupIDs", "");
        editor.putString("JoinGroupCode", "");
        editor.putString("LikedJoinIDs", "");
        editor.putString("DislikedJoinIDs", "");
        editor.putBoolean("JoinTutorial", false);
        editor.commit();
    }
}