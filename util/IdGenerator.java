package util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe sequential ID generator for bookings, guests, and payments.
 */
public class IdGenerator {

    private static final AtomicInteger bookingCounter = new AtomicInteger(1000);
    private static final AtomicInteger guestCounter   = new AtomicInteger(100);
    private static final AtomicInteger paymentCounter = new AtomicInteger(500);

    private IdGenerator() {}   // utility class — no instantiation

    /** Returns the next booking ID, e.g. "BK1001" */
    public static String nextBookingId() {
        return "BK" + bookingCounter.incrementAndGet();
    }

    /** Returns the next guest ID, e.g. "G101" */
    public static String nextGuestId() {
        return "G" + guestCounter.incrementAndGet();
    }

    /** Returns the next payment ID, e.g. "PAY501" */
    public static String nextPaymentId() {
        return "PAY" + paymentCounter.incrementAndGet();
    }
}
