package com.smartlivedata.osm.traversal;

import com.smartlivedata.osm.parser.Constants;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;

public class AtlasTraversals {

    public TraversalDescription getWayTraversalDescriptionIncludingWayStartNode(GraphDatabaseService db, Node wayStart) {
                   return db.traversalDescription()
                            .depthFirst()
                            //.evaluator(Evaluators.lastRelationshipTypeIs(Evaluation.INCLUDE_AND_PRUNE, Evaluation.EXCLUDE_AND_PRUNE, RelationshipType.withName(Constants.NEXT_WAY_NODE)))
                            .expand(
                                    PathExpanderBuilder.allTypesAndDirections()
                                            .addRelationshipFilter(relationship -> relationship.hasProperty(Constants.RELATIONSHIP_WAY_PROPERTY))
                                            .addRelationshipFilter(relationship -> {
                                                return ((Long)relationship.getProperty(Constants.RELATIONSHIP_WAY_PROPERTY)).equals( (Long)wayStart.getProperty(Constants.CURRENT_ID_STRING) );
                                            })
                                            .build()
                            );
    }

    public TraversalDescription getWayTraversalDescriptionExcludingWayStartNode(GraphDatabaseService db, Node wayStart) {
        return getWayTraversalDescriptionIncludingWayStartNode(db,wayStart).evaluator(Evaluators.excludeStartPosition());
    }
}
