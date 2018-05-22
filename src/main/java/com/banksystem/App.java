package com.banksystem;

import com.banksystem.Exceptions.SetupNotCalledException;

public class App {

    private boolean setupWasCalled = false;

    /**
     * You start the app with this method, must be called after setup() method has been called
     */
    public void start() throws SetupNotCalledException{
        if(!setupWasCalled)
            throw new SetupNotCalledException();

    }


    /**
     *  Setup for the app before it is being started. Must be called before start()
     * @return if setup method finished correctly
     */
    public boolean setup(){


        return true;
    }

}
