package com.smartlivedata.osm.writer;

import com.smartlivedata.atlas.core.write.IAtlasWriter;
import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.OSMEntity;
import com.smartlivedata.osm.data.Relation;
import com.smartlivedata.osm.data.Way;
import com.smartlivedata.osm.parser.Constants;
import org.neo4j.graphdb.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Mihai Raulea on 30/10/2017.
 */
public class FastWriter implements IAtlasWriter {

    GraphDatabaseService db;

    public int numberOfRelationsAdded = 0;
    public int numberOfWaysAdded = 0;
    public int numberOfNodesAdded = 0;

    public void start(GraphDatabaseService graphDatabaseService) {
       db = graphDatabaseService;
       numberOfRelationsAdded = 0;
       numberOfWaysAdded = 0;
       numberOfNodesAdded = 0;
    }

    public Node addOSMEntity(OSMEntity osmEntity) {
        if(osmEntity instanceof FastNode) return addNode((FastNode)osmEntity);
        if(osmEntity instanceof Way) return addWay((Way)osmEntity);
        if(osmEntity instanceof Relation) return addRelation((Relation) osmEntity);
        return null;
    }
    // a transaction has to be open for this!!
    private void setNodeProperties(Node databaseNode, OSMEntity inMemoryNode) {
        inMemoryNode.properties.keySet().stream().forEach(
                key -> databaseNode.setProperty(key, inMemoryNode.properties.get(key) )
        );
    }

    private Node getOrCreateNode(FastNode inMemoryNode) {
        Node dbNode = db.findNode(Label.label(Constants.OSM_NODE), Constants.CURRENT_ID_STRING, (Long)(inMemoryNode.getId()));
        Node newNode = null;
        if(dbNode==null) {
            newNode = db.createNode(Label.label(Constants.OSM_NODE));
            newNode.setProperty(Constants.CURRENT_ID_STRING, inMemoryNode.getId());
            setNodeProperties(newNode, inMemoryNode);
            // what about the tags?
            numberOfNodesAdded++;
        }
        else {
            setNodeProperties(dbNode, inMemoryNode);
        }
        return dbNode != null ? dbNode:newNode;
    }

    private Node getOrCreateNode(Long nodeId, String[] additionalLabels) {
        Node dbNode = db.findNode(Label.label(Constants.OSM_NODE), Constants.CURRENT_ID_STRING, nodeId);
        Node newNode = null;
        if(dbNode==null) {
            newNode = db.createNode(Label.label(Constants.OSM_NODE));
            newNode.setProperty(Constants.CURRENT_ID_STRING, nodeId);
            // what about the tags?
            dbNode = newNode;

            if(additionalLabels!=null) {
                for(String label : additionalLabels) {
                    // any better way of doing this?
                    if(label!=null) dbNode.addLabel(Label.label(label));
                }
            }
        }
        return dbNode;
    }

    public Node addNode(FastNode inMemoryNode) {
        assert (db != null) : "Please call start() method before addNode() method.";
        assert (inMemoryNode.getId() != -1L) : "FastNode id must be set.";
        assert (inMemoryNode != null) : "FastNode is null";
        Transaction tx = db.beginTx();
        Node node = getOrCreateNode(inMemoryNode);
        //setNodeProperties(node, inMemoryNode);
        tx.success();
        tx.close();
        return node;
    }

    private Map<String,Object> getDummyTaggedOSMDataProperties(String id) {
        Map<String,Object> dummyProperties = new HashMap<>();
        dummyProperties.put(Constants.CURRENT_ID_STRING, id);
        return dummyProperties;
    }

