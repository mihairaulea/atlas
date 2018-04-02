package com.smartlivedata.osm.parser;

import com.smartlivedata.osm.data.FastNode;
import com.smartlivedata.osm.data.OSMEntity;
import com.smartlivedata.osm.data.Relation;
import com.smartlivedata.osm.data.Way;

import java.util.*;

/**
 * Created by Mihai Raulea on 26/10/2017.
 */
public class TaggedOSMDataRepo {

    public Map<Long,OSMEntity> osmNodeList = new HashMap<>();
    public Map<Long,OSMEntity> wayList = new LinkedHashMap<>();
    public Map<Long,OSMEntity> relationList = new LinkedHashMap<>();

    public Set<Long> idsInterestingByDefault = new HashSet<>();

    public Set<String> interestingTags = new HashSet<>();

    public static int THRESHOLD = 100;
    private int thresholdCounter = 0;

    public void addOSMEntity(OSMEntity OSMEntity) {
        Map<Long, OSMEntity> mapToInsertIn = null;
        if(OSMEntity instanceof FastNode) mapToInsertIn = osmNodeList;
        if(OSMEntity instanceof Way)      mapToInsertIn = wayList;
        if(OSMEntity instanceof Relation) mapToInsertIn = relationList;
        if(filterEntity(OSMEntity)) {
            if(!mapToInsertIn.containsKey(OSMEntity.properties.get(Constants.CURRENT_ID_STRING))) {
                mapToInsertIn.put((Long) OSMEntity.properties.get(Constants.CURRENT_ID_STRING), OSMEntity);
                // could also compute this on the fly ;)
                if(!(OSMEntity instanceof FastNode)) idsInterestingByDefault.addAll(OSMEntity.referencedIds);
            }
        }
    }

    private boolean filterEntity(OSMEntity toFilter) {
        if(interestingTags == null || interestingTags.size() == 0) return true;
        if(idsInterestingByDefault.contains(toFilter.properties.get(Constants.CURRENT_ID_STRING))) return true;
        if(toFilter.getTags().stream().filter(entityTag -> isInterestingTag(entityTag)).count()!=0) {
            return true;
        }
        return false;
    }

    private boolean isInterestingTag(String tag) {
        return interestingTags.contains(tag);
    }

/*
    private boolean entitiesMissing() {
        long numberOfReferencesFromRelationFoundInMemory =
                getStreamOfReferencedIds(relationList)
                        .filter(idReferencedByARelation -> wayList.containsKey(idReferencedByARelation) || osmNodeList.containsKey(idReferencedByARelation) )
                .count();

        long numberOfReferencesFromWayFoundInMemory = getStreamOfReferencedIds(wayList)
                .filter(idReferencedByAWay -> osmNodeList.containsKey(idReferencedByAWay))
                .count();

        return numberOfReferencesFromRelationFoundInMemory == getStreamOfReferencedIds(relationList).count() && numberOfReferencesFromWayFoundInMemory == getStreamOfReferencedIds(wayList).count();
    }

    private Stream getStreamOfReferencedIds(Map<String,OSMEntity> listToGetStreamFrom) {
        return listToGetStreamFrom.keySet().stream()
                .map(relationKey -> listToGetStreamFrom.get(relationKey).referencedIds.iterator())
                .filter(iterator -> iterator.hasNext())
                .map(iteratorReferencedIds -> iteratorReferencedIds.next());
    }

    private boolean canFindIdInOtherList(Set<String> taggedOSMRefferencedIds, Set<String> presentIds) {
        return taggedOSMRefferencedIds.stream().filter(referencedId -> presentIds.contains(referencedId)).count() == taggedOSMRefferencedIds.size();
    }

    public long getNumberOfReferencedIdsFromWay() {
        return wayList.keySet().stream()
                .map(relationKey -> wayList.get(relationKey).referencedIds.size()).mapToLong(t -> t).sum();
    }

    public long getNumberOfReferencedIdsFromRelation() {
        return relationList.keySet().stream()
                .map(relationKey -> relationList.get(relationKey).referencedIds.size()).mapToLong(t -> t).sum();
    }
*/
    public boolean finishBatch() {
        return true;
    }

    public boolean finish() {
        return true;
    }

}
