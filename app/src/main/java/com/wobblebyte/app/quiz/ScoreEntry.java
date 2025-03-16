package com.wobblebyte.app.quiz;

import org.json.JSONException;
import org.json.JSONObject;

/** The result of one finished attempt at a module. */
public final class ScoreEntry {
    public final String moduleId;
    public final int score;
    public final int total;
    public final long timestampMs;

    public ScoreEntry(String moduleId, int score, int total, long timestampMs) {
        this.moduleId = moduleId;
        this.score = score;
        this.total = total;
        this.timestampMs = timestampMs;
    }

    public int percent() {
        return total == 0 ? 0 : Math.round(100f * score / total);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("module", moduleId);
        json.put("score", score);
        json.put("total", total);
        json.put("at", timestampMs);
        return json;
    }

    public static ScoreEntry fromJson(JSONObject json) throws JSONException {
        return new ScoreEntry(
                json.getString("module"),
                json.getInt("score"),
                json.getInt("total"),
                json.getLong("at"));
    }
}
