package com.example.datafusion.Service;

import org.scify.jedai.blockbuilding.IBlockBuilding;
import org.scify.jedai.blockbuilding.StandardBlocking;
import org.scify.jedai.blockprocessing.IBlockProcessing;
import org.scify.jedai.blockprocessing.blockcleaning.BlockFiltering;
import org.scify.jedai.blockprocessing.blockcleaning.ComparisonsBasedBlockPurging;
import org.scify.jedai.blockprocessing.comparisoncleaning.CardinalityEdgePruning;
import org.scify.jedai.datamodel.*;
import org.scify.jedai.datareader.entityreader.EntityRDFReader;
import org.scify.jedai.entityclustering.IEntityClustering;
import org.scify.jedai.entityclustering.UniqueMappingClustering;
import org.scify.jedai.entitymatching.GroupLinkage;
import org.scify.jedai.entitymatching.IEntityMatching;
import org.scify.jedai.entitymatching.ProfileMatcher;
import org.scify.jedai.prioritization.AbstractHashBasedPrioritization;
import org.scify.jedai.prioritization.GlobalProgressiveSortedNeighborhood;
import org.scify.jedai.prioritization.IPrioritization;
import org.scify.jedai.prioritization.ProgressiveGlobalRandomComparisons;
import org.scify.jedai.schemaclustering.HolisticAttributeClustering;
import org.scify.jedai.schemaclustering.ISchemaClustering;
import org.scify.jedai.similarityjoins.ISimilarityJoin;
import org.scify.jedai.similarityjoins.tokenbased.PPJoin;
import org.scify.jedai.utilities.enumerations.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class FusionService {

    /**
     * Workflow 2 (Join-Based) = Data Reading -> Similarity Join -> Entity Clustering -> Data Writing
     * & Evaluation.
     * There are actually 2 types of workflow 2
     * the first one called Clean-Clean ER = receives as input two overlapping data sources that are indi-
     * vidually duplicate-free and aims to identify all duplicate pairs between the 2 data sources
     * the second one called Dirty ER = takes as input a single
     * set of resources S that contains duplicates in itself and aims to detect all pairs
     * this function only focused on the Clean-Clean ER
     * how to implement the Dirty ER is basically the same, but there are different algorithm to use in
     * some of the functions
     * @param file1 data 1 that wanted to be compared
     * @param file2 data 2 that wanted to be compared
     * @return Match data in a map form
     */
    public Map<Integer, Map<String, Map<String, String>>> workflow2(File file1, File file2) {

        Map<Integer, Map<String, Map<String, String>>> result = new HashMap<>();
        int count = 1;

        // Read both files using the built in function from jedai
        // EntityRDFReader -> for rdf file
        // EntitySPARQLReader -> for sparql file
        // EntityCSVReader -> for csv file
        // EntityJSONReader -> for json file
        // EntityXMLReader -> for xml file
        EntityRDFReader entityRDFReader = new EntityRDFReader(file1.getAbsolutePath());
        EntityRDFReader entityRDFReader1 = new EntityRDFReader(file2.getAbsolutePath());

        // Preparing similarityJoin
        ISimilarityJoin iSimilarityJoin = null;
        // one of the similarity join algorithm
        // list of similarityJoin available =
        // token based = AllPairs, PPJoin, SilkMoth
        // character based = FastSS, PassJoin, PartEnum, EdJoin, AllPairs
        // User can defined the similarity threshold
        PPJoin ppJoin = new PPJoin(0.8f);
        iSimilarityJoin = ppJoin;

        // getting the representation of a single entity or record (Part of Data Reading).
        List<EntityProfile> profile1, profile2;
        profile1 = entityRDFReader.getEntityProfiles();
        profile2 = entityRDFReader1.getEntityProfiles();

        // Preparing entity clustering
        IEntityClustering entityClustering = null;

        // For Clean-Clean ER there are = UniqueMappingClustering, RowColumnClustering, and BestAssignmentClustering
        // For Dirty ER = ConnectedComponentsClustering, CenterClustering, MergeCenterClustering, RicochetSrClustering,
        // CorrelationClustering, MarkovClustering, and CutClustering
        // more information regarding those algorithms can be found in the readme section of
        // https://github.com/scify/JedAIToolkit
        // User can also set the clustering threshold if needed
        UniqueMappingClustering uniqueMappingClustering = new UniqueMappingClustering();
        entityClustering = uniqueMappingClustering;
        List<String> join_attr = new ArrayList<>();

        // add the key that want to be compared to, the first key is for file 1, second key is for file 2
        join_attr.add("http://www.semanticweb.org/GregorKaefer/vasqua#id");
        join_attr.add("http://www.semanticweb.org/GregorKaefer/vasqua#id");

        // execute the similarityjoin
        SimilarityPairs similarityPairs = iSimilarityJoin.executeFiltering(join_attr.get(0),
                join_attr.get(1), profile1, profile2);

        // Getting all the duplicates found
        EquivalenceCluster[] clusters = entityClustering.getDuplicates(similarityPairs);

        // Loop through the result
        for (EquivalenceCluster cluster : clusters) {

            // Duplicate not found so continue
            if (cluster.getEntityIdsD1().size() != 1
                    || cluster.getEntityIdsD2().size() != 1) {
                continue;
            }

            Map<String, Map<String, String>> resultTemp = new HashMap<>();
            Map<String, String> attributeTemp = null;
            // getting entityId 1
            final int entityId1 = cluster.getEntityIdsD1().get(0);

            // getting entity profile 1
            final EntityProfile profile12 = profile1.get(entityId1);

            // getting entityId 2
            final int entityId2 = cluster.getEntityIdsD2().get(0);

            // getting entity profile 2
            final EntityProfile profile22 = profile2.get(entityId2);

            // prepare the result as a map
            Set<Attribute> attributeSet12 = profile12.getAttributes();
            Set<Attribute> attributeSet22 = profile22.getAttributes();
            Iterator<Attribute> iterator = attributeSet12.iterator();
            if (iterator != null) attributeTemp = new HashMap<>();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                attributeTemp.put(attribute.getName(), attribute.getValue());
            }
            if (attributeTemp != null) resultTemp.put("airport1", attributeTemp);
            attributeTemp = null;
            Iterator<Attribute> iterator1 = attributeSet22.iterator();
            if (iterator1 != null) attributeTemp = new HashMap<>();
            while (iterator1.hasNext()) {
                Attribute attribute = iterator1.next();
                attributeTemp.put(attribute.getName(), attribute.getValue());
            }
            if (attributeTemp != null) resultTemp.put("airport2", attributeTemp);
            result.put(count, resultTemp);
            count++;
        }
        return result;
    }

    /**
     * Workflow 1 (Blocking Based) = Data Reading -> Schema Clustering -> Block Building -> Block Cleaning
     * -> Comparison Cleaning -> Entity Matching -> Entity Clustering -> Data Writing & Evaluation.
     * There are actually 2 types of workflow 1
     * the first one called Clean-Clean ER = receives as input two overlapping data sources that are indi-
     * vidually duplicate-free and aims to identify all duplicate pairs between the 2 data sources
     * the second one called Dirty ER = takes as input a single
     * set of resources S that contains duplicates in itself and aims to detect all pairs
     * this function only focused on the Clean-Clean ER
     * how to implement the Dirty ER is basically the same, but there are different algorithm to use in
     * some of the functions
     * @param file1 data 1 that wanted to be compared
     * @param file2 data 2 that wanted to be compared
     * @return Match data in a map form
     */
    public Map<Integer, Map<String, Map<String, String>>> workflow1(File file1, File file2) {


        Map<Integer, Map<String, Map<String, String>>> result = new HashMap<>();
        int count = 1;

        // Read both files using the built in function from jedai
        // EntityRDFReader -> for rdf file
        // EntitySPARQLReader -> for sparql file
        // EntityCSVReader -> for csv file
        // EntityJSONReader -> for json file
        // EntityXMLReader -> for xml file
        EntityRDFReader entityRDFReader = new EntityRDFReader(file1.getAbsolutePath());
        EntityRDFReader entityRDFReader1 = new EntityRDFReader(file2.getAbsolutePath());
        // String[] attributesNamesToExclude = new String[0];
        // add attributes to exclude if needed
        // entityRDFReader.setAttributesToExclude(attributesNamesToExclude)
        List<EntityProfile> profile1, profile2;
        // getting the representation of a single entity or record (Part of Data Reading).
        profile1 = entityRDFReader.getEntityProfiles();
        profile2 = entityRDFReader1.getEntityProfiles();

        // Preparing SchemaClustering if needed
        // There are 3 types of schemaclustering
        // Attribute Name = clustering based on key
        // Attribute Value = clustering based on value
        // Hollistic Attribute = clustering based on combination between key and value
        // for more detailed information see http://www.vldb.org/pvldb/vol9/p312-papadakis.pdf

        //RepresentationModel representationModel = RepresentationModel.TOKEN_UNIGRAMS;
        //SimilarityMetric similarityMetric = SimilarityMetric.getModelDefaultSimMetric(representationModel);
        //ISchemaClustering schemaClustering = new HolisticAttributeClustering(representationModel, SimilarityMetric.COSINE_SIMILARITY);
        //AttributeClusters[] clusters1 = schemaClustering.getClusters(profile1, profile2);


        // Preparing blockbuilding
        IBlockBuilding blockBuilding = new StandardBlocking();
        List<AbstractBlock> blocks = new ArrayList<>();
        EquivalenceCluster clusters[] = new EquivalenceCluster[0];

        // Preparing block cleaning method
        // Available block cleaning method for both Clean-Clean ER & Dirty ER =
        // Size-based block Purging, Cardinality-based block Purging, Block Filtering, and Block Clustering
        // All methods are optional, but complementary with each other and can be used in combination
        // For more information see http://www.vldb.org/pvldb/vol9/p684-papadakis.pdf
        IBlockProcessing blockCleaning = BlockCleaningMethod.getDefaultConfiguration(BlockCleaningMethod.BLOCK_FILTERING);

        // Preparing comparison cleaning method
        // Available block cleaning method for both Clean-Clean ER & Dirty ER =
        // Comparison Propagation, Cardinality Edge Pruning, Cardinality Node Pruning, Weighed Edge Pruning,
        // Weighed Node Pruning, Reciprocal Cardinality Node Pruning, Reciprocal Weighed Node Pruning,
        // BLAST, Canopy Clustering, and Extended Canopy Clustering
        // All methods are optional, but competive, in the sense that only one of them can be part of
        // ER workflow
        // for more information see https://www.sciencedirect.com/science/article/abs/pii/S2214579616300168
        // All of the methods above can be combined with one of the following weighting schemes =
        // Aggregate Reciprocal Comparisons Scheme, Common Blocks Scheme, Enhanced Common Blocks Scheme,
        // Jaccard Scheme, Enhanced Jaccard Scheme, and Pearson chi-squared test.
        IBlockProcessing comparisonCleaning = ComparisonCleaningMethod.getDefaultConfiguration(ComparisonCleaningMethod.CARDINALITY_EDGE_PRUNING);

        // example of comparsionCleaning with a weighted scheme
        // CardinalityEdgePruning cardinalityEdgePruning = new CardinalityEdgePruning(WEIGHTEDSCHEME);
        // comparisonCleaning = cardinalityEdgePruning

        // Preparing Entity Matching
        // Following schema-agnostic method are currently available =
        // Group Linkage -> more information see http://pike.psu.edu/publications/icde07.pdf
        // Profile Matcher -> which aggregates all attributes values in an individual entity into a textual representation
        // Both methods can be combined with the following representation model
        // character n-grams, character n-grams graphs, token n-grams, token n-grams graphs
        // for more information see https://link.springer.com/article/10.1007/s11280-015-0365-x
        // the bag models (the one without graphs in its name) can be combined with the following similarity
        // measures =
        // ARCS similarity, Cosine similarity, Jaccard similarity, Generalized Jaccard similarity, and Enhanced
        // Jaccard similarity
        IEntityMatching entityMatching = new ProfileMatcher(profile1, profile2);
        // example entity matching with a representation model and similarity measure
        // change model with one of the representation model, and measure with one of the similarity measure
        // IEntityMatching entityMatching =  new ProfileMatcher(profile1, profile2, model, measure)

        // add all blocks from block building
        blocks.addAll(blockBuilding.getBlocks(profile1, profile2));
        // clean the blocks if needed
        blocks = blockCleaning.refineBlocks(blocks);
        // clean the comparison if needed
        blocks = comparisonCleaning.refineBlocks(blocks);
        // run entity matching
        SimilarityPairs similarityPairs1 = entityMatching.executeComparisons(blocks);
        // Prepare entity clustering
        // Current available entity clustering methods for Dirty ER =
        // Connected Components Clustering, Center Clustering, Merge-Center Clustering, Ricochet SR Clustering,
        // Correlation Clustering, Markov Clustering, Cut Clustering
        // for more information see http://dblab.cs.toronto.edu/~fchiang/docs/vldb09.pdf
        // Current available entity clustering methods for Clean-Clean ER =
        // Unique Mapping Clustering, Row-Column Clustering, Best Assignment Clustering
        // for more information see https://github.com/scify/JedAIToolkit
        IEntityClustering entityClustering = null;
        // User can also specify the threshold
        UniqueMappingClustering uniqueMappingClustering = new UniqueMappingClustering(0.5f);
        entityClustering = uniqueMappingClustering;
        // getting the duplicates
        clusters = entityClustering.getDuplicates(similarityPairs1);
        // Loop through the result
        for (EquivalenceCluster cluster : clusters) {
            // duplicate not found so continue
            if (cluster.getEntityIdsD1().size() != 1
                    || cluster.getEntityIdsD2().size() != 1) {
                continue;
            }

            // same as above for workflow 2
            Map<String, Map<String, String>> resultTemp = new HashMap<>();
            Map<String, String> attributeTemp = null;
            final int entityId1 = cluster.getEntityIdsD1().get(0);
            final EntityProfile profile12 = profile1.get(entityId1);

            final int entityId2 = cluster.getEntityIdsD2().get(0);
            final EntityProfile profile22 = profile2.get(entityId2);

            Set<Attribute> attributeSet12 = profile12.getAttributes();
            Set<Attribute> attributeSet22 = profile22.getAttributes();
            Iterator<Attribute> iterator = attributeSet12.iterator();
            if (iterator != null) attributeTemp = new HashMap<>();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                attributeTemp.put(attribute.getName(), attribute.getValue());
            }
            if (attributeTemp != null) resultTemp.put("airport1", attributeTemp);
            attributeTemp = null;
            Iterator<Attribute> iterator1 = attributeSet22.iterator();
            if (iterator1 != null) attributeTemp = new HashMap<>();
            while (iterator1.hasNext()) {
                Attribute attribute = iterator1.next();
                attributeTemp.put(attribute.getName(), attribute.getValue());
            }
            if (attributeTemp != null) resultTemp.put("airport2", attributeTemp);
            result.put(count, resultTemp);
            count++;

        }
        return result;
    }

    /**
     * Workflow 3 (Progressive) = Data Reading -> Schema Clustering -> Block Building -> Block Cleaning
     * -> Comparison Prioritization -> Entity Matching -> Data Writing & Evaluation.
     * There are actually 2 types of workflow 3
     * the first one called Clean-Clean ER = receives as input two overlapping data sources that are indi-
     * vidually duplicate-free and aims to identify all duplicate pairs between the 2 data sources
     * the second one called Dirty ER = takes as input a single
     * set of resources S that contains duplicates in itself and aims to detect all pairs
     * this function only focused on the Clean-Clean ER
     * how to implement the Dirty ER is basically the same, but there are different algorithm to use in
     * some of the functions
     * @param file1 data 1 that wanted to be compared
     * @param file2 data 2 that wanted to be compared
     * @return Match data in a map form
     */
    public Map<Integer, Map<String, Map<String, String>>> workflow3(File file1, File file2) {


        Map<Integer, Map<String, Map<String, String>>> result = new HashMap<>();
        int count = 1;
        // Read both files using the built in function from jedai
        // EntityRDFReader -> for rdf file
        // EntitySPARQLReader -> for sparql file
        // EntityCSVReader -> for csv file
        // EntityJSONReader -> for json file
        // EntityXMLReader -> for xml file
        EntityRDFReader entityRDFReader = new EntityRDFReader(file1.getAbsolutePath());
        EntityRDFReader entityRDFReader1 = new EntityRDFReader(file2.getAbsolutePath());
        // String[] attributesNamesToExclude = new String[0];
        // add attributes to exclude if needed
        // entityRDFReader.setAttributesToExclude(attributesNamesToExclude)
        List<EntityProfile> profile1, profile2;
        // getting the representation of a single entity or record (Part of Data Reading).
        profile1 = entityRDFReader.getEntityProfiles();
        profile2 = entityRDFReader1.getEntityProfiles();

        // Preparing SchemaClustering if needed
        // There are 3 types of schemaclustering
        // Attribute Name = clustering based on key
        // Attribute Value = clustering based on value
        // Hollistic Attribute = clustering based on combination between key and value
        // for more detailed information see http://www.vldb.org/pvldb/vol9/p312-papadakis.pdf

        // RepresentationModel representationModel = RepresentationModel.TOKEN_UNIGRAMS;
        // SimilarityMetric similarityMetric = SimilarityMetric.getModelDefaultSimMetric(representationModel);
        // ISchemaClustering schemaClustering = new HolisticAttributeClustering(representationModel, SimilarityMetric.COSINE_SIMILARITY);
        // AttributeClusters[] clusters1 = schemaClustering.getClusters(profile1, profile2);

        // Preparing block building
        IBlockBuilding blockBuilding = new StandardBlocking();
        List<AbstractBlock> blocks = new ArrayList<>();
        EquivalenceCluster clusters[] = new EquivalenceCluster[0];
        //IBlockProcessing blockCleaning = BlockCleaningMethod.getDefaultConfiguration(BlockCleaningMethod.BLOCK_FILTERING);

        // Preparing block cleaning method
        // Available block cleaning method for both Clean-Clean ER & Dirty ER =
        // Size-based block Purging, Cardinality-based block Purging, Block Filtering, and Block Clustering
        // All methods are optional, but complementary with each other and can be used in combination
        // For more information see http://www.vldb.org/pvldb/vol9/p684-papadakis.pdf
        IBlockProcessing blockCleaning = new BlockFiltering();

        // Preparing comparison cleaning method
        // Available block cleaning method for both Clean-Clean ER & Dirty ER =
        // Comparison Propagation, Cardinality Edge Pruning, Cardinality Node Pruning, Weighed Edge Pruning,
        // Weighed Node Pruning, Reciprocal Cardinality Node Pruning, Reciprocal Weighed Node Pruning,
        // BLAST, Canopy Clustering, and Extended Canopy Clustering
        // All methods are optional, but competive, in the sense that only one of them can be part of
        // ER workflow
        // for more information see https://www.sciencedirect.com/science/article/abs/pii/S2214579616300168
        // All of the methods above can be combined with one of the following weighting schemes =
        // Aggregate Reciprocal Comparisons Scheme, Common Blocks Scheme, Enhanced Common Blocks Scheme,
        // Jaccard Scheme, Enhanced Jaccard Scheme, and Pearson chi-squared test.
        IBlockProcessing comparisonCleaning = ComparisonCleaningMethod.getDefaultConfiguration(ComparisonCleaningMethod.CARDINALITY_EDGE_PRUNING);

        // example of comparsionCleaning with a weighted scheme
        // CardinalityEdgePruning cardinalityEdgePruning = new CardinalityEdgePruning(WEIGHTEDSCHEME);
        // comparisonCleaning = cardinalityEdgePruning

        // Preparing Entity Matching
        // Following schema-agnostic method are currently available =
        // Group Linkage -> more information see http://pike.psu.edu/publications/icde07.pdf
        // Profile Matcher -> which aggregates all attributes values in an individual entity into a textual representation
        // Both methods can be combined with the following representation model
        // character n-grams, character n-grams graphs, token n-grams, token n-grams graphs
        // for more information see https://link.springer.com/article/10.1007/s11280-015-0365-x
        // the bag models (the one without graphs in its name) can be combined with the following similarity
        // measures =
        // ARCS similarity, Cosine similarity, Jaccard similarity, Generalized Jaccard similarity, and Enhanced
        // Jaccard similarity
        IEntityMatching entityMatching = new ProfileMatcher(profile1, profile2);
        // example entity matching with a representation model and similarity measure
        // change model with one of the representation model, and measure with one of the similarity measure
        // IEntityMatching entityMatching =  new ProfileMatcher(profile1, profile2, model, measure)

        // add all blocks from block building
        blocks.addAll(blockBuilding.getBlocks(profile1, profile2));
        // cleaning the blocks if needed
        blocks = blockCleaning.refineBlocks(blocks);
        // cleaning the comparison if needed
        blocks = comparisonCleaning.refineBlocks(blocks);

        /* ignore
        // if blocks not empty -> execute entity matching
        SimilarityPairs ogPairs = null;
        if(!blocks.isEmpty()) {
            ogPairs = entityMatching.executeComparisons(blocks);
        }
         */

        // Prepare entity clustering
        // Current available entity clustering methods for Dirty ER =
        // Connected Components Clustering, Center Clustering, Merge-Center Clustering, Ricochet SR Clustering,
        // Correlation Clustering, Markov Clustering, Cut Clustering
        // for more information see http://dblab.cs.toronto.edu/~fchiang/docs/vldb09.pdf
        // Current available entity clustering methods for Clean-Clean ER =
        // Unique Mapping Clustering, Row-Column Clustering, Best Assignment Clustering
        // for more information see https://github.com/scify/JedAIToolkit
        IEntityClustering entityClustering = null;
        UniqueMappingClustering uniqueMappingClustering = new UniqueMappingClustering();
        entityClustering = uniqueMappingClustering;

        // Preparing weight for prioritization
        double totalComparisons = 0;
        if (!blocks.isEmpty()) {
            totalComparisons = getTotalComparisons(blocks);
        }

        // Calculate budget/comparisons here
        long budget;
        if (blocks.isEmpty()) {
            // No blocks, calculate budget based on entity profiles
            // budget = (profile1.size * (profile1.size-1))/2 for Dirty ER
            budget = (profile1.size() * profile2.size());

        } else {
            // Use number of comparisons from blocks as budget
            budget = (long) totalComparisons;
        }

        // Preparing Prioritization
        // Currently there are following methods available for both Clean-Clean ER & Dirty ER =
        // Local Progressive Sorted Neighborhood, Global Progressive Sorted Neighborhood,
        // Progressive Block Scheduling, Progressive Entity Scheduling, Progressive Global Top Comparisons,
        // and Progressive Local Top Comparisons
        // for more information see https://arxiv.org/pdf/1905.06385.pdf
        IPrioritization prioritization = new ProgressiveGlobalRandomComparisons((int)budget);
        // a weighting scheme can also be add to prioritization
        // example of prioritization with a user define weighting scheme
        // IPrioritization prioritization = new GlobalProgressiveSortedNeighborhood(budget, weightingScheme)
        if(prioritization != null) {
            if(!blocks.isEmpty()) {
                prioritization.developBlockBasedSchedule(blocks);
            }
        } else {
            prioritization.developEntityBasedSchedule(profile1, profile2);
        }

        // run entity matching
        SimilarityPairs similarityPairs1 = new SimilarityPairs(true,(int)totalComparisons);
        Iterator<Comparison> comparisonsIter;
        if (prioritization != null)
            comparisonsIter = prioritization;
        else{
            List<Comparison> allComparisons = new ArrayList<>();
            for (AbstractBlock block : blocks) {
                final ComparisonIterator cIterator = block.getComparisonIterator();
                while (cIterator.hasNext()) {
                    allComparisons.add(cIterator.next());
                }
            }
            Collections.shuffle(allComparisons, new Random(System.currentTimeMillis()));
            comparisonsIter = allComparisons.iterator();
        }
        EquivalenceCluster[] equivalenceClusters1 = null;

        while (comparisonsIter.hasNext()) {
            // get the comparison
            Comparison comparison = comparisonsIter.next();
            // calculate the similarity
            float similarity = entityMatching.executeComparison(comparison);
            comparison.setUtilityMeasure(similarity);
            similarityPairs1.addComparison(comparison);
            // run clustering
            equivalenceClusters1 = entityClustering.getDuplicates(similarityPairs1);

        }

        // same as above for workflow 1 and 2
        for (EquivalenceCluster cluster : equivalenceClusters1) {
            if (cluster.getEntityIdsD1().size() != 1
                    || cluster.getEntityIdsD2().size() != 1) {
                continue;
            }

            Map<String, Map<String, String>> resultTemp = new HashMap<>();
            Map<String, String> attributeTemp = null;
            final int entityId1 = cluster.getEntityIdsD1().get(0);
            final EntityProfile profile12 = profile1.get(entityId1);

            final int entityId2 = cluster.getEntityIdsD2().get(0);
            final EntityProfile profile22 = profile2.get(entityId2);

            Set<Attribute> attributeSet12 = profile12.getAttributes();
            Set<Attribute> attributeSet22 = profile22.getAttributes();
            Iterator<Attribute> iterator = attributeSet12.iterator();
            if (iterator != null) attributeTemp = new HashMap<>();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                attributeTemp.put(attribute.getName(), attribute.getValue());
            }
            if (attributeTemp != null) resultTemp.put("airport1", attributeTemp);
            attributeTemp = null;
            Iterator<Attribute> iterator1 = attributeSet22.iterator();
            if (iterator1 != null) attributeTemp = new HashMap<>();
            while (iterator1.hasNext()) {
                Attribute attribute = iterator1.next();
                attributeTemp.put(attribute.getName(), attribute.getValue());
            }
            if (attributeTemp != null) resultTemp.put("airport2", attributeTemp);
            result.put(count, resultTemp);
            count++;

        }
        return result;
    }

    /**
     * get total comparisons from a list of block
     * @param blocks list of blocks
     * @return total comparisons
     */
    public static float getTotalComparisons(List<AbstractBlock> blocks) {
        float originalComparisons = 0f;
        originalComparisons = blocks.stream()
                .map(AbstractBlock::getNoOfComparisons)
                .reduce((long) originalComparisons, (accumulator, _item) -> accumulator + _item);
        System.out.println("Original comparisons\t:\t" + originalComparisons);
        return originalComparisons;
    }
}
