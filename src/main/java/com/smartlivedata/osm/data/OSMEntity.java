package com.smartlivedata.osm.data;

import com.smartlivedata.atlas.core.IAtlasEntity;
import com.smartlivedata.osm.parser.Constants;

import java.util.*;

/**
 * Created by Mihai Raulea on 18/08/2017.
 */
public class OSMEntity implements IAtlasEntity {

    public boolean usedInInterestingOSMStructure = false;
    public Map<String,Object> properties = new LinkedHashMap<>();
    private Set<String> tags = new HashSet<>();
    public List<Long> referencedIds = new LinkedList<>();

    public Set<String> getTags() {
        return tags;
    }

    public void addTag(String key, String value) {
        String goesInSet = key+"**"+value;
        tags.add(goesInSet);
    }

    public void setId(long id) {
        properties.put(Constants.CURRENT_ID_STRING, id);
    }

    public long getId() {
        Object id = properties.get(Constants.CURRENT_ID_STRING);
        if(properties.containsKey(Constants.CURRENT_ID_STRING)) {
            if(id instanceof String) return Long.parseLong((String)properties.get(Constants.CURRENT_ID_STRING));
            if(id instanceof Long) return (Long)id;
        }
        return -1L;
    }

}
