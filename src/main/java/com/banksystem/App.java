package com.banksystem;

import com.banksystem.Exceptions.SetupNotCalledException;
import com.banksystem.handlers.AccountHandler;
import com.banksystem.model.User;
import com.banksystem.repository.BankDataStore;
import com.banksystem.ui.ConsoleUI;

public class App {

    private boolean setupWasCalled = false;
    private ConsoleUI ui;


    /**
     * You start the app with this method, must be called after setup() method has been called
     */
    public void start() throws SetupNotCalledException{
        if(!setupWasCalled)
            throw new SetupNotCalledException();

        ui.launch();
    }


    /**
     *  Setup for the app before it is being started. Must be called before start()
     * @return if setup method finished correctly
     */
    public boolean setup(){
        BankDataStore bankDataStore = new BankDataStore();

        User user = new User();
        user.setFirstName("Mark");
        user.setLastName("Vega");
        user.setId(1);

        // Make two accounts for user
        bankDataStore.makeAccount(user.getId());
        bankDataStore.makeAccount(user.getId());


        ui = new ConsoleUI(new AccountHandler(bankDataStore), user);

        setupWasCalled = true;
        return true;
    }

}
