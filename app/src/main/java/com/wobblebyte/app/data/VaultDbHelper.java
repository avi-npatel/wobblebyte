package com.wobblebyte.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wobblebyte.app.crypto.VaultCrypto;

import java.util.ArrayList;
import java.util.List;

/** SQLite storage for the vault. Everything sensitive arrives here already encrypted. */
public final class VaultDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "vault.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "credentials";
    private static final String COL_ID = "_id";
    private static final String COL_SITE = "site";
    private static final String COL_IV = "iv";
    private static final String COL_CIPHER = "cipher_text";
    private static final String COL_CREATED = "created_at";

    public VaultDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SITE + " TEXT NOT NULL, "
                + COL_IV + " BLOB NOT NULL, "
                + COL_CIPHER + " BLOB NOT NULL, "
                + COL_CREATED + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the only schema so far.
    }

    public long insert(String site, VaultCrypto.Sealed sealed) {
        ContentValues values = new ContentValues();
        values.put(COL_SITE, site);
        values.put(COL_IV, sealed.iv);
        values.put(COL_CIPHER, sealed.cipherText);
        values.put(COL_CREATED, System.currentTimeMillis());
        return getWritableDatabase().insertOrThrow(TABLE, null, values);
    }

    public List<Credential> all() {
        List<Credential> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE, null, null, null, null, null, COL_SITE + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                result.add(new Credential(
                        c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_SITE)),
                        new VaultCrypto.Sealed(
                                c.getBlob(c.getColumnIndexOrThrow(COL_IV)),
                                c.getBlob(c.getColumnIndexOrThrow(COL_CIPHER))),
                        c.getLong(c.getColumnIndexOrThrow(COL_CREATED))));
            }
        }
        return result;
    }

    public int delete(long id) {
        return getWritableDatabase().delete(TABLE, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public long count() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE);
    }
}
