package com.wobblebyte.app.quiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

public class QuizSessionTest {

    private static QuizModule threeQuestionModule() {
        QuizQuestion first = new QuizQuestion("q1", Arrays.asList("a", "b", "c", "d"), 2, "e1");
        QuizQuestion second = new QuizQuestion("q2", Arrays.asList("a", "b", "c", "d"), 0, "e2");
        QuizQuestion third = new QuizQuestion("q3", Arrays.asList("a", "b", "c", "d"), 3, "e3");
        return new QuizModule("m", "Module", "summary", Arrays.asList(first, second, third));
    }

    @Test
    public void scoresCorrectAnswersOnly() {
        QuizSession session = new QuizSession(threeQuestionModule());

        assertTrue(session.answer(2));
        session.next();
        assertFalse(session.answer(3));
        session.next();
        assertTrue(session.answer(3));
        session.next();

        assertTrue(session.isFinished());
        assertEquals(2, session.score());
        assertEquals(3, session.total());
    }

    @Test
    public void positionAdvancesAndCapsAtTheTotal() {
        QuizSession session = new QuizSession(threeQuestionModule());
        assertEquals(1, session.position());
        session.answer(0);
        session.next();
        assertEquals(2, session.position());
        session.answer(0);
        session.next();
        assertTrue(session.isOnLastQuestion());
        session.answer(0);
        session.next();
        assertEquals(3, session.position());
    }

    @Test(expected = IllegalStateException.class)
    public void aQuestionCanOnlyBeAnsweredOnce() {
        QuizSession session = new QuizSession(threeQuestionModule());
        session.answer(2);
        session.answer(2);
    }

    @Test(expected = IllegalStateException.class)
    public void cannotSkipAQuestion() {
        new QuizSession(threeQuestionModule()).next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAnOptionThatDoesNotExist() {
        new QuizSession(threeQuestionModule()).answer(4);
    }

    @Test(expected = IllegalStateException.class)
    public void noCurrentQuestionAfterTheEnd() {
        QuizSession session = new QuizSession(threeQuestionModule());
        for (int i = 0; i < 3; i++) {
            session.answer(0);
            session.next();
        }
        session.current();
    }
}
