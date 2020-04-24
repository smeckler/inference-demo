package de.fraunhofer.demo.fuseki;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class RulesUploader {

    private static final Logger log = LoggerFactory.getLogger(RulesUploader.class);

    @Value("${fuseki.rulesFileName}")
    private String rulesFileName;

    @Value("${fuseki.endpointUrl}")
    private String endpointUrl;

    @Value("${fuseki.datasetName}")
    private String datasetName;

    @Value("${fuseki.rulesGraphName}")
    private String rulesGraph;

    @Autowired
    private ResourceLoader resourceLoader;

    @EventListener(ApplicationReadyEvent.class)
    public void runUpload() {
        uploadRules();
    }

    private void uploadRules() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(endpointUrl + datasetName).queryEndpoint("query");

        log.error(endpointUrl + datasetName);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            log.info(conn.toString());
            conn.put(rulesGraph, loadOntologyModel());
            log.info("Rules uploaded successfully.");
        }
    }

    private Model loadOntologyModel() {
        Model ontmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            InputStream stream = resourceLoader.getResource("classpath:" + rulesFileName).getInputStream();
            ontmodel.read(stream, "", "TTL");
        } catch (IOException ioe) {
            log.error("OntologyModel loading failed. " + ioe.toString());
        }
        ontmodel.write(System.out, "N-TRIPLES");
        return ontmodel;
    }

}