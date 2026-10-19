
import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static WalletService service;

    private static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();

        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Enter a valid numeric amount.");
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("       DIGITAL WALLET SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Register User");
        System.out.println("2. Create Wallet");
        System.out.println("3. Top-up Wallet");
        System.out.println("4. Transfer Between Wallets");
        System.out.println("5. Merchant Payment");
        System.out.println("6. Refund Payment");
        System.out.println("7. View Wallet Statement");
        System.out.println("8. Check Wallet Balance");
        System.out.println("9. List Users and Wallets");
        System.out.println("10. Change Transfer Fee");
        System.out.println("0. Exit");
        System.out.println("========================================");
        System.out.println("Transfer fee: "
                + service.getTransferFeePercent() + "%");
    }

    public static void main(String[] args) {
        System.out.println("Welcome to Digital Wallet & Transfers");

        BigDecimal fee = new BigDecimal("1.5");

        try {
            System.out.println("Default transfer fee is 1.5%.");
            String feeInput = readText(
                    "Enter transfer fee percentage, or press Enter "
                            + "to keep 1.5%: ");

            if (!feeInput.isEmpty()) {
                fee = new BigDecimal(feeInput);
            }

            service = new WalletService(fee);
        } catch (Exception e) {
            System.out.println("Invalid fee. Using default 1.5%.");
            service = new WalletService(new BigDecimal("1.5"));
        }

        boolean running = true;

        while (running) {
            printMenu();
            String choice = readText("Choose an option: ");

            try {
                switch (choice) {
                    case "1": {
                        String name = readText("Enter full name: ");
                        String email = readText("Enter email: ");
                        service.createUser(name, email);
                        break;
                    }

                    case "2": {
                        String userId = readText("Enter user ID: ");
                        service.createWallet(userId);
                        break;
                    }

                    case "3": {
                        String walletId = readText("Enter wallet ID: ");
                        BigDecimal amount = readAmount("Enter top-up amount: ");

                        String confirm = readText(
                                "Confirm top-up? (Y/N): ");

                        if (confirm.equalsIgnoreCase("Y")) {
                            service.topUp(walletId, amount);
                        } else {
                            System.out.println("Top-up cancelled.");
                        }
                        break;
                    }

                    case "4": {
                        String from = readText(
                                "Enter sender wallet ID: ");
                        String to = readText(
                                "Enter receiver wallet ID: ");
                        BigDecimal amount = readAmount("Enter transfer amount: ");

                        service.transfer(from, to, amount);
                        break;
                    }

                    case "5": {
                        String walletId = readText("Enter wallet ID: ");
                        String merchant = readText("Enter merchant name: ");
                        BigDecimal amount = readAmount("Enter payment amount: ");

                        service.makePayment(walletId, merchant, amount);
                        break;
                    }

                    case "6": {
                        String paymentId = readText(
                                "Enter original payment transaction ID: ");
                        BigDecimal amount = readAmount("Enter refund amount: ");

                        service.refund(paymentId, amount);
                        break;
                    }

                    case "7": {
                        String walletId = readText("Enter wallet ID: ");
                        service.showStatement(walletId);
                        break;
                    }

                    case "8": {
                        String walletId = readText("Enter wallet ID: ");
                        service.showBalance(walletId);
                        break;
                    }

                    case "9": {
                        service.listUsers();
                        break;
                    }

                    case "10": {
                        BigDecimal newFee = readAmount(
                                "Enter new transfer fee percentage: ");
                        service.setTransferFeePercent(newFee);
                        System.out.println("Transfer fee updated to "
                                + service.getTransferFeePercent() + "%");
                        break;
                    }

                    case "0": {
                        running = false;
                        System.out.println(
                                "Thank you for using Digital Wallet.");
                        break;
                    }

                    default:
                        System.out.println(
                                "Invalid choice. Select an option from the menu.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\nOperation failed: "
                        + e.getMessage());
            } catch (Exception e) {
                System.out.println("\nUnexpected error: "
                        + e.getMessage());
            }
        }

        sc.close();
    }
}
