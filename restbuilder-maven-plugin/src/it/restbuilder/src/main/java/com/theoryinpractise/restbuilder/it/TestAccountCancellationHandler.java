package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.Account;
import com.example.rbuilder.AccountIdentifier;
import com.example.rbuilder.handler.AccountCancellationHandler;
import com.example.rbuilder.operation.CancellationOperation;
import com.example.rbuilder.operation.NotifyOperation;

import java.util.Date;

public class TestAccountCancellationHandler implements AccountCancellationHandler {

    Account account = new Account(10, "Test Account", new Date(), new Date());

    public Account handleCancellation(final AccountIdentifier identifier, final CancellationOperation cancellation) {
        System.out.println("cancell");
        return account;
    }

}
