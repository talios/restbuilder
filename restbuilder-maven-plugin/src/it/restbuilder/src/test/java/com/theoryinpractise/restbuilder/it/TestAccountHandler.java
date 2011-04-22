package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.Account;
import com.example.rbuilder.AccountIdentifier;
import com.example.rbuilder.handler.AccountHandler;
import com.example.rbuilder.operation.CancellationOperation;
import com.example.rbuilder.operation.NotifyOperation;

import java.util.Date;

public class TestAccountHandler implements AccountHandler {

    Account account = new Account(10, "Test Account", new Date(), new Date());

    public Account represent(final AccountIdentifier identifier) {

        System.out.println("Representing account for id " + identifier.getId());

        return account;

    }

    public Account handleNotify(final AccountIdentifier identifier, final NotifyOperation notify) {
        System.out.println("notify");
        return account;
    }

    public Account handleCancellation(final AccountIdentifier identifier, final CancellationOperation cancellation) {
        System.out.println("cancell");
        return account;
    }

}
