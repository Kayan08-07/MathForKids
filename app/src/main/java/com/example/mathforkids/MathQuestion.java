package com.example.mathforkids;

public class MathQuestion {
    private final int firstNumber;
    private final int secondNumber;
    private final String operation;
    private final int answer;

    public MathQuestion(int firstNumber, int secondNumber, String operation, int answer) {
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.operation = operation;
        this.answer = answer;
    }

    public int getAnswer() {
        return answer;
    }

    public String getOperation() {
        return operation;
    }

    public String getQuestionText() {
        return firstNumber + " " + operation + " " + secondNumber + " = ?";
    }

    public String getArabicQuestion() {
        return "كم يساوي " + firstNumber + " " + getArabicOperation() + " " + secondNumber + "؟";
    }

    private String getArabicOperation() {
        if ("+".equals(operation)) return "زائد";
        if ("-".equals(operation)) return "ناقص";
        if ("*".equals(operation)) return "ضرب";
        return "قسمة";
    }
}
