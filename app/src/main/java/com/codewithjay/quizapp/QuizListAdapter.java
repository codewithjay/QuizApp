package com.codewithjay.quizapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by max on 10/24/17.
 */

public class QuizListAdapter extends ArrayAdapter<Quiz> {
    private static final String QUIZ_LIST_ADAPTER_TAG = "QuizAppListAdapter";

    private Context mContext;
    private List<Quiz> mQuizzes;
    private StorageReference mFirebaseStorageReference;

    public QuizListAdapter(@NonNull Context context, @NonNull List<Quiz> quizzes, StorageReference storageReference) {
        super(context, R.layout.activity_quiz_selector_item, quizzes);
        this.mContext = context;
        this.mQuizzes = quizzes;
        this.mFirebaseStorageReference = storageReference;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_quiz_selector_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.quiz_selector_item_name);
        textView.setText(mQuizzes.get(position).getName());
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.quiz_image);
        mFirebaseStorageReference.child(mQuizzes.get(position).getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(QUIZ_LIST_ADAPTER_TAG, "Image downloaded successfully.");
                Glide.with(mContext)
                        .load(uri)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(QUIZ_LIST_ADAPTER_TAG, "Image download failed.");
            }
        });
        return rowView;
    }
}
