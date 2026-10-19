
import java.math.BigDecimal;

public class Wallet {
    private final String walletId;
    private final String userId;
    private BigDecimal balance;

    public Wallet(String walletId, String userId) {
        this.walletId = walletId;
        this.userId = userId;
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public String getWalletId() {
        return walletId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount).setScale(2);
    }

    public boolean debit(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) {
            return false;
        }
        balance = balance.subtract(amount).setScale(2);
        return true;
    }
}
