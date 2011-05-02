package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.SimpleAccountView;
import com.example.rbuilder.AccountIdentifier;
import com.example.rbuilder.handler.SimpleAccountHandler;
import com.example.rbuilder.operation.CancellationOperation;
import com.example.rbuilder.operation.NotifyOperation;

import java.util.Date;

public class TestSimpleAccountHandler implements SimpleAccountHandler {

    SimpleAccountView simpleAccount = new SimpleAccountView(10, "Test Account", "TA");

    public SimpleAccountView represent(final AccountIdentifier identifier) {
        System.out.println("Representing simple account for id " + identifier.getId());
        return simpleAccount;
    }

}
