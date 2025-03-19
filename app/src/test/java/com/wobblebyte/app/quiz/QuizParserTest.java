package com.wobblebyte.app.quiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizParserTest {

    private static List<QuizModule> bundledQuiz() throws IOException, JSONException {
        // Gradle runs unit tests with the module directory (app/) as the working directory.
        String json = new String(
                Files.readAllBytes(Paths.get("src/main/assets/quiz.json")), StandardCharsets.UTF_8);
        return QuizParser.parse(json);
    }

    @Test
    public void bundledQuizHasFourModulesOfSixQuestions() throws Exception {
        List<QuizModule> modules = bundledQuiz();
        assertEquals(4, modules.size());
        for (QuizModule module : modules) {
            assertEquals(module.id, 6, module.questions.size());
        }
    }

    @Test
    public void everyQuestionHasFourDistinctOptionsAndAnExplanation() throws Exception {
        for (QuizModule module : bundledQuiz()) {
            for (QuizQuestion question : module.questions) {
                assertEquals(question.prompt, 4, question.options.size());
                assertEquals(question.prompt, 4, new HashSet<>(question.options).size());
                assertFalse(question.explanation.trim().isEmpty());
            }
        }
    }

    @Test
    public void moduleIdsAndPromptsAreUnique() throws Exception {
        Set<String> ids = new HashSet<>();
        Set<String> prompts = new HashSet<>();
        for (QuizModule module : bundledQuiz()) {
            assertTrue("duplicate module " + module.id, ids.add(module.id));
            for (QuizQuestion question : module.questions) {
                assertTrue("duplicate prompt " + question.prompt, prompts.add(question.prompt));
            }
        }
    }

    @Test
    public void correctAnswersAreNotAllInTheSamePosition() throws Exception {
        Set<Integer> positions = new HashSet<>();
        for (QuizModule module : bundledQuiz()) {
            for (QuizQuestion question : module.questions) {
                positions.add(question.correctIndex);
            }
        }
        assertEquals(4, positions.size());
    }

    @Test
    public void rejectsACorrectIndexOutsideTheOptions() {
        String json = "{\"modules\":[{\"id\":\"x\",\"title\":\"X\",\"summary\":\"s\",\"questions\":["
                + "{\"prompt\":\"p\",\"options\":[\"a\",\"b\"],\"correctIndex\":2,\"explanation\":\"e\"}]}]}";
        try {
            QuizParser.parse(json);
            fail("expected a JSONException");
        } catch (JSONException expected) {
            assertTrue(expected.getMessage().contains("correctIndex"));
        }
    }

    @Test
    public void rejectsAQuestionWithOneOption() {
        String json = "{\"modules\":[{\"id\":\"x\",\"title\":\"X\",\"summary\":\"s\",\"questions\":["
                + "{\"prompt\":\"p\",\"options\":[\"a\"],\"correctIndex\":0,\"explanation\":\"e\"}]}]}";
        try {
            QuizParser.parse(json);
            fail("expected a JSONException");
        } catch (JSONException expected) {
            assertTrue(expected.getMessage().contains("two options"));
        }
    }
}
