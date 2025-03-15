package com.wobblebyte.app.data;

import android.content.Context;

import com.wobblebyte.app.crypto.VaultCrypto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * The vault the screens talk to. It seals the username and password together as
 * one AES-GCM blob; only the site name is stored in the clear so the list can be
 * shown without unlocking anything.
 */
public final class Vault {
    private static Vault instance;

    private final VaultDbHelper db;

    private Vault(Context context) {
        this.db = new VaultDbHelper(context.getApplicationContext());
    }

    public static synchronized Vault get(Context context) {
        if (instance == null) {
            instance = new Vault(context);
        }
        return instance;
    }

    /** A decrypted credential. Hold it only as long as it is on screen. */
    public static final class Entry {
        public final String username;
        public final String password;

        Entry(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public long add(String site, String username, String password)
            throws GeneralSecurityException, IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("u", username);
        payload.put("p", password);
        byte[] plain = payload.toString().getBytes(StandardCharsets.UTF_8);
        try {
            return db.insert(site, VaultCrypto.seal(plain));
        } finally {
            Arrays.fill(plain, (byte) 0);
        }
    }

    public Entry reveal(Credential credential)
            throws GeneralSecurityException, IOException, JSONException {
        byte[] plain = VaultCrypto.open(credential.sealed);
        try {
            JSONObject payload = new JSONObject(new String(plain, StandardCharsets.UTF_8));
            return new Entry(payload.getString("u"), payload.getString("p"));
        } finally {
            Arrays.fill(plain, (byte) 0);
        }
    }

    public List<Credential> list() {
        return db.all();
    }

    public void delete(long id) {
        db.delete(id);
    }

    public long count() {
        return db.count();
    }
}
