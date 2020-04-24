package de.fraunhofer.demo.fuseki;

import org.apache.jena.fuseki.main.FusekiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

@Component
public class FusekiStarter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FusekiStarter.class);

    private FusekiServer server;

    @Value("${fuseki.port}")
    private int port;

    @Value("${fuseki.configFile}")
    private String configFile;

    @Override
    public void run(String... args) throws Exception {
        startFusekiServer();
    }

    private void startFusekiServer() {
        try {
            server = FusekiServer.create()
                    .parseConfigFile(configFile)
                    .build();
            server.start();
        } catch (Exception ex) {
            log.warn("Error starting Fuseki server", ex);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping Fuseki server");
        if(server != null) {
            server.stop();
        }
    }
}
