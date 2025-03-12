package com.wobblebyte.app.crypto;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.StrongBoxUnavailableException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * The AES-256 key that protects every saved credential.
 *
 * The key is generated inside the Android Keystore (in the StrongBox chip when
 * the phone has one), so the key bytes never enter the app's memory and cannot be
 * copied off the device. It is also locked behind the screen lock: Keystore
 * refuses to use it unless the user has authenticated in the last
 * {@link #AUTH_WINDOW_SECONDS} seconds with a strong biometric or the device PIN.
 */
public final class VaultKey {
    private static final String PROVIDER = "AndroidKeyStore";
    private static final String ALIAS = "wobblebyte_vault_key_v1";

    /** How long one successful unlock keeps the key usable. */
    public static final int AUTH_WINDOW_SECONDS = 30;

    private VaultKey() {
    }

    public static SecretKey getOrCreate() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(PROVIDER);
        keyStore.load(null);

        Key existing = keyStore.getKey(ALIAS, null);
        if (existing instanceof SecretKey) {
            return (SecretKey) existing;
        }

        try {
            return create(true);
        } catch (StrongBoxUnavailableException noStrongBox) {
            return create(false);
        }
    }

    private static SecretKey create(boolean strongBox) throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                        AUTH_WINDOW_SECONDS,
                        KeyProperties.AUTH_BIOMETRIC_STRONG | KeyProperties.AUTH_DEVICE_CREDENTIAL)
                // The PIN already unlocks the key, so invalidating it when a
                // fingerprint is added would only risk locking the user out.
                .setInvalidatedByBiometricEnrollment(false)
                .setIsStrongBoxBacked(strongBox)
                .build();
        generator.init(spec);
        return generator.generateKey();
    }
}
