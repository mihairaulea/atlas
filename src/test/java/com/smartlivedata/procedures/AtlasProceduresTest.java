package com.smartlivedata.procedures;

import apoc.util.TestUtil;
import com.smartlivedata.osm.utils.OSMTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertTrue;


public class AtlasProceduresTest {

    GraphDatabaseService db;
    OSMTestUtil osmTestUtil = new OSMTestUtil();

    @Before
    public void prepare() throws KernelException {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        TestUtil.registerProcedure(db, AtlasProcedures.class);
    }

    @Test
    public void testDummyProcedureDeployment() {
        Result result = db.execute( "CALL com.smartlivedata.procedures.importOSM('"+osmTestUtil.fullOSMSmall+"')" );
        assertTrue(result.hasNext() == true);
        assertTrue(result.next().get("importedSuccessfully").equals(true));
    }
}
