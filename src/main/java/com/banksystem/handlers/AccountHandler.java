package com.banksystem.handlers;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.NoSuchAccountException;
import com.banksystem.Exceptions.WithdrawalExceedsBalanceException;
import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;
import com.banksystem.repository.BankDataStore;

import javax.security.auth.login.AccountLockedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class AccountHandler {

    private BankDataStore bankDataStore;
    // This lock is not really necessary for this application since
    // it's only used to store transactionInfo sequentially in bankDataStore.
    // This is not needed since each transaction has a timestamp so they could be
    // ordered after timestamp when needed.
    private ReentrantLock lock;

    public AccountHandler(BankDataStore bankDataStore){
        this.bankDataStore = bankDataStore;

        lock = new ReentrantLock();
    }

    public List<BankAccount> getAllAccountsForUser(int userId) {
        return bankDataStore.getAllBankAccountsForUser(userId);
    }

    public BankAccount getAccount(long accountNumber) throws NoSuchAccountException {

        return bankDataStore.getAccount(accountNumber);
    }

    public void depositMoney(long accountNumber, double amount) throws NegativeDepositException, NoSuchAccountException {

        try {
            lock.lock();
            BankAccount account = getAccount(accountNumber);
            account.depositMoney(amount);
            LocalDateTime localDateTime = LocalDateTime.now();
            TransactionInfo tInfo = new TransactionInfo("Deposit" , amount, localDateTime);
            bankDataStore.storeTransactionInfo(tInfo, accountNumber);
        } finally {
            lock.unlock();
        }
    }

    public void withdrawMoney(long accountNumber, double amount) throws AccountLockedException, WithdrawalExceedsBalanceException, NoSuchAccountException {

        try {
            lock.lock();
            BankAccount account = getAccount(accountNumber);
            account.withdrawMoney(amount);
            //Withdraws should be stored as negative amounts
            LocalDateTime localDateTime = LocalDateTime.now();
            TransactionInfo tInfo = new TransactionInfo("Withdraw", -amount, localDateTime);
            bankDataStore.storeTransactionInfo(tInfo, accountNumber);
        } finally {
            lock.unlock();
        }
    }

    public void transferMoney(long fromAccountNumber, long toAccountNumber, double amount) throws AccountLockedException, WithdrawalExceedsBalanceException, NegativeDepositException, NoSuchAccountException {

        //Transaction
        try{
            lock.lock();
            BankAccount fromAccount = getAccount(fromAccountNumber);
            BankAccount toAccount = getAccount(toAccountNumber);
            toAccount.depositMoney(fromAccount.withdrawMoney(amount));
            //If no exception was thrown save transaction
            LocalDateTime localDateTime = LocalDateTime.now();
            TransactionInfo tInfo = new TransactionInfo("Transfer-Withdraw", -amount, localDateTime);
            bankDataStore.storeTransactionInfo(tInfo, fromAccountNumber);
            tInfo = new TransactionInfo("Transfer-Deposit", amount, localDateTime);
            bankDataStore.storeTransactionInfo(tInfo, toAccountNumber);
        } finally {
            lock.unlock();
        }

    }

    public List<TransactionInfo> getTransactionsLog(long accountNumber) {
        return bankDataStore.getAllTransactionInfo(accountNumber);
    }

    public void lockAccount(long accountNumber) throws NoSuchAccountException {
        getAccount(accountNumber).lock();
    }

    public void unlockAccount(long accountNumber) throws NoSuchAccountException {
        getAccount(accountNumber).unlock();
    }
}
