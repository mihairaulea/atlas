package com.smartlivedata.osm.parser;

import com.smartlivedata.atlas.core.IAtlasEntity;
import com.smartlivedata.atlas.core.parse.IAtlasParser;
import com.smartlivedata.osm.data.OsmEnd;
import com.smartlivedata.osm.data.ProcessingUnit;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by Mihai Raulea on 05/08/2017.
 */
public class OSMParser implements IAtlasParser {

    javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
    CountedFileReader reader = null;
    XMLStreamReader parser = null;
    public NodeProcessor nodeProcessor = new NodeProcessor();

    ArrayList<String> currentXMLTags = new ArrayList<String>();
    String tagPath = null;
    int depth = 0;


    @Override
    public <T> Stream<T> getEntityStream(String data) throws NoSuchElementException, XMLStreamException, FileNotFoundException {
        reader = new CountedFileReader( data, StandardCharsets.UTF_8 );
        parser = factory.createXMLStreamReader( reader );
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false);
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        try{
            hasNext = parser.hasNext();
        }
        catch (Exception e) {
            hasNext = false;
        }
        return hasNext;
    }

    @Override
    public IAtlasEntity next() {
        return nextEntity();
    }

    private IAtlasEntity nextEntity() {
        IAtlasEntity result = null;
        try
        {
            while ( result == null )
            {
                int event = parser.next();
                if ( event == javax.xml.stream.XMLStreamConstants.END_DOCUMENT )
                {
                    return new OsmEnd();
                }
                switch ( event )
                {
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT: {
                        currentXMLTags.add(depth, parser.getLocalName());
                        depth++;
                        Map<String,Object> properties = extractProperties(parser);

                        tagPath = currentXMLTags.toString();
                        result = submitWork(properties, tagPath,true);

                        break;
                    }

                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT: {
                        tagPath = currentXMLTags.toString();
                        //System.out.println(tagPath);
                        try {
                            result = submitWork(null, tagPath, false);
                        }
                        catch (Exception e) {
                            // we might encounter tagPath for which we have no case
                            System.out.println(e.getMessage());
                            System.out.println("FAILED TO SUBMIT WORK");
                        }
                            //System.out.println(result + " result");
                        depth--;
                        currentXMLTags.remove(depth);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        catch (XMLStreamException xmlStreamException) {
            System.out.println(xmlStreamException.getMessage());
        }
        return result;
    }

    private Map<String, Object> extractProperties(XMLStreamReader parser ) {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        for ( int i = 0; i < parser.getAttributeCount(); i++ )
        {
            String prop = parser.getAttributeLocalName( i );
            Object value = parser.getAttributeValue( i );
            properties.put( prop, value );
        }
        return properties;
    }


    private IAtlasEntity submitWork(Map<String,Object> properties, String tagPath, boolean isStart) {
        ProcessingUnit processingUnit = new ProcessingUnit();
        processingUnit.isStart = isStart;
        if(properties!=null) {
            if (properties.containsKey(Constants.INITIAL_ID_STRING_NAME)) {
                Object value = properties.remove(Constants.INITIAL_ID_STRING_NAME);
                properties.put(Constants.CURRENT_ID_STRING, Long.valueOf((String)value));
            }
            // this would go in a filter
            /*
            if (properties.containsKey("uid")) properties.remove("uid");
            if (properties.containsKey("version")) properties.remove("version");
            if (properties.containsKey("changeset")) properties.remove("changeset");
            if (properties.containsKey("user")) properties.remove("user");
            if (properties.containsKey("timestamp")) properties.remove("timestamp");
            */
        }
        processingUnit.properties = properties;
        processingUnit.tagPath = tagPath;
        return nodeProcessor.processNode(processingUnit);
    }

}
