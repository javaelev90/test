package com.banksystem.repository;

import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankDataStore {

    private static long BANK_ACCOUNT_COUNTER = 0;
    private Lock generateBankAccountNumberLock;
    //Key: user id, Value: list of bank account numbers
    private ConcurrentHashMap<Integer, List<Long>> bank;
    //Key: bank account number, Value: bank account object
    private ConcurrentHashMap<Long, BankAccount> numberToAccountMapping;
    //Key: bank account number, Value: list of transactions
    private ConcurrentHashMap<Long, List<TransactionInfo>> transactions;


    public BankDataStore(){
        generateBankAccountNumberLock = new ReentrantLock();
        bank = new ConcurrentHashMap<>();
        numberToAccountMapping = new ConcurrentHashMap<>();
        transactions =  new ConcurrentHashMap<>();
    }


    /**
     *  Makes an account for the user
     * @param userId
     * @return the bank account number for the created account
     */
    public long makeAccount(int userId) {
        long accountNumber = generateBankAccountNumber();
        BankAccount bankAccount = new BankAccount(accountNumber, userId);
        //Map account number to a bank account object
        numberToAccountMapping.put(accountNumber, bankAccount);
        //Initialize transaction info list
        transactions.put(accountNumber, Collections.synchronizedList(new ArrayList<>()));
        synchronized(bank){
            if(bank.containsKey(userId)){
                List<Long> accounts = bank.get(userId);
                accounts.add(accountNumber);
            } else {
                List<Long> accounts = Collections.synchronizedList(new ArrayList<>());
                accounts.add(accountNumber);
                bank.put(userId, accounts);
            }
        }

        return bankAccount.getAccountNumber();
    }

    private long generateBankAccountNumber(){
        try{
            generateBankAccountNumberLock.lock();
            return BANK_ACCOUNT_COUNTER++;
        } finally {
            generateBankAccountNumberLock.unlock();
        }

    }

    public BankAccount getAccount(long accountNumber) {

        return numberToAccountMapping.get(accountNumber);
    }


    public List<BankAccount> getAllBankAccountsForUser(int userId) {
        List<Long> accountNumbers = bank.get(userId);
        if(accountNumbers == null){
            return null;
        }
        List<BankAccount> accounts = new ArrayList<>();
        accountNumbers.forEach(num -> accounts.add(numberToAccountMapping.get(num)));
        return accounts;
    }


    public boolean storeTransactionInfo(TransactionInfo tInfo, long accountNumber) {
        List<TransactionInfo> accountTransactions = transactions.get(accountNumber);
        return accountTransactions.add(tInfo);
    }

    public List<TransactionInfo> getAllTransactionInfo(long accountNumber) {

        return transactions.get(accountNumber);
    }



}
