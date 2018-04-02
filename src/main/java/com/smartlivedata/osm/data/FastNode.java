package com.smartlivedata.osm.data;

/**
 * Created by Mihai Raulea on 14/08/2017.
 */
// or should i just annotate the node as partOf either way or relation?
public class FastNode extends OSMEntity {

    @Override
    public String toString() {
        return String.valueOf(this.getId());
    }

}
