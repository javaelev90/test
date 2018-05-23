package com.banksystem.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserTest {

    private User user;

    @Before
    public void setup(){
        user = new User();
    }

    @Test
    public void testCanSetId(){
        user.setId(1);
        Assert.assertEquals(1, user.getId());
    }

    @Test
    public void testCanUpdateFirstName(){

        user.setFirstName("Ivar");
        Assert.assertEquals("Ivar", user.getFirstName());
    }

    @Test
    public void testCanUpdateLastName(){

        user.setLastName("Sari");
        Assert.assertEquals("Sari", user.getLastName());
    }

}
