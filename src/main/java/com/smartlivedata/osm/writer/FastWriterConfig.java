package com.smartlivedata.osm.writer;

public class FastWriterConfig {

    // if a new node should be created right away, or it should first check for and update existing node
    public boolean SEARCH_BEFORE_INSERT;
    // NO_LABELS   -- tags do not become labels
    // SPECIFIC    -- some tags become labels
    // ALWAYS      -- all tags become labels
    public String TAGS_TO_LABELS;
    //public boolean

}
