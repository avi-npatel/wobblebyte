package com.wobblebyte.app.auth;

import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.UserNotAuthenticatedException;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.wobblebyte.app.R;

/**
 * Runs something that needs the vault key. If Keystore says the unlock window has
 * expired, it shows the biometric prompt and then runs the action again.
 */
public final class VaultAccess {

    private VaultAccess() {
    }

    public interface Action {
        void run() throws Exception;
    }

    public interface ErrorHandler {
        void onError(String message);
    }

    public static void withUnlock(FragmentActivity activity, Action action, ErrorHandler onError) {
        if (!BiometricGate.isAvailable(activity)) {
            onError.onError(activity.getString(R.string.error_no_screen_lock));
            return;
        }

        try {
            action.run();
            return;
        } catch (UserNotAuthenticatedException needsUnlock) {
            // Expected on first use and after the window expires; ask below.
        } catch (Exception e) {
            onError.onError(describe(activity, e));
            return;
        }

        BiometricGate.prompt(activity, new BiometricGate.Callback() {
            @Override
            public void onSuccess() {
                try {
                    action.run();
                } catch (Exception e) {
                    onError.onError(describe(activity, e));
                }
            }

            @Override
            public void onFailure(int errorCode, CharSequence message) {
                boolean cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.ERROR_CANCELED
                        || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON;
                if (!cancelled) {
                    onError.onError(message.toString());
                }
            }
        });
    }

    private static String describe(FragmentActivity activity, Exception e) {
        if (e instanceof KeyPermanentlyInvalidatedException) {
            return activity.getString(R.string.error_key_invalidated);
        }
        return activity.getString(R.string.error_generic, e.getClass().getSimpleName());
    }
}
