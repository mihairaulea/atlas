package com.smartlivedata.osm.transformer;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Iterator;

/**
 * Created by Mihai Raulea on 04/11/2017.
 */
public class RoadContracter {

    GraphDatabaseService db;

    public RoadContracter(GraphDatabaseService gdb) {
        this.db = gdb;
    }

    public boolean contractRoad() {
        Transaction tx = db.beginTx();
        Iterator<Node> wayStartIterator = db.findNodes(Label.label("WAY_START"));
        while(wayStartIterator.hasNext()) {
            Node wayStartNode = wayStartIterator.next();

        }
        tx.success();
        tx.close();
        return true;
    }



}
