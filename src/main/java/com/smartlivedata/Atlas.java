package com.smartlivedata;

import com.smartlivedata.atlas.core.parse.IAtlasParser;
import com.smartlivedata.index.AtlasIndex;
import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.OSMEntity;
import com.smartlivedata.osm.parser.OSMParser;
import com.smartlivedata.osm.transformer.AtlasTransformer;
import com.smartlivedata.osm.transformer.ContractCriteria;
import com.smartlivedata.osm.transformer.LinkCriteria;
import com.smartlivedata.osm.writer.FastWriter;
import com.smartlivedata.osm.writer.RemoveCriteria;
import com.smartlivedata.osm.writer.RemoveSummary;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;

/**
 * Created by Mihai Raulea on 01/11/2017.
 */

/*
TODO
** core classes(FastNode, Way, Relation) -- reconsider
** put all system constants in a class
** solve the Long, String, Object dillema on the id
** move utils to generate data to a separate package
** maybe put a dynamic name on the rel, so it becomes easy to follow?
** write a couple of examples
*/
public class Atlas {

    String path; //= "/Users/user/Desktop/atlas/target/mainrun2";
    String dataset;// = Paths.get(".","src/test/resources","ways/multipleWaysWithTags.osm").normalize().toAbsolutePath().toString();
    GraphDatabaseService db;

    IAtlasParser parser = new OSMParser();
    FastWriter writer = new FastWriter();
    AtlasIndex index = new AtlasIndex();
    AtlasTransformer atlasTransformer = new AtlasTransformer();

    public Atlas(String dbPath, String dataset) throws Exception {
        this.path = dbPath;
        db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(path)).newGraphDatabase();
        this.dataset = dataset;
    }

}
