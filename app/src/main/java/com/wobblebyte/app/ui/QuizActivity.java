package com.wobblebyte.app.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.wobblebyte.app.R;
import com.wobblebyte.app.quiz.QuizModule;
import com.wobblebyte.app.quiz.QuizQuestion;
import com.wobblebyte.app.quiz.QuizRepository;
import com.wobblebyte.app.quiz.QuizSession;
import com.wobblebyte.app.quiz.ScoreEntry;
import com.wobblebyte.app.quiz.ScoreStats;
import com.wobblebyte.app.quiz.ScoreStore;

import org.json.JSONException;

import java.io.IOException;

/** Runs one quiz module: a question at a time, feedback after each answer, then a results screen. */
public class QuizActivity extends AppCompatActivity {
    public static final String EXTRA_MODULE_ID = "module_id";

    private QuizModule module;
    private QuizSession session;
    private ScoreStore scores;

    private View quizGroup;
    private View resultGroup;
    private TextView progressText;
    private LinearProgressIndicator progressBar;
    private TextView questionText;
    private LinearLayout optionsContainer;
    private TextView feedbackText;
    private MaterialButton nextButton;
    private TextView resultTitle;
    private TextView resultHistory;
    private TextView resultChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        module = findModule(getIntent().getStringExtra(EXTRA_MODULE_ID));
        if (module == null) {
            finish();
            return;
        }
        scores = new ScoreStore(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(module.title);
        toolbar.setNavigationOnClickListener(v -> finish());

        quizGroup = findViewById(R.id.quiz_group);
        resultGroup = findViewById(R.id.result_group);
        progressText = findViewById(R.id.progress_text);
        progressBar = findViewById(R.id.progress_bar);
        questionText = findViewById(R.id.question_text);
        optionsContainer = findViewById(R.id.options_container);
        feedbackText = findViewById(R.id.feedback_text);
        nextButton = findViewById(R.id.button_next);
        resultTitle = findViewById(R.id.result_title);
        resultHistory = findViewById(R.id.result_history);
        resultChange = findViewById(R.id.result_change);

        nextButton.setOnClickListener(v -> onNext());
        findViewById(R.id.button_retake).setOnClickListener(v -> start());
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        start();
    }

    private QuizModule findModule(String id) {
        if (id == null) {
            return null;
        }
        try {
            for (QuizModule candidate : QuizRepository.load(this)) {
                if (candidate.id.equals(id)) {
                    return candidate;
                }
            }
        } catch (IOException | JSONException e) {
            // Falls through to null; the screen closes.
        }
        return null;
    }

    private void start() {
        session = new QuizSession(module);
        resultGroup.setVisibility(View.GONE);
        quizGroup.setVisibility(View.VISIBLE);
        showQuestion();
    }

    private void showQuestion() {
        QuizQuestion question = session.current();

        progressText.setText(getString(R.string.quiz_progress, session.position(), session.total()));
        progressBar.setMax(session.total());
        progressBar.setProgressCompat(session.position() - 1, true);
        questionText.setText(question.prompt);
        feedbackText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        optionsContainer.removeAllViews();
        for (int i = 0; i < question.options.size(); i++) {
            MaterialButton option = (MaterialButton) getLayoutInflater()
                    .inflate(R.layout.item_quiz_option, optionsContainer, false);
            option.setText(question.options.get(i));
            final int choice = i;
            option.setOnClickListener(v -> onAnswer(choice));
            optionsContainer.addView(option);
        }
    }

    private void onAnswer(int choice) {
        if (session.isAnswered()) {
            return;
        }
        QuizQuestion question = session.current();
        boolean correct = session.answer(choice);

        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            MaterialButton option = (MaterialButton) optionsContainer.getChildAt(i);
            option.setClickable(false);
            if (i == question.correctIndex) {
                tint(option, R.color.correct_bg);
            } else if (i == choice) {
                tint(option, R.color.wrong_bg);
            }
        }

        String verdict = getString(correct ? R.string.quiz_correct : R.string.quiz_incorrect);
        feedbackText.setText(verdict + " " + question.explanation);
        feedbackText.setVisibility(View.VISIBLE);

        nextButton.setText(session.isOnLastQuestion() ? R.string.quiz_finish : R.string.quiz_next);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void onNext() {
        session.next();
        if (session.isFinished()) {
            showResults();
        } else {
            showQuestion();
        }
    }

    private void showResults() {
        scores.record(new ScoreEntry(
                module.id, session.score(), session.total(), System.currentTimeMillis()));
        ScoreStats.Summary summary = ScoreStats.summarize(scores.forModule(module.id));

        resultTitle.setText(getString(R.string.quiz_result_title, session.score(), session.total()));
        resultHistory.setText(ScoreText.history(this, summary));

        String change = ScoreText.change(this, summary);
        resultChange.setText(change);
        resultChange.setVisibility(change.isEmpty() ? View.GONE : View.VISIBLE);

        quizGroup.setVisibility(View.GONE);
        resultGroup.setVisibility(View.VISIBLE);
    }

    private void tint(MaterialButton button, @ColorRes int color) {
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
    }
}
