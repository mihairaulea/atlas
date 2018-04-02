package com.smartlivedata.traversal;

import com.smartlivedata.AtlasTest;
import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.Way;
import com.smartlivedata.osm.parser.Constants;
import com.smartlivedata.osm.traversal.AtlasTraversals;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;

public class AtlasTraversalTest extends AtlasTest {

    AtlasTraversals atlasTraversals = new AtlasTraversals();

    @Test
    public void getWayNodesIncludingWayStart() {
        List<FastNode> dummyNodesForWay1 = getDummyNodesForWay(20);
        Way way1 = getDummyWay(dummyNodesForWay1);

        fastWriter.start(graphDatabaseService);
        dummyNodesForWay1.stream().forEach(node -> fastWriter.addNode(node));
        Node wayNode = fastWriter.addWay(way1);

        Transaction tx = graphDatabaseService.beginTx();
        assertPathContainsNodesAndJustNodes(wayNode, dummyNodesForWay1);
        tx.close();
        tx.success();
    }

    @Test
    public void getWayNodesExcludingWayStart() {

    }

    // this is an integration test
    @Test
    public void getWayNodes() {
        List<FastNode> dummyNodesForWay1 = getDummyNodesForWay(20);
        List<FastNode> dummyNodesForWay2 = getDummyNodesForWay(19);

        FastNode middleNode = dummyNodesForWay1.get(10);
        dummyNodesForWay2.add(10,middleNode);

        Way way1 = getDummyWay(dummyNodesForWay1);
        Way way2 = getDummyWay(dummyNodesForWay2);

        fastWriter.start(graphDatabaseService);
        dummyNodesForWay1.stream().forEach(fastNode ->  fastWriter.addNode(fastNode));
        dummyNodesForWay2.stream().forEach(fastNode ->  fastWriter.addNode(fastNode));
        Node wayNode1 = fastWriter.addWay(way1);
        Node wayNode2 = fastWriter.addWay(way2);

        // because there are two WAY_START nodes
        assertFastNodeCountInDatabase(39,41);

        Transaction tx = graphDatabaseService.beginTx();
        //getNodesInTraversal(wayNode1).forEach(node -> System.out.println(node.getAllProperties().toString()));
        assertPathContainsNodesAndJustNodes(wayNode1, dummyNodesForWay1);
        assertPathContainsNodesAndJustNodes(wayNode2, dummyNodesForWay2);
        tx.success();
        tx.close();
    }

    // this just validates that
    private void assertPathContainsNodesAndJustNodes(Node wayNodeStart, List<FastNode> listOfFastNodeToValidate) {
        getNodesInTraversal(wayNodeStart).filter(nodeInTraversal -> !nodeInTraversal.getProperty(Constants.CURRENT_ID_STRING).equals(wayNodeStart.getProperty(Constants.CURRENT_ID_STRING))).forEach(node -> {
            assertTrue(listOfFastNodeToValidate.stream().filter(fastNode -> ((Long)node.getProperty(Constants.CURRENT_ID_STRING)).equals(fastNode.getId())).count() == 1);
        });

        // -1 because we subtract the starting way node
        assertTrue(getNodesInTraversal(wayNodeStart).count()-1 == listOfFastNodeToValidate.size());
    }

    private Stream<Node> getNodesInTraversal(Node wayNodeStart) {
        return atlasTraversals.getWayTraversalDescriptionIncludingWayStartNode(graphDatabaseService, wayNodeStart)
                .traverse(wayNodeStart).nodes().stream();
    }

}
