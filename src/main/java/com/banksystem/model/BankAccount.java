package com.banksystem.model;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalance;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BankAccount {

    private int userId;
    private long accountNumber;
    private volatile double accountBalance;

    private ReadWriteLock lock;
    private Lock readLock;
    private Lock writeLock;

    public BankAccount(long accountNumber, int userId){
        this.accountNumber = accountNumber;
        this.userId = userId;
        accountBalance = 0.0;
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();

    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public synchronized double getAccountBalance() {
        try{
            readLock.lock();
            return accountBalance;
        } finally {
            readLock.unlock();
        }
    }

    public synchronized void depositMoney(double deposit) throws NegativeDepositException{
        if(deposit < 0) throw new NegativeDepositException();
        try{
            writeLock.lock();
            accountBalance += deposit;
        } finally {
            writeLock.unlock();
        }

    }

    public synchronized double withdrawMoney(double withdrawal) throws WithdrawalExceedsBalance {
        if(withdrawal > accountBalance) throw new WithdrawalExceedsBalance();
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

}

