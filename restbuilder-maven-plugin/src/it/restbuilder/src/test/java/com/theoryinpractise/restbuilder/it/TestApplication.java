package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.handler.AccountHandler;
import com.example.rbuilder.handler.AccountCancellationHandler;
import com.example.rbuilder.handler.AccountNotifyHandler;
import com.example.rbuilder.resource.AccountResource;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.fest.assertions.Assertions.assertThat;

public class TestApplication {


    private ApplicationBuilder server;

    @BeforeMethod
    public void startServer() {
        Module module = new Module() {
            public void configure(Binder binder) {
                binder.bind(AccountHandler.class).to(TestAccountHandler.class);
                binder.bind(AccountCancellationHandler.class).to(TestAccountCancellationHandler.class);
                binder.bind(AccountNotifyHandler.class).to(TestAccountNotifyHandler.class);
            }
        };

        server = new ApplicationBuilder(module)
                .attach(AccountResource.URI, AccountResource.class);

        server.startServer(8182);

    }

    @AfterMethod
    public void stopServer() {
        server.stopServer();
    }


    @Test
    public void testApplication() throws InterruptedException, ExecutionException, IOException {

        Map account = requestJsonAsMap("http://localhost:8182/account/43");
        assertThat(account.get("displayName")).isEqualTo("Test Account");

    }

    private Map requestJsonAsMap(final String url) throws IOException, InterruptedException, ExecutionException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        Future<Response> f = asyncHttpClient.prepareGet(url).execute();
        Response r = f.get();
        return new ObjectMapper().readValue(r.getResponseBody(), Map.class);
    }


}
