package com.banksystem.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class InputReader extends BufferedReader {

    public InputReader(Reader in) {
        super(in);
    }

    public int readChoiceInput(){
        try {
            String choice = readLine();
            if(choice.equals("")){
                return -1;
            }
            return Integer.parseInt(choice);
        } catch (IOException e) {
            System.err.println(e);
            return 0;
        } catch (NumberFormatException e){
            return -2;
        }
    }

    public String readInput(){
        try{
            return readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns a positive integer
     * @return
     */
    public long enterBankAccountNumber(){
        String input = readInput();
        if(input.equals("")){
            //Empty string
            return -1;
        }
        try{
            long output = Long.parseLong(input);
            if(output < 0){
                //Less than zero
                return -2;
            }
            return output;
        } catch (NumberFormatException e){
            //Not a long number
            return -3;
        }

    }

    public double enterAmountOfMoney(){
        String input = readInput();
        if(input.equals("")){
            //Empty string
            return -1;
        }
        try{
            double output = Double.parseDouble(input);
            if(output < 0){
                //Less than zero
                return -2;
            }
            return output;
        } catch (NumberFormatException e){
            //Not a double number
            return -3;
        }
    }

}
