package com.banksystem.handlers;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalance;
import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;
import com.banksystem.repository.BankDataStore;

import javax.security.auth.login.AccountLockedException;
import java.util.List;

public class AccountHandler {

    private BankDataStore bankDataStore;

    public AccountHandler(BankDataStore bankDataStore){
        this.bankDataStore = bankDataStore;
    }

    public List<BankAccount> getAllAccountsForUser(int userId) {
        return bankDataStore.getAllBankAccountsForUser(userId);
    }

    public BankAccount getAccount(long accountNumber) {
        return bankDataStore.getAccount(accountNumber);
    }

    public void depositMoney(long accountNumber, double amount) throws NegativeDepositException, AccountLockedException {
        getAccount(accountNumber).depositMoney(amount);
        TransactionInfo tInfo = new TransactionInfo(accountNumber,
                accountNumber, amount);
        bankDataStore.storeTransactionInfo(tInfo, accountNumber);
    }

    public void withdrawMoney(long accountNumber, double amount) throws AccountLockedException, WithdrawalExceedsBalance {
        getAccount(accountNumber).withdrawMoney(amount);
        //Withdraws should be stored as negative amounts
        TransactionInfo tInfo = new TransactionInfo(accountNumber,
                accountNumber, -amount);
        bankDataStore.storeTransactionInfo(tInfo, accountNumber);
    }

    public void transferMoney(long fromAccountNumber, long toAccountNumber, double amount) throws AccountLockedException, WithdrawalExceedsBalance, NegativeDepositException {
        BankAccount fromAccount = getAccount(fromAccountNumber);
        BankAccount toAccount = getAccount(toAccountNumber);
        //Transaction
        toAccount.depositMoney(fromAccount.withdrawMoney(amount));
        //If no exception was thrown save transaction
        TransactionInfo tInfo = new TransactionInfo(fromAccountNumber,
                toAccountNumber, -amount);
        bankDataStore.storeTransactionInfo(tInfo, fromAccountNumber);
        tInfo = new TransactionInfo(fromAccountNumber,
                toAccountNumber, amount);
        bankDataStore.storeTransactionInfo(tInfo, toAccountNumber);
    }

    public List<TransactionInfo> getTransactionsLog(long accountNumber) {
        return bankDataStore.getAllTransactionInfo(accountNumber);
    }

    public void lockAccount(long accountNumber){
        getAccount(accountNumber).lock();
    }

    public void unlockAccount(long accountNumber){
        getAccount(accountNumber).unlock();
    }
}
