
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TopUp {
    private final String transactionId;
    private final String walletId;
    private final BigDecimal amount;
    private final LocalDateTime time;

    public TopUp(String transactionId, String walletId,
            BigDecimal amount) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
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
