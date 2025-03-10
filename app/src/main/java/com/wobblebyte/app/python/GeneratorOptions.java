package com.wobblebyte.app.python;

/** What the user asked for on the Generate screen. */
public final class GeneratorOptions {
    public int length = 16;
    public boolean lower = true;
    public boolean upper = true;
    public boolean digits = true;
    public boolean symbols = false;
    public boolean excludeAmbiguous = false;

    public boolean hasAnyCharacterType() {
        return lower || upper || digits || symbols;
    }
}
