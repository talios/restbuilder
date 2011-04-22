package com.theoryinpractise.restbuilder.it;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestletServerContainer {


    public RestletServerContainer(final Application restletApplication) {

        try {
            // Create a new Component.
            Component component = new Component();

            // Add a new HTTP server listening on port 8182.
            component.getServers().add(Protocol.HTTP, 8182);

            // Attach the sample application.
            component.getDefaultHost().attach(restletApplication);

            // Start the component.
            component.start();
        } catch (Exception e) {
            // Something is wrong.
            e.printStackTrace();
        }
    }

}
