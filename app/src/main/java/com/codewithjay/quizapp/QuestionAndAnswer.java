package com.codewithjay.quizapp;

/**
 * POJO class storing the question, choices and the answer.
 */

public class QuestionAndAnswer {
    private String question;
    private String image;

    // Choices are represented as four variables instead of an array, since each question should
    // strictly have four choices.
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;

    private String answer;

    QuestionAndAnswer() {
        this.question = "";
        this.choice1 = "";
        this.choice2 = "";
        this.choice3 = "";
        this.choice4 = "";
        this.answer = "";
        this.image = "";
    }

    QuestionAndAnswer(String question, String image, String choice1, String choice2, String choice3, String choice4, String answer) {
        this.question = question;
        this.image = image;
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
        this.choice4 = choice4;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getChoice1() {
        return choice1;
    }

    public void setChoice1(String choice1) {
        this.choice1 = choice1;
    }

    public String getChoice2() {
        return choice2;
    }

    public void setChoice2(String choice2) {
        this.choice2 = choice2;
    }

    public String getChoice3() {
        return choice3;
    }

    public void setChoice3(String choice3) {
        this.choice3 = choice3;
    }

    public String getChoice4() {
        return choice4;
    }

    public void setChoice4(String choice4) {
        this.choice4 = choice4;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
