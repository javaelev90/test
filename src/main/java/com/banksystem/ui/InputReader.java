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


}
