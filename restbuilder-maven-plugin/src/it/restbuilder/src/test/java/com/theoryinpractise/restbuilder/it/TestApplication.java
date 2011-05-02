package com.theoryinpractise.restbuilder.it;

import com.example.rbuilder.ExampleRouteManager;
import com.example.rbuilder.handler.AccountCancellationHandler;
import com.example.rbuilder.handler.AccountHandler;
import com.example.rbuilder.handler.SimpleAccountHandler;
import com.example.rbuilder.handler.AccountNotifyHandler;
import com.google.common.base.Function;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.resource.Resource;
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

        final Module module = new Module() {
            public void configure(Binder binder) {
                binder.bind(AccountHandler.class).to(TestAccountHandler.class);
                binder.bind(SimpleAccountHandler.class).to(TestSimpleAccountHandler.class);
                binder.bind(AccountCancellationHandler.class).to(TestAccountCancellationHandler.class);
                binder.bind(AccountNotifyHandler.class).to(TestAccountNotifyHandler.class);
            }
        };

        server = new ApplicationBuilder(module);

        ExampleRouteManager.attachExampleModel(server.getRouter(), new Function<Class<? extends Resource> , Finder>() {
            public Finder apply(final Class<? extends Resource> resourceClass) {
                return new Finder() {
                    public Handler createTarget(final Request request, final org.restlet.data.Response response) {
                        final Injector injector = Guice.createInjector(module, new Module() {
                            @Override
                            public void configure(Binder binder) {
                                binder.bind(Request.class).toInstance(request);
                                binder.bind(org.restlet.data.Response.class).toInstance(response);
                            }
                        });

                        return injector.createChildInjector().getInstance(resourceClass);
                    }
                };
            }
        });

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
