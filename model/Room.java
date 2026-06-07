package model;

import java.io.Serializable;

/**
 * Represents a hotel room with its type, number, floor, and availability.
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String roomNumber;
    private final RoomType type;
    private final int floor;
    private boolean available;

    public Room(String roomNumber, RoomType type, int floor) {
        this.roomNumber = roomNumber;
        this.type       = type;
        this.floor      = floor;
        this.available  = true;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String   getRoomNumber() { return roomNumber; }
    public RoomType getType()       { return type;       }
    public int      getFloor()      { return floor;      }
    public boolean  isAvailable()   { return available;  }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("Room %-5s | %-20s | Floor %d | ₹%,.2f/night | %s",
                roomNumber,
                type.getDisplayName(),
                floor,
                type.getPricePerNight(),
                available ? "AVAILABLE" : "BOOKED");
    }
}
