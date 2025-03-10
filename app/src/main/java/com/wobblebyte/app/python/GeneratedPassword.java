package com.wobblebyte.app.python;

/** A password from the Python generator plus the figures shown under it. */
public final class GeneratedPassword {
    public final String password;
    public final double entropyBits;
    public final String strength;
    /** Time for the whole Java to Python to Java call, in milliseconds. */
    public final double roundTripMs;
    /** Time spent inside the Python function itself, in milliseconds. */
    public final double pythonMs;

    public GeneratedPassword(String password, double entropyBits, String strength,
                             double roundTripMs, double pythonMs) {
        this.password = password;
        this.entropyBits = entropyBits;
        this.strength = strength;
        this.roundTripMs = roundTripMs;
        this.pythonMs = pythonMs;
    }
}
