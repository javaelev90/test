package com.banksystem;

import com.banksystem.Exceptions.SetupNotCalledException;

public class Main {

    public static void main(String[] args){

        App app = new App();
        app.setup();
        try {
            app.start();
        } catch (SetupNotCalledException e) {
            System.out.println("Setup method has to be called in App before start method.");
        }

    }

}
