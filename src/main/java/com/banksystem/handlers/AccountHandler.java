package com.banksystem.handlers;

import com.banksystem.repository.BankDataStore;

public class AccountHandler {

    private BankDataStore bankDataStore;

    public AccountHandler(BankDataStore bankDataStore){
        this.bankDataStore = bankDataStore;
    }

}
