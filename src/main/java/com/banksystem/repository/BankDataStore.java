package com.banksystem.repository;

import com.banksystem.model.BankAccount;
import com.banksystem.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankDataStore {

    private static long BANK_ACCOUNT_COUNTER = 0;
    private Lock generateBankAccountNumberLock;
    private ConcurrentHashMap<Integer, List<BankAccount>> bank;


    public BankDataStore(){
        generateBankAccountNumberLock = new ReentrantLock();
        bank = new ConcurrentHashMap<>();
    }

    public BankAccount makeAccount(User user) {
        BankAccount bankAccount = new BankAccount(generateBankAccountNumber(),user.getId());
        if(bank.containsKey(user.getId())){
            List<BankAccount> accounts = bank.get(user.getId());
            accounts.add(bankAccount);
        } else {
            List<BankAccount> accounts = new ArrayList<>();
            accounts.add(bankAccount);
            bank.put(user.getId(), accounts);
        }
        return bankAccount;
    }



    private long generateBankAccountNumber(){
        try{
            generateBankAccountNumberLock.lock();
            return BANK_ACCOUNT_COUNTER++;
        } finally {
            generateBankAccountNumberLock.unlock();
        }

    }

}
