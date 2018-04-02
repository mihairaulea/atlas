package com.smartlivedata.osm.utils;

import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.OSMEntity;
import com.smartlivedata.osm.parser.OSMParser;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by Mihai Raulea on 23/10/2017.
 */
public class OSMTestUtil {

    public String nodes = Paths.get(".", "src/test/resources", "nodes/nodes.osm").normalize().toAbsolutePath().toString();
    public String nodeWithSingleTagOsmPath = Paths.get(".", "src/test/resources", "nodes/nodeWithSingleTag.osm").normalize().toAbsolutePath().toString();
    public String nodeWithMultipleTagOsmPath = Paths.get(".", "src/test/resources", "nodes/nodeWithMultipleTags.osm").normalize().toAbsolutePath().toString();

    public String simpleWay = Paths.get(".", "src/test/resources", "ways/way.osm").normalize().toAbsolutePath().toString();
    public String wayWithSingleTag = Paths.get(".", "src/test/resources", "ways/waywithtags.osm").normalize().toAbsolutePath().toString();
    public String waysWithMultipleTag = Paths.get(".", "src/test/resources", "ways/multipleWaysWithTags.osm").normalize().toAbsolutePath().toString();

    public String fullOSMSmall = Paths.get(".","src/test/resources","fullOSM/7cities3woods.osm").normalize().toAbsolutePath().toString();
    public String fullOSMBig = Paths.get(".","src/test/resources","fullOSM/belgium-latest.osm").normalize().toAbsolutePath().toString();

    public String relationWithMultipleWaysAndMultipleTags = Paths.get(".", "src/test/resources", "relations/relationToMultipleWaysWithMultipleTags.osm").normalize().toAbsolutePath().toString();


    public static boolean testNumberOfNodes(OSMParser osmParser, String path, int expectedNumberOfNodes) throws XMLStreamException, FileNotFoundException {
        return getNodeStream(osmParser, path).count() == expectedNumberOfNodes;
    }

    public static Stream<FastNode> getNodeStream(OSMParser osmParser, String path) throws XMLStreamException, FileNotFoundException {
        return osmParser.getEntityStream(path).filter(entity -> entity instanceof FastNode).map(entity -> (FastNode)entity).collect(Collectors.toList()).stream();
    }
    /*
 Helpers
  */
    /*
    public NodeProcessor getFinishedNodeProcessorThread(OSMParser OSMParser, String path) throws Exception {
        OSMParser.importFile(path);
        return OSMParser.nodeProcessor;
    }
    */

    public void testCorrectNodeImport(String[] expectedNodes, Map<String,OSMEntity> nodes) {
        assertTrue(expectedNodes.length == nodes.size());
        Arrays.stream(expectedNodes).forEach(t -> assertTrue(nodes.containsKey(t)));
    }
    /*
        public NodeProcessor testBlueprint(OSMParser inMemoryOSMImporter, String path, String[] expectedNodeIds, String[] expectedWayIds, String[] expectedRelationIds) throws Exception {
            NodeProcessor nodeProcessor = getFinishedNodeProcessorThread(inMemoryOSMImporter, path);
            assertTrue(nodeProcessor.osmNodeList.size() == expectedNodeIds.length);
            assertTrue(nodeProcessor.wayList.size() == expectedWayIds.length);
            assertTrue(nodeProcessor.relationList.size() == expectedRelationIds.length);

            testCorrectNodeImport(expectedNodeIds, nodeProcessor.osmNodeList);
            return nodeProcessor;
        }
    */
    public void deleteData(String path)  {
        try {
            Path rootPath = Paths.get(path);
            Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    //.peek(System.out::println)
                    .forEach(File::delete);
            System.out.println("Successfully removed previous database...");
        } catch(NoSuchFileException e) {
            System.out.println("No database present, starting fresh...");
        }
        catch(IOException e) {
            System.out.println("IOException...");
        }
    }

}
