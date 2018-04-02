package com.smartlivedata.atlas.core.parse;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Created by Mihai Raulea on 09/11/2017.
 */
public interface IAtlasParser extends Iterator {

    <T> Stream<T> getEntityStream(String data) throws Exception;
}
