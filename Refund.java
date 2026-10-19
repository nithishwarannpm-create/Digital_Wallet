
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Refund {
    private final String transactionId;
    private final String originalPaymentId;
    private final String walletId;
    private final BigDecimal amount;
    private final LocalDateTime time;

    public Refund(String transactionId, String originalPaymentId,
            String walletId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.originalPaymentId = originalPaymentId;
        this.walletId = walletId;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOriginalPaymentId() {
        return originalPaymentId;
    }

    public String getWalletId() {
        return walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
