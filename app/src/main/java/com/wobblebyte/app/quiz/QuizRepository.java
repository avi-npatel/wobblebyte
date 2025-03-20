package com.wobblebyte.app.quiz;

import android.content.Context;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Loads the quiz content bundled in the APK's assets folder. */
public final class QuizRepository {
    private static final String ASSET = "quiz.json";

    private QuizRepository() {
    }

    public static List<QuizModule> load(Context context) throws IOException, JSONException {
        try (InputStream in = context.getAssets().open(ASSET)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return QuizParser.parse(new String(out.toByteArray(), StandardCharsets.UTF_8));
        }
    }
}
