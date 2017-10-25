package com.codewithjay.quizapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class QuizActivity extends AppCompatActivity {
    private static final String QUIZ_ACTIVITY_TAG = "QuizAppQuizActivity";
    private static final String STORAGE_URI_PREFIX = "gs://quizapp-8e847.appspot.com/QuizImages/";

    private List<QuestionAndAnswer> mQuestionAndAnswerList;
    private int mScore;
    private int mQuestionNo = 0;
    private String mUserName;
    private String mQuizName;

    // UI member variables.
    TextView mWelcomeText;

    TextView mQuestionText;
    Button mChoice1Button;
    Button mChoice2Button;
    Button mChoice3Button;
    Button mChoice4Button;

    ImageView mQuestionImageView;

    // Firebase member variables.
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFirebaseStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Intent quizIntent = getIntent();
        mUserName = QuizSelectorActivity.GetUserNameFromIntent(quizIntent);
        mQuizName = QuizSelectorActivity.GetQuizNameFromIntent(quizIntent);

        mQuestionAndAnswerList = new ArrayList<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = mFirebaseDatabase.getReference().child("masterSheet").child("quizzes").child(mQuizName).child("questions");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseStorageReference = mFirebaseStorage.getReference().child("QuizImages");

        mWelcomeText = (TextView) findViewById(R.id.welcome_text);

        mQuestionText = (TextView) findViewById(R.id.question_text);
        mChoice1Button = (Button) findViewById(R.id.choice1_button);
        mChoice2Button = (Button) findViewById(R.id.choice2_button);
        mChoice3Button = (Button) findViewById(R.id.choice3_button);
        mChoice4Button = (Button) findViewById(R.id.choice4_button);

        mQuestionImageView = (ImageView) findViewById(R.id.question_image);

        View.OnClickListener userInputValidator = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button currentButton = (Button)v;
                final String answer = mQuestionAndAnswerList.get(mQuestionNo).getAnswer();
                Log.d(QUIZ_ACTIVITY_TAG, "User answer is " + currentButton.getText() + ". Correct answer is " + answer);
                if(currentButton.getText().equals(answer)) {
                    Toast.makeText(getApplicationContext(), "Great job!!", Toast.LENGTH_SHORT).show();
                    mScore += 1;
                    updateWelcomeMessage(mUserName, mScore);
                } else {
                Toast.makeText(getApplicationContext(), "Sorry, so close!!", Toast.LENGTH_SHORT).show();
                }
                ++mQuestionNo;
                if(mQuestionNo < mQuestionAndAnswerList.size()) {
                    updateQuestionUI(mQuestionAndAnswerList.get(mQuestionNo), mQuizName);
                } else {
                    ProcessQuizComplete();
                }
            }
        };

        mChoice1Button.setOnClickListener(userInputValidator);
        mChoice2Button.setOnClickListener(userInputValidator);
        mChoice3Button.setOnClickListener(userInputValidator);
        mChoice4Button.setOnClickListener(userInputValidator);

        mScore = 0;
        updateWelcomeMessage(mUserName, mScore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PerformListCleanUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PerformInitialization();
    }

    void PerformListCleanUp() {
        if(mChildEventListener != null) {
            mFirebaseDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
            mQuestionAndAnswerList.clear();
        }
    }

    void PerformInitialization() {
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    QuestionAndAnswer addedQuestionAndAnswer = dataSnapshot.getValue(QuestionAndAnswer.class);
                    mQuestionAndAnswerList.add(addedQuestionAndAnswer);
                    if(mQuestionNo == 0) {
                        updateQuestionUI(mQuestionAndAnswerList.get(mQuestionNo), mQuizName);
                    }
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

    void updateQuestionUI(QuestionAndAnswer questionAndAnswer, String quizName) {
        Log.d(QUIZ_ACTIVITY_TAG, "Question image is " + questionAndAnswer.getImage());
        mQuestionText.setText(questionAndAnswer.getQuestion());
        mChoice1Button.setText(questionAndAnswer.getChoice1());
        mChoice2Button.setText(questionAndAnswer.getChoice2());
        mChoice3Button.setText(questionAndAnswer.getChoice3());
        mChoice4Button.setText(questionAndAnswer.getChoice4());
        mFirebaseStorageReference.child(questionAndAnswer.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(QUIZ_ACTIVITY_TAG, "Image downloaded successfully.");
                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(mQuestionImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(QUIZ_ACTIVITY_TAG, "Image download failed.");
            }
        });
    }

    void updateWelcomeMessage(String userName, int score) {
        mWelcomeText.setText("Welcome " + userName + ", Your score is " + Integer.toString(score));
    }

    void ProcessQuizComplete() {
        Toast.makeText(this, "Congratulations, you have completed the " + mQuizName + " quiz.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, QuizSelectorActivity.class));
    }
}
