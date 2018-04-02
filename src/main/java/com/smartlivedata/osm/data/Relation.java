package com.smartlivedata.osm.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Mihai Raulea on 09/08/2017.
 */
public class Relation extends OSMEntity {

    public Map<Long,Member> components = new LinkedHashMap();

    public void addComponent(Long id, Member member) {
        this.referencedIds.add(id);
        components.put(id, member);
    }

    public void addMember(Member member) {
        this.referencedIds.add(member.id);
        components.put(member.id, member);
    }

}
