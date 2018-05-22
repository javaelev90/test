package com.banksystem.model;

public class BankAccount {

    private long accountNumber;
    private double accountBalance;

    public BankAccount(long accountNumber){
        this.accountNumber = accountNumber;
        accountBalance = 0D;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

}

