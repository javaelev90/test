package com.banksystem;

import com.banksystem.Exceptions.SetupNotCalledException;
import com.banksystem.handlers.AccountHandler;
import com.banksystem.repository.BankDataStore;
import com.banksystem.ui.ConsoleUI;

public class App {

    private boolean setupWasCalled = false;
    private BankDataStore bankDataStore;


    /**
     * You start the app with this method, must be called after setup() method has been called
     */
    public void start() throws SetupNotCalledException{
        if(!setupWasCalled)
            throw new SetupNotCalledException();
        AccountHandler acccountHandler = new AccountHandler(bankDataStore);
        ConsoleUI ui = new ConsoleUI(acccountHandler);
        ui.launch();
    }


    /**
     *  Setup for the app before it is being started. Must be called before start()
     * @return if setup method finished correctly
     */
    public boolean setup(){
        bankDataStore = new BankDataStore();
        // Make two accounts
        bankDataStore.makeAccount(1);
        bankDataStore.makeAccount(1);

        setupWasCalled = true;
        return true;
    }

}
