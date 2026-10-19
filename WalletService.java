
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletService {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Wallet> wallets = new HashMap<>();

    private final Map<String, TopUp> topUps = new HashMap<>();
    private final Map<String, Transfer> transfers = new HashMap<>();
    private final Map<String, Payment> payments = new HashMap<>();
    private final Map<String, Refund> refunds = new HashMap<>();

    private final List<StatementEntry> statements = new ArrayList<>();

    private int userCounter = 1001;
    private int walletCounter = 2001;
    private int transactionCounter = 3001;

    private BigDecimal transferFeePercent;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public WalletService(BigDecimal transferFeePercent) {
        if (transferFeePercent == null
                || transferFeePercent.compareTo(BigDecimal.ZERO) < 0
                || transferFeePercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "Fee percentage must be between 0 and 100.");
        }

        this.transferFeePercent = transferFeePercent;
    }

    public BigDecimal getTransferFeePercent() {
        return transferFeePercent;
    }

    public void setTransferFeePercent(BigDecimal percent) {
        if (percent == null
                || percent.compareTo(BigDecimal.ZERO) < 0
                || percent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "Fee percentage must be between 0 and 100.");
        }

        transferFeePercent = percent;
    }

    private String nextTransactionId() {
        return "TXN" + transactionCounter++;
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than 2 decimal places.");
        }
    }

    private Wallet getWallet(String walletId) {
        Wallet wallet = wallets.get(walletId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "Wallet not found: " + walletId);
        }

        return wallet;
    }

    private void addStatement(String txnId, Wallet wallet,
            String type, BigDecimal amount,
            BigDecimal fee, String description) {
        statements.add(new StatementEntry(
                txnId,
                wallet.getWalletId(),
                type,
                amount,
                fee,
                wallet.getBalance(),
                description));
    }

    // 1. Create a user
    public User createUser(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || !email.trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Enter a valid email.");
        }

        for (User user : users.values()) {
            if (user.getEmail().equalsIgnoreCase(email.trim())) {
                throw new IllegalArgumentException(
                        "This email is already registered.");
            }
        }

        String id = "U" + userCounter++;

        User user = new User(id, name.trim(), email.trim());
        users.put(id, user);

        System.out.println("\nUser created successfully.");
        System.out.println("User ID: " + id);
        return user;
    }

    // 2. Create a wallet for a user
    public Wallet createWallet(String userId) {
        User user = users.get(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        for (Wallet existing : wallets.values()) {
            if (existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException(
                        "This user already has a wallet: "
                                + existing.getWalletId());
            }
        }

        String id = "W" + walletCounter++;
        Wallet wallet = new Wallet(id, userId);
        wallets.put(id, wallet);

        System.out.println("Wallet created successfully.");
        System.out.println("Owner: " + user.getName());
        System.out.println("Wallet ID: " + id);
        System.out.println("Opening balance: 0.00");

        return wallet;
    }

    // 3. Top-up
    public TopUp topUp(String walletId, BigDecimal amount) {
        amount = validateAmount(amount);
        Wallet wallet = getWallet(walletId);

        String txnId = nextTransactionId();
        wallet.credit(amount);

        TopUp topUp = new TopUp(txnId, walletId, amount);
        topUps.put(txnId, topUp);

        addStatement(txnId, wallet, "TOP_UP", amount,
                BigDecimal.ZERO.setScale(2), "Wallet top-up");

        printReceipt(txnId, "TOP-UP", amount,
                BigDecimal.ZERO.setScale(2), wallet);

        return topUp;
    }

    // 4. Wallet-to-wallet transfer
    public Transfer transfer(String fromId, String toId,
            BigDecimal amount) {
        amount = validateAmount(amount);

        if (fromId == null || toId == null || fromId.equals(toId)) {
            throw new IllegalArgumentException(
                    "Source and destination wallets must be different.");
        }

        Wallet sender = getWallet(fromId);
        Wallet receiver = getWallet(toId);

        BigDecimal fee = amount.multiply(transferFeePercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal totalDebit = amount.add(fee);

        if (sender.getBalance().compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds. Required: " + totalDebit
                            + " (amount + fee), available: "
                            + sender.getBalance());
        }

        String txnId = nextTransactionId();

        // All validation is completed before changing either wallet.
        boolean debited = sender.debit(totalDebit);

        if (!debited) {
            throw new IllegalArgumentException("Transfer failed.");
        }

        receiver.credit(amount);

        Transfer transfer = new Transfer(
                txnId, fromId, toId, amount, fee);
        transfers.put(txnId, transfer);

        addStatement(txnId, sender, "TRANSFER_OUT",
                amount.negate(), fee,
                "Transfer to " + toId);

        addStatement(txnId, receiver, "TRANSFER_IN",
                amount, BigDecimal.ZERO.setScale(2),
                "Transfer from " + fromId);

        System.out.println("\n========== TRANSFER RECEIPT ==========");
        System.out.println("Transaction ID : " + txnId);
        System.out.println("From wallet    : " + fromId);
        System.out.println("To wallet      : " + toId);
        System.out.println("Transfer amount: " + amount);
        System.out.println("Fee (" + transferFeePercent
                + "%)       : " + fee);
        System.out.println("Total debited  : " + totalDebit);
        System.out.println("Sender balance : " + sender.getBalance());
        System.out.println("Receiver balance: " + receiver.getBalance());
        System.out.println("Status         : SUCCESS");
        System.out.println("======================================");

        return transfer;
    }

    // 5. Merchant payment
    public Payment makePayment(String walletId, String merchant,
            BigDecimal amount) {
        amount = validateAmount(amount);
        Wallet wallet = getWallet(walletId);

        if (merchant == null || merchant.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Merchant name is required.");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds. Available balance: "
                            + wallet.getBalance());
        }

        String txnId = nextTransactionId();
        wallet.debit(amount);

        Payment payment = new Payment(
                txnId, walletId, merchant.trim(), amount);
        payments.put(txnId, payment);

        addStatement(txnId, wallet, "PAYMENT",
                amount.negate(), BigDecimal.ZERO.setScale(2),
                "Payment to " + merchant.trim());

        printReceipt(txnId, "PAYMENT", amount,
                BigDecimal.ZERO.setScale(2), wallet);

        System.out.println("Merchant: " + merchant.trim());
        return payment;
    }

    // 6. Full or partial refund against an original payment
    public Refund refund(String originalPaymentId, BigDecimal amount) {
        amount = validateAmount(amount);

        Payment original = payments.get(originalPaymentId);

        if (original == null) {
            throw new IllegalArgumentException(
                    "Original payment not found. Refunds are allowed "
                            + "only for successful payments.");
        }

        BigDecimal alreadyRefunded = BigDecimal.ZERO.setScale(2);

        for (Refund refund : refunds.values()) {
            if (refund.getOriginalPaymentId()
                    .equals(originalPaymentId)) {
                alreadyRefunded = alreadyRefunded.add(refund.getAmount());
            }
        }

        BigDecimal remaining = original.getAmount()
                .subtract(alreadyRefunded);

        if (amount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException(
                    "Refund exceeds refundable amount. Remaining: "
                            + remaining);
        }

        Wallet wallet = getWallet(original.getWalletId());

        String txnId = nextTransactionId();
        wallet.credit(amount);

        Refund refund = new Refund(
                txnId,
                originalPaymentId,
                original.getWalletId(),
                amount);

        refunds.put(txnId, refund);

        addStatement(txnId, wallet, "REFUND",
                amount, BigDecimal.ZERO.setScale(2),
                "Refund for payment " + originalPaymentId);

        printReceipt(txnId, "REFUND", amount,
                BigDecimal.ZERO.setScale(2), wallet);

        System.out.println("Original payment: " + originalPaymentId);
        System.out.println("Previously refunded: " + alreadyRefunded);
        System.out.println("Remaining refundable amount: "
                + remaining.subtract(amount));

        return refund;
    }

    // 7. Chronological statement for a wallet
    public void showStatement(String walletId) {
        Wallet wallet = getWallet(walletId);

        List<StatementEntry> entries = new ArrayList<>();

        for (StatementEntry entry : statements) {
            if (entry.getWalletId().equals(walletId)) {
                entries.add(entry);
            }
        }

        entries.sort(Comparator
                .comparing(StatementEntry::getTime)
                .thenComparing(StatementEntry::getTransactionId));

        System.out.println("\n================ WALLET STATEMENT ================");
        System.out.println("Wallet ID: " + walletId);
        System.out.println("Current balance: " + wallet.getBalance());

        if (entries.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            System.out.printf("%-18s %-14s %12s %10s %12s%n",
                    "DATE/TIME", "TYPE", "AMOUNT", "FEE", "BALANCE");

            for (StatementEntry entry : entries) {
                System.out.printf("%-18s %-14s %12s %10s %12s%n",
                        entry.getTime().format(FORMAT),
                        entry.getType(),
                        entry.getAmount(),
                        entry.getFee(),
                        entry.getBalanceAfter());

                System.out.println("  " + entry.getDescription()
                        + " | Transaction: "
                        + entry.getTransactionId());
            }
        }

        System.out.println("===================================================");
    }

    // 8. Check balance
    public void showBalance(String walletId) {
        Wallet wallet = getWallet(walletId);
        User owner = users.get(wallet.getUserId());

        System.out.println("\n--- WALLET DETAILS ---");
        System.out.println("Wallet ID: " + wallet.getWalletId());
        System.out.println("Owner: " + owner.getName());
        System.out.println("Balance: " + wallet.getBalance());
    }

    // 9. List users and their wallets
    public void listUsers() {
        if (users.isEmpty()) {
            System.out.println("No users registered.");
            return;
        }

        System.out.println("\n--- REGISTERED USERS ---");

        for (User user : users.values()) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Name: " + user.getName());
            System.out.println("Email: " + user.getEmail());

            boolean found = false;
            for (Wallet wallet : wallets.values()) {
                if (wallet.getUserId().equals(user.getUserId())) {
                    System.out.println("Wallet ID: "
                            + wallet.getWalletId());
                    System.out.println("Balance: "
                            + wallet.getBalance());
                    found = true;
                }
            }

            if (!found) {
                System.out.println("Wallet: Not created yet");
            }

            System.out.println("-------------------------");
        }
    }

    private void printReceipt(String txnId, String type,
            BigDecimal amount, BigDecimal fee,
            Wallet wallet) {
        System.out.println("\n============= TRANSACTION RECEIPT =============");
        System.out.println("Transaction ID : " + txnId);
        System.out.println("Type           : " + type);
        System.out.println("Wallet ID      : " + wallet.getWalletId());
        System.out.println("Amount         : " + amount);
        System.out.println("Fee            : " + fee);
        System.out.println("Balance after  : " + wallet.getBalance());
        System.out.println("Status         : SUCCESS");
        System.out.println("Time           : "
                + java.time.LocalDateTime.now().format(FORMAT));
        System.out.println("================================================");
    }
}
