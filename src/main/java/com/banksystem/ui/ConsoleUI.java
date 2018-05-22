package com.banksystem.ui;

public class ConsoleUI {

    /**
     * This is where the ui started and the menu loop beings
     */
    public void launch(){

        boolean userWantsToExit = false;

        while(!userWantsToExit){

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
                    userWantsToExit = true;
                    break;
                default:
                    break;
            }


        }

    }

    /**
     * Prints main menu
     */
    public void showMainMenu() {
        System.out.println("---Main menu---");
        System.out.println("1. View all bank accounts");
    }




}
