package com.banksystem.model;

import org.junit.Assert;
import org.junit.Test;

public class UserTest {

    @Test
    public void canMakeUser(){
        User user = new User("Åsa", "Marklund");

        Assert.assertEquals("Åsa", user.getFirstName());
        Assert.assertEquals("Marklund", user.getLastName());
    }

    @Test
    public void canUpdateUser(){
        User user = new User("Pelle", "Sari");

        user.setFirstName("Ivar");
        Assert.assertEquals("Ivar", user.getFirstName());
        user.setLastName("Marklund");
        Assert.assertEquals("Marklund", user.getLastName());
    }
}
