package com.banksystem;

import com.banksystem.Exceptions.SetupNotCalledException;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AppTest {

    @Test(expected = SetupNotCalledException.class)
    public void testFaultyAppStartup() throws SetupNotCalledException{
        ExpectedException thrown = ExpectedException.none();
        thrown.expect(SetupNotCalledException.class);
        App app = new App();
        app.start();
    }
}
