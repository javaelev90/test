package com.banksystem.ui;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalanceException;
import com.banksystem.handlers.AccountHandler;
import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;
import com.banksystem.model.User;

import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ConsoleUI {

    private AccountHandler accountHandler;
    private User user;

    public ConsoleUI(AccountHandler accountHandler, User user){
        this.user = user;
        this.accountHandler = accountHandler;
    }


    /**
     * This is where the ui started and the menu loop beings
     */
    public void launch(){

        boolean userWantsToExit = false;
        long accountNumber;
        try(InputReader reader = new InputReader(new InputStreamReader(System.in))){

            while(!userWantsToExit){
                showMainMenu();
                int choice = reader.readChoiceInput();
                switch(choice) {
                    case 1:
                        printAccounts(accountHandler.getAllAccountsForUser(user.getId()));
                        break;
                    case 2:
                        printBalanceForAllAccounts(accountHandler.getAllAccountsForUser(user.getId()));
                        break;
                    case 3:
                        accountNumber = enterBankAccountNumber(reader);
                        if(accountNumber == -1){
                            continue;
                        }
                        printBalanceSpecificAccount(accountHandler.getAccount(accountNumber));
                        break;
                    case 4:
                        showTransActionsForAccount(reader);
                        break;
                    case 5:
                        depositMoney(reader);
                        break;
                    case 6:
                        withdrawMoney(reader);
                        break;
                    case 7:
                        transferMoney(reader);
                        break;
                    case 8:
                        lockAccount(reader);
                        break;
                    case 9:
                        unlockAccount(reader);
                        break;
                    case 10:
                        System.out.println("Now exiting.");
                        userWantsToExit = true;
                        break;
                    case -2:
                        System.out.println("You have to supply an integer.");
                        break;
                    default:
                        System.out.println("That is not a valid menu option.");
                        break;
                }


            }
        } catch (IOException e) {
            System.err.println("An "+e.toString()+" was thrown when reading from system in.");
        }

    }

    private int showTransActionsForAccount(InputReader reader) {
        long accountNumber = enterBankAccountNumber(reader);
        if(accountNumber == -1){
            return -1;
        }
        List<TransactionInfo> transactionInfoList = accountHandler.getTransactionsLog(accountNumber);
        if(transactionInfoList == null || transactionInfoList.isEmpty()){
            System.out.println("No transactions for that account.");
            return -1;
        }
        System.out.println("Transaction history for account: "+accountNumber);
        transactionInfoList.forEach(tInfo ->
                System.out.println("-"+tInfo.toString())
        );
        return 0;
    }

    private int unlockAccount(InputReader reader) {
        long accountToUnlock = enterBankAccountNumber(reader);
        if(accountToUnlock == -1){
            return -1;
        }
        accountHandler.unlockAccount(accountToUnlock);
        return 0;
    }

    private int lockAccount(InputReader reader) {
        long accountToLock = enterBankAccountNumber(reader);
        if(accountToLock == -1){
            return -1;
        }
        accountHandler.lockAccount(accountToLock);
        return 0;
    }

    private int transferMoney(InputReader reader) {
        double amount = enterAmountOfMoneyToAdd(reader);
        if(amount == -1){
            return -1;
        }
        System.out.println("From account");
        long fromAccount = enterBankAccountNumber(reader);
        if(fromAccount == -1){
            return -1;
        }
        System.out.println("To account");
        long toAccount = enterBankAccountNumber(reader);
        if(toAccount == -1){
            return -1;
        }
        try {
            accountHandler.transferMoney(fromAccount, toAccount, amount);
        }  catch (AccountLockedException e) {
            System.out.println("Account "+e.getMessage()+" is locked.");
        } catch (WithdrawalExceedsBalanceException e) {
            System.out.println("The withdrawal exceeds funds.");
        } catch (NegativeDepositException e) {
            System.out.println("You can't deposit a negative amount.");
        }
        return 0;
    }

    private int withdrawMoney(InputReader reader) {
        double amount = enterAmountOfMoneyToAdd(reader);
        if(amount == -1){
            return -1;
        }
        long accountNumber = enterBankAccountNumber(reader);
        if(accountNumber == -1){
            return -1;
        }
        try {
            accountHandler.withdrawMoney(accountNumber, amount);
        }  catch (AccountLockedException e) {
            System.out.println("Account "+e.getMessage()+" is locked.");
        } catch (WithdrawalExceedsBalanceException e) {
            System.out.println("The withdrawal exceeds funds.");
        }
        return 0;
    }

    private int depositMoney(InputReader reader){
        double amount = enterAmountOfMoneyToAdd(reader);
        if(amount == -1){
            return -1;
        }
        long accountNumber = enterBankAccountNumber(reader);
        if(accountNumber == -1){
            return -1;
        }
        try {
            accountHandler.depositMoney(accountNumber, amount);
        } catch (NegativeDepositException e) {
            System.out.println("You can't deposit a negative amount.");
        }
        return 0;
    }

    private double enterAmountOfMoneyToAdd(InputReader reader) {
        System.out.println("Enter an amount of money, leave blank to exit");
        double amount = reader.enterAmountOfMoney();
        if(amount == -1){
            return -1;
        } else if(amount == -2){
            System.out.println("Number has to be a positive number");
            return -1;
        } else if(amount == -3){
            System.out.println("The input has to be a number");
            return -1;
        }
        return amount;
    }

    private long enterBankAccountNumber(InputReader reader){
        System.out.println("Enter account number, leave blank to exit");
        long accountNumber = reader.enterBankAccountNumber();
        if(accountNumber == -1){
            return -1;
        } else if(accountNumber == -2){
            System.out.println("Account number has to be a positive number");
            return -1;
        } else if(accountNumber == -3){
            System.out.println("The input has to be a number");
            return -1;
        }
        return accountNumber;
    }




    private void printBalanceSpecificAccount(BankAccount account){
        if(account == null){
            System.out.println("That account does not exist.");
            return;
        }

        double balance = account.getAccountBalance();
        System.out.println("Account "+account.getAccountNumber()+" has balance: "+balance);

    }

    private void printBalanceForAllAccounts(List<BankAccount> accounts){
        accounts.forEach(this::printBalanceSpecificAccount);
    }

    private void printAccounts(List<BankAccount> accounts){
        System.out.println("Accounts: ");
        accounts.forEach(e -> System.out.println(e.getAccountNumber()));
    }

    /**
     * Prints main menu
     */
    private void showMainMenu() {
        System.out.println("---Main menu---");
        System.out.println("1. View all bank accounts");
        System.out.println("2. View balance on all accounts");
        System.out.println("3. View balance for a bank account");
        System.out.println("4. View transaction history for a bank account");
        System.out.println("5. Deposit money to a bank account");
        System.out.println("6. Withdraw money from a bank account");
        System.out.println("7. Transfer money between accounts");
        System.out.println("8. Lock an account");
        System.out.println("9. Unlock an account");
        System.out.println("10. Exit");

    }




}
