package com.smartlivedata;

import com.smartlivedata.osm.data.*;
import com.smartlivedata.osm.parser.OSMParser;
import com.smartlivedata.osm.utils.OSMTestUtil;
import com.smartlivedata.osm.writer.FastWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertTrue;

public class AtlasTest {

    public FastWriter fastWriter = new FastWriter();
    public OSMParser osmParser = new OSMParser();
    public OSMTestUtil osmTestUtil = new OSMTestUtil();
    public String dbTest = Paths.get(".", "src/test/resources", "testRun").normalize().toAbsolutePath().toString();
    public GraphDatabaseService graphDatabaseService;

    @Before
    public void setup() {
        tearDown();
        graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbTest));
    }

    // move these out of here
    @Test
    public void doTwoWaysReferTheSameNode() throws Exception {
        List<Long> allReferencedIds = osmParser.getEntityStream(osmTestUtil.fullOSMSmall).filter(t -> t instanceof Way).map(t -> (Way)t)
                .map(way -> (way.referencedIds)).map(referencedIds -> referencedIds.iterator().next()).collect(Collectors.toList());

        Set<Long> duplicates = findDuplicates(allReferencedIds);
        assertTrue(duplicates.size()!=0);
    }

    public Set<Long> findDuplicates(List<Long> listContainingDuplicates)
    {
        final Set<Long> setToReturn = new HashSet();
        final Set<Long> set1 = new HashSet();

        for (Long yourInt : listContainingDuplicates)
        {
            if (!set1.add(yourInt))
            {
                setToReturn.add(yourInt);
            }
        }
        return setToReturn;
    }

    public void testFastNodeCountFromOsmFile(String path, int noOfFastNodesExpected, int noOfNodesInDatabaseExpected) throws XMLStreamException, IOException {
        fastWriter.start(graphDatabaseService);
        osmParser.getEntityStream(path).filter(entity -> entity instanceof FastNode).map(fastNode -> (FastNode)(fastNode)).forEach(fastNode -> fastWriter.addNode(fastNode));
        assertFastNodeCountInDatabase(noOfFastNodesExpected,noOfNodesInDatabaseExpected);
        fastWriter.finish();
    }

    public void assertFastNodeCountInDatabase(int noOfFastNodesExpected, int noOfNodesInDatabaseExpected) {
        Transaction tx = graphDatabaseService.beginTx();
        assertTrue(graphDatabaseService.getAllNodes().stream().count() == noOfNodesInDatabaseExpected);
        assertTrue(fastWriter.numberOfNodesAdded == noOfFastNodesExpected);
        tx.success();
        tx.close();
    }

    // make it so data makes sense
    public FastNode getDummyFastNode() {
        FastNode result = (FastNode)getDummyOSMEntity(FastNode.class);
        return result;
    }

    // make it so data makes sense; points come in a sequence
    public OSMEntity getDummyOSMEntity(Class clazz) {
        Constructor[] allConstructors = clazz.getDeclaredConstructors();
        // should rather find the first constructor
        assert(allConstructors[0].getParameterCount() == 0);
        OSMEntity osmEntity = null;
        try {
            osmEntity = (OSMEntity) allConstructors[0].newInstance();
        }
        catch (Exception e) {

        }
        if(osmEntity!=null) {
            osmEntity.setId(getLongRandomId());
            osmEntity.properties.put("lat", Math.random());
            osmEntity.properties.put("lon", Math.random());
            osmEntity.addTag("myTag", "myValue");
        }
        return osmEntity;
    }

    // move these to an utility function?
    // make it so data makes sense
    public Way getDummyWay(List<FastNode> arrayOfNodesToUse) {
        Way way = (Way)getDummyOSMEntity(Way.class);
        arrayOfNodesToUse.stream().forEach(fastNode -> way.referencedIds.add((fastNode.getId())));
        return way;
    }

    public List<Member> getDummyMembersFromNodes(List<FastNode> fastNodes) {
        return fastNodes.stream().map(fastNode -> new Member("Node", (fastNode.getId()), "dummyRole")).collect(Collectors.toList());
    }

    public List<Member> getDummyMembersFromWay(List<Way> wayList) {
        return wayList.stream().map(way -> new Member("Way", (way.getId()), "dummyRole")).collect(Collectors.toList());
    }

    public Relation getDummyRelation() {
        return null;
    }

    public List<FastNode> getDummyNodesForWay(int noOfNodes) {
        List<FastNode> fastNodeList = new ArrayList<>();
        IntStream.range(0, noOfNodes).forEach(index -> fastNodeList.add(getDummyFastNode()));
        return fastNodeList;
    }

    public Long getLongRandomId() {
        return (Double.doubleToLongBits(Math.abs(Math.random()*1000)));
    }

    public String getStringRandomId() {
        return String.valueOf(getLongRandomId());
    }

    @After
    public void tearDown() {
        if(graphDatabaseService!=null) graphDatabaseService.shutdown();
        osmTestUtil.deleteData(dbTest);
        graphDatabaseService = null;
    }

}
