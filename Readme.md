# Example of Identity Resolution Using [Jedai](https://jedai.scify.org/)

## Installation

create a jar file using

```console
mvn clean package
```

and run it using

```console
mvn spring-boot:run
```

## Endpoint

There are currently 3 Endpoint available

```console
http://localhost:8081/workflow1
```
Workflow 1 is using jedai Blocking Based Workflow
which is:
Data Reading -> Schema Clustering -> Block Building ->
Block Cleaning -> Comparison Cleaning -> Entity Matching ->
Entity Clustering -> Data Writing & Evaluation

this endpoint takes 2 files in form of an RDF data
```console
http://localhost:8081/workflow2
```
Workflow 2 is using jedai Join Based Workflow
which is:
Data Reading -> Similarity Join -> Entity Clustering -> Data Writing & Evaluation

this endpoint takes 2 files in form of an RDF data
```console
http://localhost:8081/workflow3
```
Workflow 3 is using jedai Progressive Workflow
which is:
Data Reading -> Schema Clustering -> Block Building ->
Block Cleaning -> Comparison Prioritization -> Entity Matching ->
Data Writing & Evaluation

this endpoint takes 2 files in form of an RDF data



