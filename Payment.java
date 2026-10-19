
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private final String transactionId;
    private final String walletId;
    private final String merchant;
    private final BigDecimal amount;
    private final LocalDateTime time;

    public Payment(String transactionId, String walletId,
            String merchant, BigDecimal amount) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.merchant = merchant;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getMerchant() {
        return merchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
