package com.example.datafusion;

import com.github.jsonldjava.utils.Obj;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.atlas.iterator.Iter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scify.jedai.blockbuilding.IBlockBuilding;
import org.scify.jedai.blockbuilding.StandardBlocking;
import org.scify.jedai.blockprocessing.blockcleaning.ComparisonsBasedBlockPurging;
import org.scify.jedai.blockprocessing.comparisoncleaning.CardinalityEdgePruning;
import org.scify.jedai.datamodel.*;
import org.scify.jedai.datareader.entityreader.EntityRDFReader;
import org.scify.jedai.entityclustering.IEntityClustering;
import org.scify.jedai.entityclustering.UniqueMappingClustering;
import org.scify.jedai.entitymatching.IEntityMatching;
import org.scify.jedai.entitymatching.ProfileMatcher;
import org.scify.jedai.schemaclustering.HolisticAttributeClustering;
import org.scify.jedai.schemaclustering.ISchemaClustering;
import org.scify.jedai.similarityjoins.ISimilarityJoin;
import org.scify.jedai.similarityjoins.tokenbased.PPJoin;
import org.scify.jedai.utilities.enumerations.EntityMatchingMethod;
import org.scify.jedai.utilities.enumerations.RepresentationModel;
import org.scify.jedai.utilities.enumerations.SimilarityMetric;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Attr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SpringBootApplication
public class DataFusionApplication {

    private static final String SYSTEMPATH = "C:\\Users\\43676\\Documents\\GitHub\\dataFusion\\out";

