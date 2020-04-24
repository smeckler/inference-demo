package de.fraunhofer.demo.fuseki;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
@EnableScheduling
public class DataWorker {

    private Random random = new Random();
    private static final Logger log = LoggerFactory.getLogger(DataWorker.class);

    @Value("${fuseki.endpointUrl}")
    private String endpointUrl;

    @Value("${fuseki.datasetName}")
    private String datasetName;
    @Value("${fuseki.reasoningDatasetName}")
    private String reasoningDatasetName;

    @Value("${fuseki.dataGraphName}")
    private String dataGraph;


    @Scheduled(initialDelay = 5000, fixedDelayString = "${fuseki.interval}") //wait 5 seconds after completion
    public void runDemo() throws InterruptedException {
        uploadData();
        Thread.sleep(5000);
        queryTriples();
    }

    private void uploadData() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(endpointUrl + datasetName).queryEndpoint("query");

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Model data = createRandomData();
            conn.load(dataGraph, data);
            log.info(data.size() + " triples successfully uploaded to data graph.");
        }
    }

    private void queryTriples() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(endpointUrl + reasoningDatasetName).queryEndpoint("query");

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // query animals and cats in demodataset
            Query queryAnimals = QueryFactory.create("SELECT * WHERE { ?Animal a <urn:demo:animal> }");
            Query queryCats = QueryFactory.create("SELECT * WHERE { ?cat a <urn:demo:cat> }");
            ResultSet setAnimals = conn.query(queryAnimals).execSelect();
            ResultSet setCats = conn.query(queryCats).execSelect();
            ResultSetFormatter.out(setAnimals);
            ResultSetFormatter.out(setCats);

            // output number of triples for animals (created by inference) and cats (manually inserted)
            log.warn(setAnimals.getRowNumber() + " animals in reasoning dataset, should be: " + setCats.getRowNumber());
        }
    }

    private Model createRandomData() {
        Model model = ModelFactory.createDefaultModel();
        String caturi = String.format("urn:demo:cat%d", random.nextInt(10000));
        model.createResource(caturi).addProperty(RDF.type, model.createResource("urn:demo:cat"));
        return model;
    }
}