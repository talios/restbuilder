package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.Account;
import com.example.rbuilder.handler.AccountHandler;
import com.example.rbuilder.operation.CancellationOperation;
import com.example.rbuilder.operation.NotifyOperation;

import java.util.Date;

public class TestAccountHandler implements AccountHandler {

    Account account = new Account(new Date(), new Date(), "Test Account", 10);

    public Account represent() {

        System.out.println("Representing accounts...");

        return account;

    }

    public Account handleNotify(final NotifyOperation notify) {
        System.out.println("notify");
        return account;
    }

    public Account handleCancellation(final CancellationOperation cancellation) {
        System.out.println("cancell");
        return account;
    }

}
