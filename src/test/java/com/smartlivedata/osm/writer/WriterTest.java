package com.smartlivedata.osm.writer;

import com.smartlivedata.AtlasTest;
import com.smartlivedata.osm.data.*;
import com.smartlivedata.osm.parser.Constants;
import org.junit.Test;
import org.neo4j.graphdb.*;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class WriterTest extends AtlasTest {

    /*
    NODE
    */
    @Test
    public void writeNodeFromMemoryTest() {
        fastWriter.start(graphDatabaseService);
        fastWriter.addNode(getDummyFastNode());
        fastWriter.finish();
        assertFastNodeCountInDatabase(1,1);
    }

    // these are integration tests, move to appropriate location
    @Test
    public void writeNodeTest() throws XMLStreamException, IOException {
        testFastNodeCountFromOsmFile(osmTestUtil.nodes, 8, 8);
    }

    @Test
    public void writeNodeSingleTagTest() throws XMLStreamException, IOException {
        testFastNodeCountFromOsmFile(osmTestUtil.nodeWithSingleTagOsmPath,3,3);
        // at a second insert, the same nodes should not be inserted into the database again
        //testFastNodeCountFromOsmFile(osmTestUtil.nodeWithSingleTagOsmPath,3,3);
    }

    @Test
    public void writeMultipleNodesTest() throws XMLStreamException, IOException {
        testFastNodeCountFromOsmFile(osmTestUtil.nodeWithMultipleTagOsmPath, 3, 3);
    }

    @Test
    public void writeSameNodeIdACoupleOfTimes() {
        FastNode fastNode = getDummyFastNode();
        fastWriter.start(graphDatabaseService);
        fastWriter.addNode(fastNode);
        fastWriter.addNode(fastNode);
        FastNode fastNode1 = getDummyFastNode();
        fastNode1.setId(fastNode.getId());
        // should have updated properties !!!!
        fastWriter.addNode(fastNode1);
        fastWriter.finish();
        assertFastNodeCountInDatabase(1,1);
    }

    /*
    NODE
    */
    @Test
    public void writeWay() {
        fastWriter.start(graphDatabaseService);
        List<FastNode> fastNodes = getDummyNodesForWay(3);
        Way way = getDummyWay(fastNodes);
        fastNodes.stream().forEach(fastNode -> fastWriter.addNode(fastNode));
        fastWriter.addWay(way);
        fastWriter.finish();

        Transaction tx = graphDatabaseService.beginTx();
        assertTrue(graphDatabaseService.getAllNodes().stream().count() == 4);
        assertTrue(graphDatabaseService.findNodes(Label.label(Constants.WAY_NODE)).stream().count() == 1);
        Node wayStartNode = graphDatabaseService.findNodes(Label.label(Constants.WAY_NODE)).next();
        assertTrue(wayStartNode.hasLabel(Label.label(Constants.WAY_NODE)));
        assertTrue(wayStartNode.hasProperty(Constants.CURRENT_ID_STRING));
        Node node1 = getNode(fastNodes.get(0));
        Node node2 = getNode(fastNodes.get(1));
        Node node3 = getNode(fastNodes.get(2));

        testNodeOutgoingRelationships(wayStartNode, Constants.NEXT_WAY_NODE, way.getId(), fastNodes.get(0).getId());
        testNodeOutgoingRelationships(node1, Constants.NEXT_WAY_NODE, fastNodes.get(0).getId(), fastNodes.get(1).getId());
        testNodeOutgoingRelationships(node2, Constants.NEXT_WAY_NODE, fastNodes.get(1).getId(), fastNodes.get(2).getId());
        testNodeOutgoingRelationships(node3, Constants.BACK_TO_WAY_NODE, fastNodes.get(2).getId(), way.getId());

        tx.success();
        tx.close();
    }

    private Node getNode(FastNode fastNode) {
        return graphDatabaseService.findNode(Label.label(Constants.OSM_NODE), Constants.CURRENT_ID_STRING, fastNode.properties.get(Constants.CURRENT_ID_STRING));
    }

    private void testNodeOutgoingRelationships(Node nodeToTestForRels, String relationshipName, long idOnStart, long idOnEnd) {
        Iterator<Relationship> it = nodeToTestForRels.getRelationships(Direction.OUTGOING).iterator();
        while(it.hasNext()) {
            Relationship rel = it.next();
            if(rel.isType(RelationshipType.withName(relationshipName))) {
                assertTrue((Long)rel.getStartNode().getProperty(Constants.CURRENT_ID_STRING) == idOnStart);
                assertTrue((Long)rel.getEndNode().getProperty(Constants.CURRENT_ID_STRING) == idOnEnd);
            }
        }
    }

    @Test
    public void writeWayWithReferenceNodes() {

    }

    @Test
    public void writeWayWithNoNodes() {

    }

    @Test
    public void writeWayWithOneNode() {

    }

    // these are integration tests, too
    @Test
    public void writeMultipleWays() {

    }

    @Test
    public void writeWayWithTag() {

    }

    @Test
    public void writeMutipleWaysWithTags() {

    }
    /*
    RELATION
    */
    @Test
    public void writeRelation() throws InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        fastWriter.start(graphDatabaseService);
        List<FastNode> fastNodes = getDummyNodesForWay(3);
        List<FastNode> fastNodes2 = getDummyNodesForWay(3);
        Way way = getDummyWay(fastNodes);
        Way way2 = getDummyWay(fastNodes2);

        Relation relation = (Relation)getDummyOSMEntity(Relation.class);
        getDummyMembersFromNodes(fastNodes).stream().forEach(dummyNodeMember -> relation.addMember(dummyNodeMember));
        getDummyMembersFromNodes(fastNodes2).stream().forEach(dummyNodeMember -> relation.addMember(dummyNodeMember));

        getDummyMembersFromWay(Arrays.asList(way,way2)).stream().forEach(dummyWayMember -> relation.addMember(dummyWayMember));

        fastNodes.stream().forEach(fastNode -> fastWriter.addNode(fastNode));
        fastWriter.addWay(way);
        fastNodes2.stream().forEach(fastNode -> fastWriter.addNode(fastNode));
        fastWriter.addWay(way2);
        fastWriter.addRelation(relation);
        fastWriter.finish();

        // no idea about any order in which Members should be found
    }

}
