package com.wobblebyte.app.ui;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;

/** Copies a password and wipes the clipboard again after 30 seconds. */
public final class ClipboardHelper {
    private static final long CLEAR_AFTER_MS = 30_000;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private ClipboardHelper() {
    }

    public static void copy(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText(label, text);
        if (Build.VERSION.SDK_INT >= 33) {
            // Tells Android to hide the value in the clipboard preview.
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true);
            clip.getDescription().setExtras(extras);
        }
        clipboard.setPrimaryClip(clip);

        // Android 10 and newer only lets the foreground app read the clipboard, and
        // the usual next step is pasting into another app, so the check "is it still
        // our text?" isn't possible. Clearing unconditionally is the trade-off.
        HANDLER.removeCallbacksAndMessages(null);
        HANDLER.postDelayed(clipboard::clearPrimaryClip, CLEAR_AFTER_MS);
    }
}
