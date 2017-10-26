package com.codewithjay.quizapp;

/**
 * POJO class representing each quiz.
 */

public class Quiz {
    // Name of the quiz.
    private String name;

    // Path of the thumbnail image, relative to the QuizApp directory in Firebase storage.
    private String image;

    // No argument constructor is needed for this class, for using it in Firebase StorageReference's
    // ChildEventListener.
    public Quiz() {
        this.name = "";
        this.image = "";
    }

    public Quiz(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
