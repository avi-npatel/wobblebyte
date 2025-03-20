package com.wobblebyte.app.quiz;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps every quiz attempt so improvement can be shown over time. Scores are not
 * secret, so they live in plain SharedPreferences rather than the encrypted vault.
 */
public final class ScoreStore {
    private static final String PREFS = "quiz_scores";
    private static final String KEY_ENTRIES = "entries";

    private final SharedPreferences prefs;

    public ScoreStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void record(ScoreEntry entry) {
        try {
            JSONArray all = new JSONArray(prefs.getString(KEY_ENTRIES, "[]"));
            all.put(entry.toJson());
            prefs.edit().putString(KEY_ENTRIES, all.toString()).apply();
        } catch (JSONException e) {
            throw new IllegalStateException("Could not save the quiz score", e);
        }
    }

    /** Attempts for one module, oldest first. */
    public List<ScoreEntry> forModule(String moduleId) {
        List<ScoreEntry> result = new ArrayList<>();
        try {
            JSONArray all = new JSONArray(prefs.getString(KEY_ENTRIES, "[]"));
            for (int i = 0; i < all.length(); i++) {
                JSONObject json = all.getJSONObject(i);
                if (moduleId.equals(json.getString("module"))) {
                    result.add(ScoreEntry.fromJson(json));
                }
            }
        } catch (JSONException e) {
            // A corrupt history is not worth crashing for; start fresh.
            prefs.edit().remove(KEY_ENTRIES).apply();
            result.clear();
        }
        return result;
    }
}
