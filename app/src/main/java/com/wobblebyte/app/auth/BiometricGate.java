package com.wobblebyte.app.auth;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.wobblebyte.app.R;

/** Shows the system biometric prompt (fingerprint or face, with the device PIN as fallback). */
public final class BiometricGate {
    private static final int AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

    private BiometricGate() {
    }

    public interface Callback {
        void onSuccess();

        /** @param errorCode a BiometricPrompt.ERROR_* constant */
        void onFailure(int errorCode, CharSequence message);
    }

    /** True when the phone has a screen lock the vault key can sit behind. */
    public static boolean isAvailable(Context context) {
        return BiometricManager.from(context).canAuthenticate(AUTHENTICATORS)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void prompt(FragmentActivity activity, Callback callback) {
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.unlock_title))
                .setSubtitle(activity.getString(R.string.unlock_subtitle))
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build();

        BiometricPrompt prompt = new BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        callback.onFailure(errorCode, errString);
                    }
                });
        prompt.authenticate(info);
    }
}