    // also put generic relationship, so that roads can be followed even though they are not part of the same way?
    // what if there is a node which is shared for two ways?!?
    // it is not good that i have put the node ids in a set; the first node can be the same as the last node, to close the loop and create a special type of geometry
    // the original code also adds pointers from one way to the other; i do not find this useful
    public Node addWay(Way inMemoryWay) {
        assert (inMemoryWay.getId() != -1L);
        assert (inMemoryWay != null);
        Transaction tx = db.beginTx();

        // maybe the node existed beforehand? yes
        // this could have already been created
        Node wayNode = db.createNode(Label.label(Constants.OSM_NODE), Label.label(Constants.WAY_NODE));
        setNodeProperties(wayNode, inMemoryWay);

        Node lastNode =
                inMemoryWay.referencedIds.stream()
                .map(id -> getOrCreateNode((id), null))
                        .reduce(
                        wayNode, (acc, node) ->
                                addNodeToStructure(inMemoryWay.getId(), wayNode,acc,node));

        addLastStructureNode(inMemoryWay.getId(), wayNode, lastNode);

        tx.success();
        tx.close();
        numberOfWaysAdded++;
        return wayNode;
    }

    private Node addNodeToStructure(Long wayId, Node structureStartNode, Node previousNode, Node nodeToAttach) {
        //if(wayNode.getProperty(Constants.CURRENT_ID_STRING) == lastNode.getProperty(Constants.CURRENT_ID_STRING))
        long wayNodeId = (Long)(structureStartNode.getProperty(Constants.CURRENT_ID_STRING));
        long previousNodeId = (Long)(previousNode.getProperty(Constants.CURRENT_ID_STRING));

        if(wayNodeId == previousNodeId)
            return addFirstStructureNode(wayId, structureStartNode,nodeToAttach);
        else
            return addIntermediateStructureNode(wayId, previousNode, nodeToAttach);
    }

    private Node addFirstStructureNode(Long wayId, Node wayNode, Node currentNode) {
        wayNode.createRelationshipTo(currentNode, RelationshipType.withName(Constants.NEXT_WAY_NODE)).setProperty(Constants.RELATIONSHIP_WAY_PROPERTY, wayId);
        return currentNode;
    }

    private Node addIntermediateStructureNode(Long wayId, Node previousNode, Node currentNode) {
        previousNode.createRelationshipTo(currentNode, RelationshipType.withName(Constants.NEXT_WAY_NODE)).setProperty(Constants.RELATIONSHIP_WAY_PROPERTY, wayId);
        return currentNode;
    }

    private Node addLastStructureNode(Long wayId, Node wayNode, Node lastNode) {
        lastNode.createRelationshipTo(wayNode, RelationshipType.withName(Constants.BACK_TO_WAY_NODE)).setProperty(Constants.RELATIONSHIP_WAY_PROPERTY, wayId);
        return wayNode;
    }

    // adding way-type relationship
    public Node addRelation(Relation inMemoryRelation) {
        long wayNodeId = inMemoryRelation.getId();
        assert (inMemoryRelation.getId() != -1L);
        assert (inMemoryRelation != null);
        Transaction tx = db.beginTx();
        // this could have already been created
        Node relationNode = db.createNode(Label.label(Constants.OSM_NODE),Label.label(Constants.RELATION_NODE));
        setNodeProperties(relationNode, inMemoryRelation);

        Node lastNode = inMemoryRelation.components.entrySet().stream()
                .map(entry -> getOrCreateNode(Long.valueOf(entry.getValue().id), new String[]{ osmObjectTypeToLabel(entry.getValue().objectType) }) )
                .reduce(
                        relationNode, (acc, node) ->
                                addNodeToStructure(inMemoryRelation.getId(),relationNode,acc,node));

        addLastStructureNode(inMemoryRelation.getId(), relationNode, lastNode);


        tx.success();
        tx.close();
        numberOfRelationsAdded++;
        return relationNode;
    }

    // - disconnected
    // - do not have relevant tag
    // -
    public RemoveSummary removeNodes(RemoveCriteria removeCriteria) {
        return new RemoveSummary();
    }

    // updates returned node with info summary
    public boolean updateWithRemoveSummary(RemoveSummary removeSummary) {
        return true;
    }

    // why is this needed?
    private String osmObjectTypeToLabel(String objectType) {
        switch (objectType) {
            case "Way": return Constants.WAY_NODE;
            case "Relation": return Constants.RELATION_NODE;
        }
        return null;
    }

    private Node addNodeToRelation() {

        return null;
    }

    public void countAllNodes() {
        Transaction tx = db.beginTx();
        tx.success();
        tx.close();
    }

    public void finish() {
        //batchInserter.shutdown();
    }

}
