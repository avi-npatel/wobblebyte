package com.wobblebyte.app.quiz;

import java.util.List;

/** One topic of the quiz, such as "Passwords" or "Phishing". */
public final class QuizModule {
    public final String id;
    public final String title;
    public final String summary;
    public final List<QuizQuestion> questions;

    public QuizModule(String id, String title, String summary, List<QuizQuestion> questions) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.questions = questions;
    }
}
