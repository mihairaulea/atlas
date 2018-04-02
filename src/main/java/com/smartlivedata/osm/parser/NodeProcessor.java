package com.smartlivedata.osm.parser;

import com.smartlivedata.atlas.core.IAtlasEntity;
import com.smartlivedata.osm.data.*;
import java.util.Map;

/**
 * Created by Mihai Raulea on 16/08/2017.
 */
// instead of putting stuff into properties, should specialize in individual props?
public class NodeProcessor {

    Long nodeId, relationId;
    public OsmBounds osmBounds = new OsmBounds();
    public FastNode osmNode = new FastNode();
    public Way way = new Way();
    public Relation relation = new Relation();

    public NodeProcessor() {

    }

    public IAtlasEntity processNode(ProcessingUnit processingUnit) {
        if(processingUnit.isStart) return processStartElementPath(processingUnit.tagPath,processingUnit.properties);
        else return processEndElementPath(processingUnit.tagPath);
    }

    private IAtlasEntity processStartElementPath(String tagPath, Map<String,Object> properties) {
        if ( tagPath.equals( "[osm]" ) )
        {
            // put these in separate objects
            //osmWriter.setDatasetProperties( extractProperties( parser ) );
        }
        else if ( tagPath.equals( "[osm, bounds]" ) )
        {
            //<bounds minlat="56.0437000" minlon="12.9388000" maxlat="56.0761000" maxlon="13.0109000"/>
            osmBounds.minlat = Double.valueOf( (String)properties.get("minlat") );
            osmBounds.minlon = Double.valueOf( (String)properties.get("minlon"));
            osmBounds.maxlat = Double.valueOf( (String)properties.get("maxlat"));
            osmBounds.maxlon = Double.valueOf( (String)properties.get("maxlon"));
            // put these in separate objects
            //osmWriter.addOSMBBox( extractProperties( PROP_BBOX, parser ) );
        }

        if (tagPath.equals("[osm, node]")) {
            osmNode = new FastNode();
            osmNode.properties = properties;
        }
        if (tagPath.equals("[osm, node, tag]")) {
            osmNode.addTag((String)properties.get("k"), (String)properties.get("v"));
        }

        // WAY
        if (tagPath.equals("[osm, way]")) {
            way = new Way();
            way.properties = properties;
        }

        if (tagPath.equals("[osm, way, nd]")) {
            nodeId = Long.valueOf((String)properties.get("ref"));
            way.referencedIds.add(nodeId);
        }

        if (tagPath.equals("[osm, way, tag]")) {
            way.addTag((String)properties.get("k"), (String)properties.get("v"));
        }

        // RELATION
        if (tagPath.equals("[osm, relation]")) {
            relation = new Relation();
            relation.properties = properties;
            relationId = (Long)properties.get("id");
        }

        if (tagPath.equals("[osm, relation, tag]")) {
            relation.addTag((String)properties.get("k"), (String)properties.get("v"));
        }

        if (tagPath.equals("[osm, relation, member]")) {
            if (properties.containsKey("type")) {
                Long id = Long.valueOf((String)properties.get("ref"));
                String type = (String)properties.get("type");
                String role = (String)properties.get("role");
                relation.referencedIds.add(id);
                relation.addComponent(id, new Member(type, id, role));
            } //else throw new NoSuchElementException("Relation does not have a type!");
        }

        return null;
    }

    private IAtlasEntity processEndElementPath(String tagPath) {
        if ( tagPath.equals( "[osm, bounds]" ) ) {
            return osmBounds;
        }
        if (tagPath.equals("[osm, node]")) {
            return osmNode;
        }
        if (tagPath.equals("[osm, way]")) {
            return way;
        }
        if (tagPath.equals("[osm, way, nd]")) {
            return null;
        }
        if (tagPath.equals("[osm, relation]")) {
            return relation;
        }
        if (tagPath.equals("[osm]")) {
            return new OsmWrap();
        }
        return null;
    }

}
