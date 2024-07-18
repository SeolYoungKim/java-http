package org.apache.catalina.startup;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.RequestHandler;
import org.apache.coyote.http11.RequestHandlerMapping;
import org.apache.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tomcat {

    private static final Logger log = LoggerFactory.getLogger(Tomcat.class);

    public void start(RequestHandlerMapping requestHandlerMapping) {
        SessionManager sessionManager = SessionManager.create();
        var connector = new Connector(new RequestHandler(requestHandlerMapping), sessionManager);
        connector.start();

        try {
            // make the application wait until we press any key.
            System.in.read();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("web server stop.");
            connector.stop();
        }
    }
}
