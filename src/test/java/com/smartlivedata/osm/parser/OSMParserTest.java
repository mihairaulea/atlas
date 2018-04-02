package com.smartlivedata.osm.parser;

import com.smartlivedata.osm.data.Way;
import com.smartlivedata.osm.utils.OSMTestUtil;
import org.junit.Test;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
/**
 * Created by Mihai Raulea on 10/11/2017.
 */
public class OSMParserTest {

    OSMTestUtil osmTestUtil = new OSMTestUtil();
    OSMParser osmParser = new OSMParser();

    @Test
    public void countNodesTest() throws XMLStreamException, FileNotFoundException {
        assertTrue(OSMTestUtil.testNumberOfNodes(osmParser, osmTestUtil.nodes, 8));
        assertTrue(OSMTestUtil.testNumberOfNodes(osmParser, osmTestUtil.nodeWithSingleTagOsmPath, 3));
        assertTrue(OSMTestUtil.testNumberOfNodes(osmParser, osmTestUtil.nodeWithMultipleTagOsmPath, 3));
    }

    @Test
    public void countTagsTest() throws XMLStreamException, FileNotFoundException {
        assertTrue( OSMTestUtil.getNodeStream(osmParser, osmTestUtil.nodes).allMatch(t -> t.getTags().size() == 0) );
        assertTrue( OSMTestUtil.getNodeStream(osmParser, osmTestUtil.nodeWithSingleTagOsmPath).allMatch(t -> t.getTags().size() == 1) );
        assertTrue( OSMTestUtil.getNodeStream(osmParser, osmTestUtil.nodeWithMultipleTagOsmPath).allMatch(t -> t.getTags().size() == 4) );
    }

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
}
