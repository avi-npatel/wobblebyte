package com.wobblebyte.app.quiz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Turns a module's attempt history into the figures the quiz screens show. */
public final class ScoreStats {

    private ScoreStats() {
    }

    public static final class Summary {
        public final int attempts;
        public final int firstPercent;
        public final int latestPercent;
        public final int bestPercent;

        Summary(int attempts, int firstPercent, int latestPercent, int bestPercent) {
            this.attempts = attempts;
            this.firstPercent = firstPercent;
            this.latestPercent = latestPercent;
            this.bestPercent = bestPercent;
        }

        /** Percentage points gained (or lost) between the first and latest attempt. */
        public int deltaPoints() {
            return latestPercent - firstPercent;
        }
    }

    public static Summary summarize(List<ScoreEntry> entries) {
        if (entries.isEmpty()) {
            return new Summary(0, 0, 0, 0);
        }

        List<ScoreEntry> ordered = new ArrayList<>(entries);
        ordered.sort(Comparator.comparingLong(e -> e.timestampMs));

        int best = 0;
        for (ScoreEntry entry : ordered) {
            best = Math.max(best, entry.percent());
        }
        return new Summary(
                ordered.size(),
                ordered.get(0).percent(),
                ordered.get(ordered.size() - 1).percent(),
                best);
    }
}
