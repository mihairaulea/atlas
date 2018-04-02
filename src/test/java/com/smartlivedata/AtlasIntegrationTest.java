package com.smartlivedata;

import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.OSMEntity;
import com.smartlivedata.osm.data.Relation;
import com.smartlivedata.osm.data.Way;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

public class AtlasIntegrationTest extends AtlasTest {

    @Test
    public void integrationTest1() throws XMLStreamException, FileNotFoundException {
        //System.out.println("Number of relations in area:"+osmParser.getEntityStream(osmTestUtil.fullOSMSmall).filter(osmEntity -> osmEntity instanceof Relation).count());
        //System.out.println("Number of ways in area:"+osmParser.getEntityStream(osmTestUtil.fullOSMSmall).filter(osmEntity -> osmEntity instanceof Way).count());
        //System.out.println("Number of nodes in area:"+osmParser.getEntityStream(osmTestUtil.fullOSMSmall).filter(osmEntity -> osmEntity instanceof FastNode).count());

        Set<Object> tagsInRelations = getTagsInOSMEntity(Relation.class);
        Set<Object> tagsInWays = getTagsInOSMEntity(Way.class);
        Set<Object> tagsInNodes = getTagsInOSMEntity(FastNode.class);
    }

    // just import these tests as procedures
    // this inserts duplicates
    @Test
    public void integrationTest2() throws XMLStreamException, FileNotFoundException {
        fastWriter.start(graphDatabaseService);
        System.out.println(osmParser.getEntityStream(osmTestUtil.fullOSMSmall)
                // maybe i should return an OSMEntity directly
                .filter(osmEntity -> osmEntity instanceof FastNode)
                .map(osmEntity -> (FastNode)osmEntity)
                .map(osmEntity -> fastWriter.addNode(osmEntity)).count());
        fastWriter.finish();

        System.out.println(graphDatabaseService.execute("MATCH (n) return count(n);").next());
    }

    private Set<Object> getTagsInOSMEntity(Class<? extends OSMEntity> osmEntityClass) throws XMLStreamException, FileNotFoundException {
        return osmParser.getEntityStream(osmTestUtil.fullOSMSmall)
                .filter(osmEntity -> osmEntityClass.isInstance(osmEntity))
                .map(relation -> osmEntityClass.cast(relation))
                .map(relation -> relation.getTags())
                .collect(Collectors.toSet());
    }

}
