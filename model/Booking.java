package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

/**
 * Represents a complete hotel booking.
 * Encapsulates guest, room, dates, payment, and booking lifecycle.
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final String    bookingId;
    private final Guest     guest;
    private final Room      room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private       Status    status;
    private       Payment   payment;
    private final int       guests;           // number of occupants
    private final String    specialRequests;

    public Booking(String bookingId, Guest guest, Room room,
                   LocalDate checkIn, LocalDate checkOut,
                   int guests, String specialRequests) {
        this.bookingId       = bookingId;
        this.guest           = guest;
        this.room            = room;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.status          = Status.CONFIRMED;
        this.guests          = guests;
        this.specialRequests = specialRequests;
    }

    // ── Computed ──────────────────────────────────────────────────────────────
    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double getSubtotal() {
        return room.getType().getPricePerNight() * getNights();
    }

    public double getTax() {
        return getSubtotal() * 0.18;   // 18 % GST
    }

    public double getTotalAmount() {
        return getSubtotal() + getTax();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String   getBookingId()       { return bookingId;       }
    public Guest    getGuest()           { return guest;           }
    public Room     getRoom()            { return room;            }
    public LocalDate getCheckIn()        { return checkIn;         }
    public LocalDate getCheckOut()       { return checkOut;        }
    public Status   getStatus()          { return status;          }
    public Payment  getPayment()         { return payment;         }
    public int      getGuests()          { return guests;          }
    public String   getSpecialRequests() { return specialRequests; }

    public void setStatus(Status status)   { this.status  = status;  }
    public void setPayment(Payment payment){ this.payment = payment; }

    // ── Display ───────────────────────────────────────────────────────────────
    public void printReceipt() {
        String line = "─".repeat(55);
        System.out.println("\n" + line);
        System.out.println("         GRAND PALACE HOTEL — BOOKING RECEIPT");
        System.out.println(line);
        System.out.printf("  Booking ID    : %s%n",  bookingId);
        System.out.printf("  Guest         : %s%n",  guest.getName());
        System.out.printf("  Email         : %s%n",  guest.getEmail());
        System.out.printf("  Phone         : %s%n",  guest.getPhone());
        System.out.println(line);
        System.out.printf("  Room          : %s (%s)%n",
                room.getRoomNumber(), room.getType().getDisplayName());
        System.out.printf("  Floor         : %d%n",  room.getFloor());
        System.out.printf("  Check-in      : %s%n",  checkIn.format(FMT));
        System.out.printf("  Check-out     : %s%n",  checkOut.format(FMT));
        System.out.printf("  Nights        : %d%n",  getNights());
        System.out.printf("  Guests        : %d%n",  guests);
        if (specialRequests != null && !specialRequests.isBlank())
            System.out.printf("  Special Req.  : %s%n", specialRequests);
        System.out.println(line);
        System.out.printf("  Room Rate     : ₹%,.2f × %d nights%n",
                room.getType().getPricePerNight(), getNights());
        System.out.printf("  Subtotal      : ₹%,.2f%n", getSubtotal());
        System.out.printf("  GST (18%%)     : ₹%,.2f%n", getTax());
        System.out.printf("  TOTAL         : ₹%,.2f%n",  getTotalAmount());
        System.out.println(line);
        if (payment != null)
            System.out.printf("  Payment       : %s%n", payment);
        System.out.printf("  Status        : %s%n", status);
        System.out.println(line + "\n");
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Room %s (%s) | %s → %s | ₹%,.2f | %s",
                bookingId,
                guest.getName(),
                room.getRoomNumber(),
                room.getType().getDisplayName(),
                checkIn.format(FMT),
                checkOut.format(FMT),
                getTotalAmount(),
                status);
    }
}
