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
public class OntologyUploader {

    private static final Logger log = LoggerFactory.getLogger(OntologyUploader.class);

    @Value("${fuseki.ontologyFileName}")
    private String ontologyFileName;

    @Value("${fuseki.endpointUrl}")
    private String endpointUrl;

    @Value("${fuseki.datasetName}")
    private String datasetName;

    @Value("${fuseki.ontologyGraphName}")
    private String ontologyGraph;

    @Autowired
    private ResourceLoader resourceLoader;

    @EventListener(ApplicationReadyEvent.class)
    public void runUpload() {
        uploadontology();
    }

    private void uploadontology() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(endpointUrl + datasetName).queryEndpoint("query");

        log.error(endpointUrl + datasetName);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            log.info(conn.toString());
            conn.put(ontologyGraph, loadOntologyModel());
            log.info("ontology uploaded successfully.");
        }
    }

    private Model loadOntologyModel() {
        Model ontmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            InputStream stream = resourceLoader.getResource("classpath:" + ontologyFileName).getInputStream();
            ontmodel.read(stream, "", "TTL");
        } catch (IOException ioe) {
            log.error("OntologyModel loading failed. " + ioe.toString());
        }
        ontmodel.write(System.out, "N-TRIPLES");
        return ontmodel;
    }

}