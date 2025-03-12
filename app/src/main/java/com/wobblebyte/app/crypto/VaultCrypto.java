package com.wobblebyte.app.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

/**
 * AES-256-GCM encryption with the Keystore-held {@link VaultKey}.
 *
 * GCM both encrypts and authenticates, so a tampered database row fails to
 * decrypt instead of returning garbage. Keystore picks a fresh random 12-byte IV
 * for every encryption; the IV is stored next to the ciphertext.
 *
 * Both methods throw android.security.keystore.UserNotAuthenticatedException when
 * the unlock window has expired. Callers catch that and show the biometric prompt.
 */
public final class VaultCrypto {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_BITS = 128;

    private VaultCrypto() {
    }

    /** Ciphertext and the IV needed to decrypt it. */
    public static final class Sealed {
        public final byte[] iv;
        public final byte[] cipherText;

        public Sealed(byte[] iv, byte[] cipherText) {
            this.iv = iv;
            this.cipherText = cipherText;
        }
    }

    public static Sealed seal(byte[] plain) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, VaultKey.getOrCreate());
        byte[] cipherText = cipher.doFinal(plain);
        return new Sealed(cipher.getIV(), cipherText);
    }

    public static byte[] open(Sealed sealed) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, VaultKey.getOrCreate(), new GCMParameterSpec(TAG_BITS, sealed.iv));
        return cipher.doFinal(sealed.cipherText);
    }
}
