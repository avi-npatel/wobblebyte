package com.wobblebyte.app.data;

import com.wobblebyte.app.crypto.VaultCrypto;

/** One row of the vault. The site name is readable; the username and password are sealed. */
public final class Credential {
    public final long id;
    public final String site;
    public final VaultCrypto.Sealed sealed;
    public final long createdAt;

    public Credential(long id, String site, VaultCrypto.Sealed sealed, long createdAt) {
        this.id = id;
        this.site = site;
        this.sealed = sealed;
        this.createdAt = createdAt;
    }
}
