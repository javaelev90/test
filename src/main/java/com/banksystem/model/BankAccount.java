package com.banksystem.model;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalance;

import javax.security.auth.login.AccountLockedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BankAccount {

    private int userId;
    private long accountNumber;
    private volatile double accountBalance;
    private AtomicBoolean isLocked;

    private Lock readLock;
    private Lock writeLock;

    public BankAccount(long accountNumber, int userId){
        this.accountNumber = accountNumber;
        this.userId = userId;
        accountBalance = 0.0;
        isLocked = new AtomicBoolean(false);
        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public double getAccountBalance() {
        try{
            readLock.lock();
            return accountBalance;
        } finally {
            readLock.unlock();
        }
    }

    public void depositMoney(double deposit) throws NegativeDepositException {
        if(deposit < 0) throw new NegativeDepositException(""+accountNumber);
        try{
            writeLock.lock();
            accountBalance += deposit;
        } finally {
            writeLock.unlock();
        }

    }

    public double withdrawMoney(double withdrawal) throws WithdrawalExceedsBalance, AccountLockedException {
        if(isLocked.get()){
            throw new AccountLockedException(""+accountNumber);
        }
        if(withdrawal > accountBalance) throw new WithdrawalExceedsBalance(""+accountNumber);
        try{
            writeLock.lock();
            accountBalance -= withdrawal;
        } finally {
            writeLock.unlock();
        }
        return withdrawal;
    }

    public int getUserId(){
        return userId;
    }

    public void lock(){
        isLocked.set(true);
    }

    public void unlock(){
        isLocked.set(false);
    }

    public String toString(){
        return "Account number: "+accountNumber+"\n"+
                "User Id: "+ userId + "\n"+
                "Balance: "+accountBalance +"\n"+
                "Account locked: "+ isLocked.get() +"\n";
    }

}

