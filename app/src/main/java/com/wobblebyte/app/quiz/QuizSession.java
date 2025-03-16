package com.wobblebyte.app.quiz;

/** One run through a module: tracks the current question and the score. */
public final class QuizSession {
    private final QuizModule module;
    private int index;
    private int score;
    private boolean answered;

    public QuizSession(QuizModule module) {
        this.module = module;
    }

    public QuizQuestion current() {
        if (isFinished()) {
            throw new IllegalStateException("The quiz is finished");
        }
        return module.questions.get(index);
    }

    /** Records the choice and returns whether it was right. Each question can be answered once. */
    public boolean answer(int choice) {
        QuizQuestion question = current();
        if (answered) {
            throw new IllegalStateException("This question was already answered");
        }
        if (choice < 0 || choice >= question.options.size()) {
            throw new IllegalArgumentException("No option " + choice);
        }
        answered = true;
        boolean correct = choice == question.correctIndex;
        if (correct) {
            score++;
        }
        return correct;
    }

    public boolean isAnswered() {
        return answered;
    }

    public boolean isOnLastQuestion() {
        return index == module.questions.size() - 1;
    }

    public void next() {
        if (!answered) {
            throw new IllegalStateException("Answer the question before moving on");
        }
        index++;
        answered = false;
    }

    public boolean isFinished() {
        return index >= module.questions.size();
    }

    public int score() {
        return score;
    }

    public int total() {
        return module.questions.size();
    }

    /** 1-based number of the current question, capped at the total once finished. */
    public int position() {
        return Math.min(index + 1, total());
    }
}
