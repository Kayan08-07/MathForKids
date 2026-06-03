package com.example.mathforkids;

import java.util.Random;

public class MathQuestionGenerator {
    private final Random random = new Random();

    public MathQuestion generate(String operation) {
        int first = random.nextInt(10) + 1;
        int second = random.nextInt(10) + 1;

        // في القسمة نجعل الناتج صحيحاً حتى تكون مناسبة للأطفال.
        if ("/".equals(operation)) {
            int answer = random.nextInt(10) + 1;
            second = random.nextInt(9) + 1;
            first = answer * second;
            return new MathQuestion(first, second, "÷", answer);
        }

        if ("-".equals(operation) && second > first) {
            int temp = first;
            first = second;
            second = temp;
        }

        if ("+".equals(operation)) {
            return new MathQuestion(first, second, "+", first + second);
        }

        if ("-".equals(operation)) {
            return new MathQuestion(first, second, "-", first - second);
        }

        return new MathQuestion(first, second, "*", first * second);
    }
}
