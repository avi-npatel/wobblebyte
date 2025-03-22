package com.wobblebyte.app;

import com.chaquo.python.android.PyApplication;
import com.wobblebyte.app.python.PasswordBridge;

/** Starts the embedded Python interpreter (PyApplication does that) and warms it up. */
public class WobblyApp extends PyApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // Importing the generator once, off the main thread, means the first tap
        // on Generate doesn't pay the module import cost.
        new Thread(PasswordBridge::warmUp, "python-warmup").start();
    }
}
