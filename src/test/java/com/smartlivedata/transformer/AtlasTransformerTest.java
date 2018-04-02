package com.smartlivedata.transformer;

import com.smartlivedata.AtlasTest;
import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.Way;
import com.smartlivedata.osm.transformer.AtlasTransformer;
import com.smartlivedata.osm.transformer.ContractCriteria;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.util.List;

public class AtlasTransformerTest extends AtlasTest {

    AtlasTransformer atlasTransformer = new AtlasTransformer();

    @Test
    public void simpleRoadContraction() {
        List<FastNode> dummyNodesForWay1 = getDummyNodesForWay(20);
        Way way1 = getDummyWay(dummyNodesForWay1);

        fastWriter.start(graphDatabaseService);
        dummyNodesForWay1.stream().forEach(fastNode ->  fastWriter.addNode(fastNode));
        fastWriter.addWay(way1);


        assertFastNodeCountInDatabase(19,20);

        Transaction tx = graphDatabaseService.beginTx();
        ContractCriteria contractCriteria = new ContractCriteria();
        atlasTransformer.applyContraction(this.graphDatabaseService, contractCriteria);
        tx.success();
        tx.close();

        assertFastNodeCountInDatabase(19,2);
    }

    @Test
    public void contractRoadTest() {
        List<FastNode> dummyNodesForWay1 = getDummyNodesForWay(20);
        List<FastNode> dummyNodesForWay2 = getDummyNodesForWay(19);

        FastNode middleNode = dummyNodesForWay1.get(10);
        dummyNodesForWay2.add(10,middleNode);

        Way way1 = getDummyWay(dummyNodesForWay1);
        Way way2 = getDummyWay(dummyNodesForWay2);

        fastWriter.start(graphDatabaseService);
        dummyNodesForWay1.stream().forEach(fastNode ->  fastWriter.addNode(fastNode));
        dummyNodesForWay2.stream().forEach(fastNode ->  fastWriter.addNode(fastNode));
        fastWriter.addWay(way1);
        fastWriter.addWay(way2);

        // because there are two WAY_START nodes
        assertFastNodeCountInDatabase(39,41);

        System.out.println(dummyNodesForWay1.toString());
        System.out.println(dummyNodesForWay2.toString());

        Transaction tx = graphDatabaseService.beginTx();
        ContractCriteria contractCriteria = new ContractCriteria();
        atlasTransformer.applyContraction(this.graphDatabaseService, contractCriteria);
        tx.success();
        tx.close();
    }

}
