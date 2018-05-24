package com.banksystem.ui;

import com.banksystem.handlers.AccountHandler;

import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleUI {

    private AccountHandler accountHandler;

    public ConsoleUI(AccountHandler accountHandler){
        this.accountHandler = accountHandler;
    }


    /**
     * This is where the ui started and the menu loop beings
     */
    public void launch(){

        boolean userWantsToExit = false;
        try(InputReader reader = new InputReader(new InputStreamReader(System.in))){

            while(!userWantsToExit){
                showMainMenu();
                int choice = 0;
                switch(choice) {
                    case 1:

                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                    case 9:
                        break;
                    case 10:
                        System.out.println("Now exiting.");
                        userWantsToExit = true;
                        break;
                    case -2:
                        System.out.println("You have to supply an integer.");
                        break;
                    default:
                        System.out.println("That is not a valid menu option.");
                        break;
                }


            }
        } catch (IOException e) {
            System.err.println("An "+e+" was thrown when reading from system in.");
        }

    }

    /**
     * Prints main menu
     */
    public void showMainMenu() {
        System.out.println("---Main menu---");
        System.out.println("1. View all bank accounts");
        System.out.println("2. View balance on all accounts");
        System.out.println("3. View balance for a bank account");
        System.out.println("4. View transaction history for a bank account");
        System.out.println("5. Deposit money to a bank account");
        System.out.println("6. Withdraw money from a bank account");
        System.out.println("7. Make a transaction");
        System.out.println("8. Lock an account");
        System.out.println("9. Unlock an account");
        System.out.println("10. Exit");

    }




}
