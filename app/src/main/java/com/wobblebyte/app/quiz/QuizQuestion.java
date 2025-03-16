package com.wobblebyte.app.quiz;

import java.util.List;

public final class QuizQuestion {
    public final String prompt;
    public final List<String> options;
    public final int correctIndex;
    public final String explanation;

    public QuizQuestion(String prompt, List<String> options, int correctIndex, String explanation) {
        this.prompt = prompt;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }
}
