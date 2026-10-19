
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transfer {
    private final String transactionId;
    private final String fromWallet;
    private final String toWallet;
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final LocalDateTime time;

    public Transfer(String transactionId, String fromWallet,
            String toWallet, BigDecimal amount,
            BigDecimal fee) {
        this.transactionId = transactionId;
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.amount = amount;
        this.fee = fee;
        this.time = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFromWallet() {
        return fromWallet;
    }

    public String getToWallet() {
        return toWallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
