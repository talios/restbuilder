package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.Account;
import com.example.rbuilder.AccountIdentifier;
import com.example.rbuilder.handler.AccountNotifyHandler;
import com.example.rbuilder.operation.CancellationOperation;
import com.example.rbuilder.operation.NotifyOperation;

import java.util.Date;

public class TestAccountNotifyHandler implements AccountNotifyHandler {

    Account account = new Account(10, "Test Account", new Date(), new Date());

    public Account handleNotify(final AccountIdentifier identifier, final NotifyOperation notify) {
        System.out.println("notify");
        return account;
    }

}
