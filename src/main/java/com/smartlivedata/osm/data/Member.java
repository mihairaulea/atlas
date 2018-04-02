package com.smartlivedata.osm.data;

/**
 * Created by Mihai Raulea on 16/08/2017.
 */
public class Member {

    public String objectType = null;
    public long id;
    public String role = null;

    public Member(String objectType, long id, String role) {
        this.objectType = objectType;
        this.id = id;
        this.role = role;
    }

}
