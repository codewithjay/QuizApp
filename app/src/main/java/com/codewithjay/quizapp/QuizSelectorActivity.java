package com.codewithjay.quizapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main activity which verifies that user is signed in, and allows
 * user to select the quiz.
 */
public class QuizSelectorActivity extends AppCompatActivity {
    private static final String QUIZ_SELECTOR_ACTIVITY_TAG = "QuizAppSelectorActivity";
    private static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 1;
    private static final String INTENT_USER_NAME = "user_name";
    private static final String INTENT_QUIZ_NAME = "quiz_name";

    private String mUserName;
    private List<Quiz> mQuizzes;

    // Firebase member variables.
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFirebaseStorageReference;

    // UI member variables.
    private ListView mQuizList;
    private ArrayAdapter<Quiz> mQuizListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_selector);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = mFirebaseDatabase.getReference().child("masterSheet").child("quiz_data");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseStorageReference = mFirebaseStorage.getReference().child("QuizImages");

        mQuizzes = new ArrayList<Quiz>();

        mQuizList = (ListView) findViewById(R.id.quiz_selector_list);
        mQuizListAdapter = new QuizListAdapter(this, mQuizzes, mFirebaseStorageReference);
        mQuizList.setAdapter(mQuizListAdapter);
        mQuizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(CreateIntentToQuizActivity(mQuizzes.get(position).getName()));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        PerformCleanUpBeforeSignIn();
        if(mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
            mFirebaseAuthStateListener = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mFirebaseAuthStateListener == null) {
            mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    Log.d(QUIZ_SELECTOR_ACTIVITY_TAG, "Inside AuthStateListener.");
                    final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        Log.d(QUIZ_SELECTOR_ACTIVITY_TAG, "User null. Performing signin again.");
                        // User is not logged in.
                        PerformCleanUpBeforeSignIn();
                        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                                .setAvailableProviders(



                                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .setIsSmartLockEnabled(false)
                                .setTheme(R.style.AppTheme)
                                .setLogo(R.mipmap.ic_launcher_round)
                                .build(), RC_SIGN_IN);
                    } else {
                        Log.d(QUIZ_SELECTOR_ACTIVITY_TAG, "User null. Performing signin again.");
                        // User is logged in.
                        PerformInitializationAfterSignIn(firebaseUser.getDisplayName());
                    }
                }
            };
            mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quiz_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.quiz_activity_menu_signout:
                Log.d(QUIZ_SELECTOR_ACTIVITY_TAG, "Signing out.");
                AuthUI.getInstance().signOut(this);
        }
        return super.onOptionsItemSelected(item);
    }


    void PerformCleanUpBeforeSignIn() {
        mUserName = ANONYMOUS;
        if(mChildEventListener != null) {
            mFirebaseDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
            mQuizListAdapter.clear();
        }
    }

    void PerformInitializationAfterSignIn(String userName) {
        mUserName = userName;
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Quiz addedQuiz = dataSnapshot.getValue(Quiz.class);
                    mQuizListAdapter.add(addedQuiz);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mFirebaseDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    Intent CreateIntentToQuizActivity(String quizName) {
        Intent quizIntent = new Intent(this, QuizActivity.class);
        quizIntent.putExtra(INTENT_USER_NAME, mUserName);
        quizIntent.putExtra(INTENT_QUIZ_NAME, quizName);
        return quizIntent;
    }

    static String GetUserNameFromIntent(Intent intent) {
        return intent.getStringExtra(INTENT_USER_NAME);
    }

    static String GetQuizNameFromIntent(Intent intent) {
        return intent.getStringExtra(INTENT_QUIZ_NAME);
    }
}
