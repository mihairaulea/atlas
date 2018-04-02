package com.smartlivedata.osm.transformer;

import com.smartlivedata.osm.parser.Constants;
import com.smartlivedata.osm.traversal.AtlasTraversals;
import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AtlasTransformer {

    AtlasTraversals atlasTraversals = new AtlasTraversals();

    public ContractSummary applyContraction(GraphDatabaseService db, ContractCriteria contractCriteria) {
        //assume way contraction
        db.findNodes(Label.label(Constants.WAY_NODE)).stream().forEach(wayNodeStart -> {
            List<Node> nodesToMerge = new ArrayList<>();
            ResourceIterator<Node> nodeIterator =  atlasTraversals.getWayTraversalDescriptionExcludingWayStartNode(db, wayNodeStart).traverse(wayNodeStart).nodes().iterator();
            while(nodeIterator.hasNext()) {
                Node nodeToReview = nodeIterator.next();
                System.out.println(nodeToReview.getLabels().toString()+":"+nodeToReview.getAllProperties().toString());
                if(isNodeUnmergeable(nodeToReview)) {
                    applyContraction(wayNodeStart, nodesToMerge);
                    nodesToMerge = new ArrayList<>();
                }
                else {
                    nodesToMerge.add(nodeToReview);
                }
            }
        });
        return new ContractSummary();
    }

    private ContractSummary applyContraction(Node wayStart, List<Node> nodesToContract) {
        // just for debug!!
        if(nodesToContract.size() == 0) {
            return new ContractSummary();
        }

        Node startNode = nodesToContract.get(0);
        Node endNode = nodesToContract.get(nodesToContract.size()-1);

        Long uniqueWayIdentifier = (Long)wayStart.getSingleRelationship(RelationshipType.withName(Constants.NEXT_WAY_NODE),Direction.OUTGOING).getProperty(Constants.RELATIONSHIP_WAY_PROPERTY);

        // should also gather length data and other statistics, put it on the new relationship
        for(int i=1;i<nodesToContract.size()-1;i++) {
            Node nodeToDelete = nodesToContract.get(i);
            nodeToDelete.getRelationships().forEach(relationship -> relationship.delete());
            nodeToDelete.delete();
        }

        startNode.createRelationshipTo(endNode, RelationshipType.withName(Constants.NEXT_WAY_NODE)).setProperty(Constants.RELATIONSHIP_WAY_PROPERTY, uniqueWayIdentifier);

        return new ContractSummary();
    }

    private boolean isNodeUnmergeable(Node node) {
        boolean hasUnmergeableRelationship = false;
        Iterator<Relationship> relationshipIterator = node.getRelationships().iterator();
        while(relationshipIterator.hasNext()) {
            Relationship rel = relationshipIterator.next();
            if(!rel.getType().equals(RelationshipType.withName(Constants.NEXT_WAY_NODE))) {
                hasUnmergeableRelationship = true;
                break;
            }
        }
        return hasUnmergeableRelationship;
    }

    public LinkSummary applyLinking(GraphDatabaseService db, LinkCriteria linkCriteria) {
        return new LinkSummary();
    }

}
