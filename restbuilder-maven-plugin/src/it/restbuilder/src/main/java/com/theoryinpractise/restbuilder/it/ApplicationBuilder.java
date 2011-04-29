package com.theoryinpractise.restbuilder.it;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

public class ApplicationBuilder {

    Router router = new Router();
    private Module module;
    private Component component = null;

    public ApplicationBuilder(Module module) {
        this.module = module;
    }

    public Router getRouter() {
        return router;
    }

    public ApplicationBuilder attach(String path, Class<? extends Resource> resourceClass) {
        router.attach(path, new GuiceFinder(resourceClass));
        return this;
    }

    public Application buildApplication() {
        return new Application() {
            @Override
            public Restlet createRoot() {
                return router;
            }
        };
    }


    public void startServer(final int port) {
        if (component != null) {
            throw new UnsupportedOperationException("Server is already running");
        }

        try {
            // Create a new Component.
            component = new Component();

            // Add a new HTTP server listening on port 8182.
            component.getServers().add(Protocol.HTTP, port);

            // Attach the sample application.
            component.getDefaultHost().attach(buildApplication());

            // Start the component.
            component.start();
        } catch (Exception e) {
            // Something is wrong.
            e.printStackTrace();
        }

    }

    public void stopServer() {
        if (component == null) {
            throw new UnsupportedOperationException("No server started");
        }

        try {
            component.stop();
            component = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    private class GuiceFinder extends Finder {

        private Class<? extends Resource> resourceClass;

        public GuiceFinder(Class<? extends Resource> resourceClass) {
            this.resourceClass = resourceClass;
        }

        public Resource createTarget(final Request request, final Response response) {
            Injector injector = Guice.createInjector(module, new Module() {
                public void configure(Binder binder) {
                    binder.bind(Request.class).toInstance(request);
                    binder.bind(Response.class).toInstance(response);
                }
            });

            return injector.getInstance(resourceClass);

        }

    }

}
