import model.*;
import service.BookingService;
import service.RoomService;
import util.IdGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ═══════════════════════════════════════════════════════
 *   GRAND PALACE HOTEL — Reservation Management System
 *   Console Application Entry Point
 * ═══════════════════════════════════════════════════════
 *
 * Run: javac -d out $(find . -name "*.java") && java -cp out HotelApp
 */
public class HotelApp {

    private static final Scanner sc = new Scanner(System.in);
    private static final RoomService    roomService    = new RoomService();
    private static final BookingService bookingService = new BookingService(roomService);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String DIVIDER = "═".repeat(60);
    private static final String LINE    = "─".repeat(60);

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        banner();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("  Select option").trim();
            System.out.println();
            switch (choice) {
                case "1"  -> searchRooms();
                case "2"  -> makeReservation();
                case "3"  -> viewBooking();
                case "4"  -> cancelReservation();
                case "5"  -> checkInGuest();
                case "6"  -> checkOutGuest();
                case "7"  -> bookingService.printAllBookings();
                case "8"  -> bookingService.printDashboard();
                case "9"  -> roomService.printRoomInventory();
                case "0"  -> { running = false; bye(); }
                default   -> System.out.println("  Invalid option. Please choose 0–9.");
            }
        }
        sc.close();
    }

    // ── Menu & Banner ─────────────────────────────────────────────────────────
    private static void banner() {
        System.out.println("\n" + DIVIDER);
        System.out.println("       ★  GRAND PALACE HOTEL  ★");
        System.out.println("       Reservation Management System");
        System.out.println(DIVIDER);
        bookingService.printDashboard();
        roomService.printRoomInventory();
    }

    private static void printMenu() {
        System.out.println("\n" + LINE);
        System.out.println("  MAIN MENU");
        System.out.println(LINE);
        System.out.println("  1. Search Available Rooms");
        System.out.println("  2. Make a Reservation");
        System.out.println("  3. View Booking Details");
        System.out.println("  4. Cancel Reservation");
        System.out.println("  5. Check-In Guest");
        System.out.println("  6. Check-Out Guest");
        System.out.println("  7. View All Bookings");
        System.out.println("  8. Dashboard / Revenue Report");
        System.out.println("  9. Room Inventory");
        System.out.println("  0. Exit");
        System.out.println(LINE);
    }

    // ── Feature: Search rooms ─────────────────────────────────────────────────
    private static void searchRooms() {
        System.out.println("  Room Type Filter:");
        System.out.println("  1. Standard    2. Deluxe    3. Suite    4. Presidential    5. All");
        String ch = prompt("  Choose").trim();
        RoomType filter = switch (ch) {
            case "1" -> RoomType.STANDARD;
            case "2" -> RoomType.DELUXE;
            case "3" -> RoomType.SUITE;
            case "4" -> RoomType.PRESIDENTIAL;
            default  -> null;
        };
        roomService.printAvailableRooms(filter);

        if (filter != null) {
            System.out.printf("  Amenities — %s: %s%n",
                    filter.getDisplayName(), filter.getAmenities());
        }
    }

    // ── Feature: Make reservation ──────────────────────────────────────────────
    private static void makeReservation() {
        System.out.println("  ── NEW RESERVATION ─────────────────────────────────────────────");

        // Guest details
        String name    = prompt("  Guest name");
        String email   = prompt("  Email");
        String phone   = prompt("  Phone");
        String idProof = prompt("  ID proof (e.g. Aadhaar/Passport)");
        Guest guest    = new Guest(IdGenerator.nextGuestId(), name, email, phone, idProof);

        // Room
        roomService.printAvailableRooms(null);
        String roomNum = prompt("  Enter room number");

        // Dates
        LocalDate checkIn  = promptDate("  Check-in  (dd-MM-yyyy)");
        LocalDate checkOut = promptDate("  Check-out (dd-MM-yyyy)");
        if (checkIn == null || checkOut == null) return;

        // Occupants
        int numGuests = promptInt("  Number of guests (1-4)", 1, 4);
        String special = prompt("  Special requests (or press Enter to skip)");

        // Payment
        System.out.println("\n  Payment Method:");
        System.out.println("  1. Cash   2. Credit Card   3. Debit Card   4. UPI   5. Net Banking");
        String pm = prompt("  Choose").trim();
        Payment.Method method = switch (pm) {
            case "2" -> Payment.Method.CREDIT_CARD;
            case "3" -> Payment.Method.DEBIT_CARD;
            case "4" -> Payment.Method.UPI;
            case "5" -> Payment.Method.NET_BANKING;
            default  -> Payment.Method.CASH;
        };

        // Confirm cost
        roomService.findByNumber(roomNum).ifPresent(r -> {
            int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
            double sub = r.getType().getPricePerNight() * nights;
            double tax = sub * 0.18;
            System.out.printf("%n  Room: %s | %d nights | Subtotal: ₹%,.2f | GST: ₹%,.2f | TOTAL: ₹%,.2f%n",
                    r.getType().getDisplayName(), nights, sub, tax, sub + tax);
        });

        String confirm = prompt("  Confirm booking? (y/n)");
        if (!confirm.equalsIgnoreCase("y")) { System.out.println("  Booking cancelled."); return; }

        Booking booking = bookingService.createBooking(
                guest, roomNum, checkIn, checkOut, numGuests, special, method);

        if (booking != null) booking.printReceipt();
    }

    // ── Feature: View booking ──────────────────────────────────────────────────
    private static void viewBooking() {
        String id = prompt("  Enter Booking ID or Guest Name");
        Optional<Booking> opt = bookingService.findById(id);
        if (opt.isPresent()) {
            opt.get().printReceipt();
        } else {
            List<Booking> found = bookingService.findByGuestName(id);
            if (found.isEmpty()) System.out.println("  No bookings found for: " + id);
            else found.forEach(b -> System.out.println("  " + b));
        }
    }

    // ── Feature: Cancel ───────────────────────────────────────────────────────
    private static void cancelReservation() {
        String id = prompt("  Enter Booking ID to cancel");
        bookingService.findById(id).ifPresentOrElse(
                b -> {
                    System.out.println("  " + b);
                    String conf = prompt("  Confirm cancellation? (y/n)");
                    if (conf.equalsIgnoreCase("y")) bookingService.cancelBooking(id);
                    else System.out.println("  Cancellation aborted.");
                },
                () -> System.out.println("  Booking not found: " + id)
        );
    }

    // ── Feature: Check-in ─────────────────────────────────────────────────────
    private static void checkInGuest() {
        String id = prompt("  Enter Booking ID");
        bookingService.checkIn(id);
    }

    // ── Feature: Check-out ────────────────────────────────────────────────────
    private static void checkOutGuest() {
        String id = prompt("  Enter Booking ID");
        bookingService.checkOut(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static String prompt(String msg) {
        System.out.print(msg + ": ");
        return sc.nextLine();
    }

    private static LocalDate promptDate(String msg) {
        while (true) {
            String raw = prompt(msg);
            try { return LocalDate.parse(raw.trim(), DATE_FMT); }
            catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use dd-MM-yyyy (e.g. 15-07-2025).");
            }
        }
    }

    private static int promptInt(String msg, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(prompt(msg));
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.printf("  Enter a number between %d and %d.%n", min, max);
        }
    }

    private static void bye() {
        System.out.println("\n  Thank you for using Grand Palace Hotel System. Goodbye!\n");
    }
}
