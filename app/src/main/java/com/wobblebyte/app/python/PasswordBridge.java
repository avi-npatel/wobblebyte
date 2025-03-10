package com.wobblebyte.app.python;

import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The only place the Java UI touches Python. It calls
 * wobblebyte.generator.generate_with_meta, which lives in src/main/python.
 */
public final class PasswordBridge {
    private static final String TAG = "PasswordBridge";
    private static final String MODULE = "wobblebyte.generator";

    private PasswordBridge() {
    }

    /** Imports the module so the first real call is fast. Safe to call from any thread. */
    public static void warmUp() {
        try {
            Python.getInstance().getModule(MODULE);
        } catch (RuntimeException e) {
            Log.w(TAG, "Python warm-up failed", e);
        }
    }

    /**
     * Generates a password.
     *
     * @throws com.chaquo.python.PyException if Python rejects the options
     *                                        (for example a length outside 8 to 64)
     */
    public static GeneratedPassword generate(GeneratorOptions options) {
        long started = System.nanoTime();
        PyObject module = Python.getInstance().getModule(MODULE);
        String json = module.callAttr(
                "generate_with_meta",
                options.length,
                options.lower,
                options.upper,
                options.digits,
                options.symbols,
                options.excludeAmbiguous).toString();
        double roundTripMs = (System.nanoTime() - started) / 1_000_000.0;

        try {
            JSONObject parsed = new JSONObject(json);
            return new GeneratedPassword(
                    parsed.getString("password"),
                    parsed.getDouble("entropy_bits"),
                    parsed.getString("strength"),
                    roundTripMs,
                    parsed.getDouble("python_ms"));
        } catch (JSONException e) {
            throw new IllegalStateException("Unexpected reply from the Python generator", e);
        }
    }
}
