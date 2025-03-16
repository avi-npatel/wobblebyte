package com.wobblebyte.app.quiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads assets/quiz.json. It rejects malformed questions (too few options, a
 * correct answer that points outside the list) so a typo in the content fails
 * the unit tests instead of crashing the quiz screen.
 */
public final class QuizParser {

    private QuizParser() {
    }

    public static List<QuizModule> parse(String json) throws JSONException {
        JSONArray moduleArray = new JSONObject(json).getJSONArray("modules");
        List<QuizModule> modules = new ArrayList<>();

        for (int m = 0; m < moduleArray.length(); m++) {
            JSONObject moduleJson = moduleArray.getJSONObject(m);
            String id = moduleJson.getString("id");

            JSONArray questionArray = moduleJson.getJSONArray("questions");
            List<QuizQuestion> questions = new ArrayList<>();
            for (int q = 0; q < questionArray.length(); q++) {
                questions.add(parseQuestion(id, q, questionArray.getJSONObject(q)));
            }
            if (questions.isEmpty()) {
                throw new JSONException("Module " + id + " has no questions");
            }

            modules.add(new QuizModule(
                    id,
                    moduleJson.getString("title"),
                    moduleJson.getString("summary"),
                    questions));
        }
        return modules;
    }

    private static QuizQuestion parseQuestion(String moduleId, int index, JSONObject json)
            throws JSONException {
        JSONArray optionArray = json.getJSONArray("options");
        List<String> options = new ArrayList<>();
        for (int i = 0; i < optionArray.length(); i++) {
            options.add(optionArray.getString(i));
        }

        int correct = json.getInt("correctIndex");
        String where = moduleId + " question " + (index + 1);
        if (options.size() < 2) {
            throw new JSONException(where + " needs at least two options");
        }
        if (correct < 0 || correct >= options.size()) {
            throw new JSONException(where + " has a correctIndex outside its options");
        }

        return new QuizQuestion(
                json.getString("prompt"),
                options,
                correct,
                json.getString("explanation"));
    }
}
