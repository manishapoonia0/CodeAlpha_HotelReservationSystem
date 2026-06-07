package util;

import model.Booking;
import model.Room;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles file-based persistence for bookings and room availability.
 *
 * Two files are maintained:
 *   data/bookings.dat   — serialized list of all Booking objects
 *   data/rooms.dat      — serialized map of roomNumber → availability
 */
public class FileIO {

    private static final String DATA_DIR      = "data";
    private static final String BOOKINGS_FILE = DATA_DIR + "/bookings.dat";
    private static final String ROOMS_FILE    = DATA_DIR + "/rooms.dat";

    static {
        // Ensure data directory exists on first use
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (IOException e) { System.err.println("Could not create data directory: " + e.getMessage()); }
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static List<Booking> loadBookings() {
        File f = new File(BOOKINGS_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<Booking>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Warning: could not load bookings — " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveBookings(List<Booking> bookings) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            oos.writeObject(bookings);
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    // ── Room availability ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static Map<String, Boolean> loadRoomAvailability() {
        File f = new File(ROOMS_FILE);
        if (!f.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Map<String, Boolean>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Warning: could not load room data — " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void saveRoomAvailability(List<Room> rooms) {
        Map<String, Boolean> map = new HashMap<>();
        for (Room r : rooms) map.put(r.getRoomNumber(), r.isAvailable());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(map);
        } catch (IOException e) {
            System.err.println("Error saving room availability: " + e.getMessage());
        }
    }

    // ── Text receipt export ───────────────────────────────────────────────────

    public static void exportReceiptToFile(Booking booking) {
        String filename = DATA_DIR + "/receipt_" + booking.getBookingId() + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("GRAND PALACE HOTEL — BOOKING RECEIPT");
            pw.println("=".repeat(50));
            pw.printf("Booking ID    : %s%n",  booking.getBookingId());
            pw.printf("Guest         : %s%n",  booking.getGuest().getName());
            pw.printf("Email         : %s%n",  booking.getGuest().getEmail());
            pw.printf("Room          : %s (%s)%n",
                    booking.getRoom().getRoomNumber(),
                    booking.getRoom().getType().getDisplayName());
            pw.printf("Check-in      : %s%n",  booking.getCheckIn());
            pw.printf("Check-out     : %s%n",  booking.getCheckOut());
            pw.printf("Nights        : %d%n",  booking.getNights());
            pw.println("-".repeat(50));
            pw.printf("Subtotal      : Rs %.2f%n", booking.getSubtotal());
            pw.printf("GST (18%%)     : Rs %.2f%n", booking.getTax());
            pw.printf("TOTAL         : Rs %.2f%n",  booking.getTotalAmount());
            pw.println("=".repeat(50));
            pw.printf("Status        : %s%n",  booking.getStatus());
            System.out.printf("  ✓ Receipt exported to %s%n", filename);
        } catch (IOException e) {
            System.err.println("Could not export receipt: " + e.getMessage());
        }
    }
}
