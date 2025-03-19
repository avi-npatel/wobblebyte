package com.wobblebyte.app.quiz;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ScoreStatsTest {

    @Test
    public void noAttemptsGivesAnEmptySummary() {
        ScoreStats.Summary summary = ScoreStats.summarize(Collections.emptyList());
        assertEquals(0, summary.attempts);
        assertEquals(0, summary.deltaPoints());
    }

    @Test
    public void reportsFirstLatestBestAndTheChange() {
        ScoreStats.Summary summary = ScoreStats.summarize(Arrays.asList(
                new ScoreEntry("m", 3, 6, 1000L),
                new ScoreEntry("m", 6, 6, 2000L),
                new ScoreEntry("m", 5, 6, 3000L)));

        assertEquals(3, summary.attempts);
        assertEquals(50, summary.firstPercent);
        assertEquals(83, summary.latestPercent);
        assertEquals(100, summary.bestPercent);
        assertEquals(33, summary.deltaPoints());
    }

    @Test
    public void ordersByTimestampNotByInsertion() {
        ScoreStats.Summary summary = ScoreStats.summarize(Arrays.asList(
                new ScoreEntry("m", 6, 6, 3000L),
                new ScoreEntry("m", 3, 6, 1000L)));

        assertEquals(50, summary.firstPercent);
        assertEquals(100, summary.latestPercent);
    }

    @Test
    public void aDropShowsAsANegativeChange() {
        ScoreStats.Summary summary = ScoreStats.summarize(Arrays.asList(
                new ScoreEntry("m", 5, 6, 1000L),
                new ScoreEntry("m", 4, 6, 2000L)));
        // 5/6 rounds to 83 and 4/6 rounds to 67
        assertEquals(-16, summary.deltaPoints());
    }

    @Test
    public void percentRoundsToTheNearestWholeNumber() {
        assertEquals(17, new ScoreEntry("m", 1, 6, 0L).percent());
        assertEquals(67, new ScoreEntry("m", 4, 6, 0L).percent());
        assertEquals(0, new ScoreEntry("m", 0, 0, 0L).percent());
    }

    @Test
    public void entriesSurviveAJsonRoundTrip() throws Exception {
        ScoreEntry original = new ScoreEntry("phishing", 4, 6, 1_700_000_000_000L);
        JSONObject json = original.toJson();
        ScoreEntry copy = ScoreEntry.fromJson(new JSONObject(json.toString()));

        assertEquals(original.moduleId, copy.moduleId);
        assertEquals(original.score, copy.score);
        assertEquals(original.total, copy.total);
        assertEquals(original.timestampMs, copy.timestampMs);
    }
}
