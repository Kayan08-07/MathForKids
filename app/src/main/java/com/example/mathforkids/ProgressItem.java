package com.example.mathforkids;

public class ProgressItem {
    private final String documentId;
    private final String question;
    private final int childAnswer;
    private final int correctAnswer;
    private final boolean correct;

    public ProgressItem(String question, int childAnswer, int correctAnswer, boolean correct) {
        this("", question, childAnswer, correctAnswer, correct);
    }

    public ProgressItem(String documentId, String question, int childAnswer, int correctAnswer, boolean correct) {
        this.documentId = documentId;
        this.question = question;
        this.childAnswer = childAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getQuestion() {
        return question;
    }

    public int getChildAnswer() {
        return childAnswer;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }
}
