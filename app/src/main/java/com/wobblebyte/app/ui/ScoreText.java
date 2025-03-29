package com.wobblebyte.app.ui;

import android.content.Context;

import com.wobblebyte.app.R;
import com.wobblebyte.app.quiz.ScoreStats;

/** Words for a module's score history, shared by the quiz list and the results screen. */
final class ScoreText {

    private ScoreText() {
    }

    static String history(Context context, ScoreStats.Summary summary) {
        if (summary.attempts == 0) {
            return context.getString(R.string.quiz_not_taken);
        }
        if (summary.attempts == 1) {
            return context.getString(R.string.quiz_first_attempt, summary.firstPercent);
        }
        return context.getString(
                R.string.quiz_history_line,
                summary.firstPercent, summary.latestPercent, summary.bestPercent);
    }

    /** How the latest attempt compares with the first. Empty until there are two attempts. */
    static String change(Context context, ScoreStats.Summary summary) {
        if (summary.attempts < 2) {
            return "";
        }
        int delta = summary.deltaPoints();
        if (delta > 0) {
            return context.getString(R.string.quiz_delta_up, delta);
        }
        if (delta < 0) {
            return context.getString(R.string.quiz_delta_down, -delta);
        }
        return context.getString(R.string.quiz_delta_same);
    }
}
