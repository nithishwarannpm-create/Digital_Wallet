
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StatementEntry {
    private final String transactionId;
    private final String walletId;
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final BigDecimal balanceAfter;
    private final String description;
    private final LocalDateTime time;

    public StatementEntry(String transactionId, String walletId,
            String type, BigDecimal amount,
            BigDecimal fee, BigDecimal balanceAfter,
            String description) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.fee = fee;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.time = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
