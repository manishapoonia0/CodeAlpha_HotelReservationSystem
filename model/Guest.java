package model;

import java.io.Serializable;

/**
 * Represents a hotel guest with personal and contact details.
 */
public class Guest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String guestId;
    private final String name;
    private final String email;
    private final String phone;
    private final String idProof;   // e.g. Aadhaar / Passport number

    public Guest(String guestId, String name, String email, String phone, String idProof) {
        this.guestId = guestId;
        this.name    = name;
        this.email   = email;
        this.phone   = phone;
        this.idProof = idProof;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getGuestId() { return guestId; }
    public String getName()    { return name;    }
    public String getEmail()   { return email;   }
    public String getPhone()   { return phone;   }
    public String getIdProof() { return idProof; }

    @Override
    public String toString() {
        return String.format("Guest[%s] %s <%s> | Phone: %s", guestId, name, email, phone);
    }
}