    public static void main(String[] args) {
        SpringApplication.run(DataFusionApplication.class, args);
        /*
        File file = new File(SYSTEMPATH);
        String[] files = file.list();
        OntModel model = ModelFactory.createOntologyModel();
        OntClass airport = model.createClass("http://www.semanticweb.org/GregorKaefer/vasqua#Airport");
        for(String pathname: files) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(SYSTEMPATH + "\\" + pathname)));
                JSONObject jsonObject = new JSONObject(json);
                Iterator<String> keys = jsonObject.keys();
                Individual airportIndividual = airport.createIndividual(airport.getURI() + "-" +
                        pathname.replaceAll(".txt","").replaceAll("airport-",""));
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (jsonObject.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = jsonObject.getJSONArray(key);
                        int length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            Iterator<String> keys1 = jsonObject1.keys();
                            while (keys1.hasNext()) {
                                String key1 = keys1.next();
                                if (jsonObject.get(key) instanceof JSONObject) {
                                    DatatypeProperty datatypeProperty1 = model.createDatatypeProperty("http://www.semanticweb.org/GregorKaefer/vasqua#" + key + "-" + key1);
                                    airportIndividual.addProperty(datatypeProperty1, jsonObject1.getString(key1));
                                }
                            }
                        }
                    } else if (jsonObject.get(key) instanceof JSONObject) {
                        if (jsonObject.get(key).toString().equals("{}")) continue;
                        if (jsonObject.get(key) instanceof JSONObject) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject(key);
                            Iterator<String> keys1 = jsonObject1.keys();
                            while (keys1.hasNext()) {
                                String key1 = keys1.next();
                                if (jsonObject.get(key) instanceof JSONObject) {
                                    DatatypeProperty datatypeProperty1 = model.createDatatypeProperty("http://www.semanticweb.org/GregorKaefer/vasqua#" + key + "-" + key1);
                                    airportIndividual.addProperty(datatypeProperty1, jsonObject1.getString(key1));
                                }
                            }
                        }else {
                            DatatypeProperty datatypeProperty = model.createDatatypeProperty("http://www.semanticweb.org/GregorKaefer/vasqua#" + key);
                            airportIndividual.addProperty(datatypeProperty, jsonObject.getString(key));
                        }
                    } else {
                        DatatypeProperty datatypeProperty1 = model.createDatatypeProperty("http://www.semanticweb.org/GregorKaefer/vasqua#" + key);
                        airportIndividual.addProperty(datatypeProperty1, jsonObject.getString(key));

                    }
                }
                FileOutputStream out = null;
                try {
                    Path path = Paths.get("C:/Users/43676/Documents/GitHub/dataFusion/src/main/resources/airport1.ttl");
                    out = new FileOutputStream(path.toString());
                } catch (IOException e) {
                    System.out.println(e);
                }
                model.write(out, "TTL");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String splitter = ",";
        String line = "";
        OntModel model1 = ModelFactory.createOntologyModel();
        OntClass airport1 = model1.createClass("http://www.semanticweb.org/GregorKaefer/vasqua#Airport");
        int count = 0;
        String[] keys1 = {};
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("C:\\Users\\43676\\Documents\\GitHub\\dataFusion\\src\\main\\resources\\query(3).csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                if (!((line = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (count == 0) {
                keys1 = line.split(splitter);
                count++;
            } else {
                String[] temp = line.split(splitter);
                Individual airportIndividual = airport1.createIndividual(airport.getURI() + "-" + UUID.randomUUID().toString());
                for(int i = 0;i<temp.length;i++) {
                    String keyVar = "";
                    if(keys1[i].equals("IATA_airport_code")) {
                        keyVar = "id";
                    } else {
                        keyVar = keys1[i];
                    }
                    DatatypeProperty datatypeProperty1 = model1.createDatatypeProperty("http://www.semanticweb.org/GregorKaefer/vasqua#" + keyVar);
                    airportIndividual.addProperty(datatypeProperty1, temp[i]);
                }
                count++;
            }
        }

        FileOutputStream out = null;
        try {
            Path path = Paths.get("C:/Users/43676/Documents/GitHub/dataFusion/src/main/resources/airport2.ttl");
            out = new FileOutputStream(path.toString());
        } catch (IOException e) {
            System.out.println(e);
        }
        model1.write(out, "TTL");

        EntityRDFReader entityRDFReader = new EntityRDFReader("C:\\Users\\43676\\Documents\\GitHub\\dataFusion\\src\\main\\resources\\airport1.ttl");
        EntityRDFReader entityRDFReader1 = new EntityRDFReader("C:\\Users\\43676\\Documents\\GitHub\\dataFusion\\src\\main\\resources\\airport2.ttl");

        ISimilarityJoin iSimilarityJoin = null;
        PPJoin ppJoin = new PPJoin(0.8f);
        iSimilarityJoin = ppJoin;
        List<EntityProfile> profile1, profile2;
        profile1 = entityRDFReader.getEntityProfiles();
        profile2 = entityRDFReader1.getEntityProfiles();
        System.out.println(profile1.size());
        System.out.println(profile2.size());
        IEntityClustering entityClustering = null;
        UniqueMappingClustering uniqueMappingClustering = new UniqueMappingClustering();
        entityClustering = uniqueMappingClustering;
        List<String> join_attr = new ArrayList<>();
        join_attr.add("http://www.semanticweb.org/GregorKaefer/vasqua#id");
        join_attr.add("http://www.semanticweb.org/GregorKaefer/vasqua#id");
        SimilarityPairs similarityPairs = iSimilarityJoin.executeFiltering(join_attr.get(0),
                join_attr.get(1), profile1, profile2);
        EquivalenceCluster[] clusters = entityClustering.getDuplicates(similarityPairs);
        for(EquivalenceCluster cluster: clusters) {
            if (cluster.getEntityIdsD1().size() != 1
                    || cluster.getEntityIdsD2().size() != 1) {
                continue;
            }
            final int entityId1 = cluster.getEntityIdsD1().get(0);
            final EntityProfile profile12 = profile1.get(entityId1);

            final int entityId2 = cluster.getEntityIdsD2().get(0);
            final EntityProfile profile22 = profile2.get(entityId2);
            Set<Attribute> attributeSet12 = profile12.getAttributes();
            Set<Attribute> attributeSet22 = profile22.getAttributes();
            String id12 = "";
            String id22 = "";
            Iterator<Attribute> iterator = attributeSet12.iterator();
            while(iterator.hasNext()) {
                Attribute attribute = iterator.next();
                if(attribute.getName().contains("id")) {
                    id12 = attribute.getValue();
                    break;
                }
            }

            Iterator<Attribute> iterator1 = attributeSet22.iterator() ;
            while(iterator1.hasNext()) {
                Attribute attribute = iterator1.next();
                if(attribute.getName().contains("id")) {
                    id22 = attribute.getValue();
                    break;
                }
            }
            System.out.println(profile12.getEntityUrl()+ " with id " + id12 +
                    " match with " + profile22.getEntityUrl() + " with id " + id22);
        }

        RepresentationModel representationModel = RepresentationModel.TOKEN_UNIGRAMS;
        SimilarityMetric similarityMetric = SimilarityMetric.getModelDefaultSimMetric(representationModel);
        ISchemaClustering schemaClustering = new HolisticAttributeClustering(representationModel, SimilarityMetric.COSINE_SIMILARITY);
        IBlockBuilding blockBuilding = new StandardBlocking();
        List<AbstractBlock> blocks = new ArrayList<>();
        //AttributeClusters[] clusters1 = schemaClustering.getClusters(profile1, profile2);
        ComparisonsBasedBlockPurging comparisonsBasedBlockPurging = new ComparisonsBasedBlockPurging(true);
        CardinalityEdgePruning cardinalityEdgePruning = new CardinalityEdgePruning();
        //IEntityMatching entityMatching = new ProfileMatcher(profile1, profile2);
        IEntityMatching entityMatching = EntityMatchingMethod.getDefaultConfiguration(profile1, profile2, EntityMatchingMethod.PROFILE_MATCHER);
        blocks.addAll(blockBuilding.getBlocks(profile1, profile2));
        SimilarityPairs similarityPairs1 = entityMatching.executeComparisons(blocks);
        clusters = entityClustering.getDuplicates(similarityPairs1);
        for(EquivalenceCluster cluster: clusters) {
            if (cluster.getEntityIdsD1().size() != 1
                    || cluster.getEntityIdsD2().size() != 1) {
                continue;
            }
            final int entityId1 = cluster.getEntityIdsD1().get(0);
            final EntityProfile profile12 = profile1.get(entityId1);

            final int entityId2 = cluster.getEntityIdsD2().get(0);
            final EntityProfile profile22 = profile2.get(entityId2);
            System.out.println(profile12.getEntityUrl()+ " match with " + profile22.getEntityUrl());
        }


    }


         */
    }
}
