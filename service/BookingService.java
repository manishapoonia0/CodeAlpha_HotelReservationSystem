package service;

import model.*;
import util.FileIO;
import util.IdGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core booking engine.
 * Handles create, cancel, check-in, check-out, and lookup operations.
 */
public class BookingService {

    private final List<Booking> bookings;
    private final RoomService   roomService;

    public BookingService(RoomService roomService) {
        this.roomService = roomService;
        this.bookings    = FileIO.loadBookings();
    }

    // ── Create booking ─────────────────────────────────────────────────────────
    /**
     * Creates and confirms a new booking with simulated payment.
     *
     * @return the confirmed Booking, or null if room unavailable or dates invalid
     */
    public Booking createBooking(Guest guest,
                                  String roomNumber,
                                  LocalDate checkIn,
                                  LocalDate checkOut,
                                  int numGuests,
                                  String specialRequests,
                                  Payment.Method paymentMethod) {

        // Validate dates
        if (!checkOut.isAfter(checkIn)) {
            System.out.println("  ✗ Check-out must be after check-in.");
            return null;
        }
        if (checkIn.isBefore(LocalDate.now())) {
            System.out.println("  ✗ Check-in date cannot be in the past.");
            return null;
        }

        // Validate room availability
        Optional<Room> roomOpt = roomService.findByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            System.out.println("  ✗ Room " + roomNumber + " does not exist.");
            return null;
        }
        Room room = roomOpt.get();
        if (!room.isAvailable()) {
            System.out.println("  ✗ Room " + roomNumber + " is currently booked.");
            return null;
        }

        // Build booking
        String  bookingId = IdGenerator.nextBookingId();
        Booking booking   = new Booking(bookingId, guest, room,
                                        checkIn, checkOut, numGuests, specialRequests);

        // Process payment
        Payment payment = new Payment(
                IdGenerator.nextPaymentId(),
                booking.getTotalAmount(),
                paymentMethod);

        System.out.println("\n  Processing payment…");
        if (payment.process()) {
            booking.setPayment(payment);
            System.out.printf("  ✓ Payment of ₹%,.2f successful — Ref: %s%n",
                    payment.getAmount(), payment.getTransactionRef());
        } else {
            System.out.println("  ✗ Payment failed. Booking not confirmed.");
            return null;
        }

        // Confirm booking
        roomService.markBooked(roomNumber);
        bookings.add(booking);
        FileIO.saveBookings(bookings);
        FileIO.exportReceiptToFile(booking);

        System.out.printf("  ✓ Booking %s CONFIRMED!%n", bookingId);
        return booking;
    }

    // ── Cancel booking ─────────────────────────────────────────────────────────
    public boolean cancelBooking(String bookingId) {
        Optional<Booking> opt = findById(bookingId);
        if (opt.isEmpty()) {
            System.out.println("  ✗ Booking not found: " + bookingId);
            return false;
        }
        Booking b = opt.get();
        if (b.getStatus() == Booking.Status.CANCELLED) {
            System.out.println("  ✗ Booking is already cancelled.");
            return false;
        }
        if (b.getStatus() == Booking.Status.CHECKED_OUT) {
            System.out.println("  ✗ Cannot cancel a completed stay.");
            return false;
        }

        b.setStatus(Booking.Status.CANCELLED);
        roomService.markAvailable(b.getRoom().getRoomNumber());

        // Refund payment
        if (b.getPayment() != null) {
            b.getPayment().refund();
            System.out.printf("  ✓ Refund of ₹%,.2f initiated to %s%n",
                    b.getPayment().getAmount(),
                    b.getPayment().getMethod());
        }

        FileIO.saveBookings(bookings);
        System.out.printf("  ✓ Booking %s cancelled.%n", bookingId);
        return true;
    }

    // ── Check-in ───────────────────────────────────────────────────────────────
    public boolean checkIn(String bookingId) {
        return findById(bookingId).map(b -> {
            if (b.getStatus() != Booking.Status.CONFIRMED) {
                System.out.println("  ✗ Booking is not in CONFIRMED state.");
                return false;
            }
            b.setStatus(Booking.Status.CHECKED_IN);
            FileIO.saveBookings(bookings);
            System.out.printf("  ✓ Guest %s checked in to room %s.%n",
                    b.getGuest().getName(), b.getRoom().getRoomNumber());
            return true;
        }).orElse(false);
    }

    // ── Check-out ──────────────────────────────────────────────────────────────
    public boolean checkOut(String bookingId) {
        return findById(bookingId).map(b -> {
            if (b.getStatus() != Booking.Status.CHECKED_IN) {
                System.out.println("  ✗ Guest is not currently checked in.");
                return false;
            }
            b.setStatus(Booking.Status.CHECKED_OUT);
            roomService.markAvailable(b.getRoom().getRoomNumber());
            FileIO.saveBookings(bookings);
            System.out.printf("  ✓ Guest %s checked out from room %s.%n",
                    b.getGuest().getName(), b.getRoom().getRoomNumber());
            return true;
        }).orElse(false);
    }

    // ── Lookups ────────────────────────────────────────────────────────────────
    public Optional<Booking> findById(String bookingId) {
        return bookings.stream()
                .filter(b -> b.getBookingId().equalsIgnoreCase(bookingId))
                .findFirst();
    }

    public List<Booking> findByGuestName(String name) {
        return bookings.stream()
                .filter(b -> b.getGuest().getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Booking> getAllBookings()  { return bookings; }

    public List<Booking> getActive() {
        return bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.CONFIRMED
                          || b.getStatus() == Booking.Status.CHECKED_IN)
                .collect(Collectors.toList());
    }

    // ── Reports ────────────────────────────────────────────────────────────────
    public void printAllBookings() {
        if (bookings.isEmpty()) { System.out.println("  No bookings on record."); return; }
        System.out.println("\n  ── ALL BOOKINGS ────────────────────────────────────────────────");
        bookings.forEach(b -> System.out.println("  " + b));
        System.out.println("  ────────────────────────────────────────────────────────────────");
    }

    public void printDashboard() {
        long confirmed   = bookings.stream().filter(b -> b.getStatus() == Booking.Status.CONFIRMED).count();
        long checkedIn   = bookings.stream().filter(b -> b.getStatus() == Booking.Status.CHECKED_IN).count();
        long checkedOut  = bookings.stream().filter(b -> b.getStatus() == Booking.Status.CHECKED_OUT).count();
        long cancelled   = bookings.stream().filter(b -> b.getStatus() == Booking.Status.CANCELLED).count();
        double revenue   = bookings.stream()
                .filter(b -> b.getStatus() != Booking.Status.CANCELLED)
                .mapToDouble(Booking::getTotalAmount).sum();

        System.out.println("\n  ── DASHBOARD ───────────────────────────────────────────────────");
        System.out.printf("  Total Bookings  : %d%n",    bookings.size());
        System.out.printf("  Confirmed       : %d%n",    confirmed);
        System.out.printf("  Checked In      : %d%n",    checkedIn);
        System.out.printf("  Checked Out     : %d%n",    checkedOut);
        System.out.printf("  Cancelled       : %d%n",    cancelled);
        System.out.printf("  Total Revenue   : ₹%,.2f%n", revenue);
        System.out.println("  ────────────────────────────────────────────────────────────────");
    }
}
