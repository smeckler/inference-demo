# Fuseki inference trigger problem demo project

[https://stackoverflow.com/questions/59952945/reasoning-in-apache-jena-fuseki-reload-dataset-or-trigger-inference](https://stackoverflow.com/questions/59952945/reasoning-in-apache-jena-fuseki-reload-dataset-or-trigger-inference)

## Demo project

This demo is basically a small Spring Boot application with an embedded Fuseki server (v3.14) that inserts data and checks for inference/reasoning results. In a loop, it inserts a statement into the triple store and performs queries to check for inferenced triples.

The [Fuseki configuration](src/main/resources/fuseki/config.ttl) has two datasets. The first one stores the data and the ontology (inference rule). The second one gets the data from the first dataset (union default graph) and runs an OWL reasoner.

start `mvn spring-boot:run`

## "Cat"-example

We have the rule that cats are animals. We create random resources of type cat. The reasoning should then infer that these resources are also animals. If the reasoning was triggered, the number of cats and animals is the same.

inserted statements: 

`<urn:demo:cat{rnd-nbmr}> a <urn:demo:cat> .`

ontology/rule: 

`<urn:demo:cat> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <urn:demo:animal> .`

queries:

`SELECT * WHERE { ?cat a <urn:demo:cat> }` 

`SELECT * WHERE { ?Animal a <urn:demo:animal> }`

## Problem

> This problem also applies to the standalone variant of Fuseki (v3.12 and v3.14).

**Expected behavior:**

The number of cats is equal to the number of animals. The reasoning dataset is updated (inference is executed) whenever new data is inserted into the data dataset.

**Actual behavior:**

The reasoning-engine performs only one inference at the beginning (1 cat, 1 animal), but not afterwards (2 cats, 1 animal). The reasoning can be forced by restarting Fuseki.

## Questions

Can we configurate Fuseki to update the "reasoning" dataset with every incoming data at the "data" dataset?

Is there a better way to trigger the reasoning than restarting Fuseki?