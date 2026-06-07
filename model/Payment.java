package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simulated payment record attached to a booking.
 */
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status  { PENDING, SUCCESS, FAILED, REFUNDED }
    public enum Method  { CASH, CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING }

    private final String        paymentId;
    private final double        amount;
    private final Method        method;
    private       Status        status;
    private final LocalDateTime timestamp;
    private       String        transactionRef;

    public Payment(String paymentId, double amount, Method method) {
        this.paymentId  = paymentId;
        this.amount     = amount;
        this.method     = method;
        this.status     = Status.PENDING;
        this.timestamp  = LocalDateTime.now();
    }

    /** Simulates payment processing — always succeeds for the demo. */
    public boolean process() {
        // In a real system this would call a payment gateway.
        this.transactionRef = "TXN" + System.currentTimeMillis();
        this.status = Status.SUCCESS;
        return true;
    }

    public void refund() {
        if (status == Status.SUCCESS) status = Status.REFUNDED;
    }

    public String     getPaymentId()      { return paymentId;     }
    public double     getAmount()         { return amount;        }
    public Method     getMethod()         { return method;        }
    public Status     getStatus()         { return status;        }
    public String     getTransactionRef() { return transactionRef; }
    public LocalDateTime getTimestamp()   { return timestamp;     }

    @Override
    public String toString() {
        return String.format("Payment[%s] ₹%,.2f via %s | %s | Ref: %s | %s",
                paymentId, amount, method,
                status,
                transactionRef != null ? transactionRef : "N/A",
                timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
    }
}
