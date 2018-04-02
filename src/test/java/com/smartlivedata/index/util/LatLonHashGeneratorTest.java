package com.smartlivedata.index.util;

import com.smartlivedata.index.geo.BoundingBox;
import com.smartlivedata.index.geo.GeoHash;
import com.smartlivedata.index.geo.WGS84Point;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class LatLonHashGeneratorTest {

    @Test
    public void testIndexingSimple() {
        GeoHash geoHash = GeoHash.withCharacterPrecision(-50.84077,-48.97582,12);
        GeoHash geoHash2 = GeoHash.withCharacterPrecision(-50.84040,-48.97530,12);

        assertTrue(!geoHash.equals(geoHash2));
    }

    @Test
    public void getPointsInBoundingBox() {
        int precision = 5;

        double latitudeOne = -50.84077;
        double longitudeOne = -48.97582;

        GeoHash geoHashUnderTest = GeoHash.withCharacterPrecision(latitudeOne, longitudeOne, precision);

        TwoGeoHashBoundingBox geoHashBoundingBox = TwoGeoHashBoundingBox.withCharacterPrecision(new BoundingBox(new WGS84Point(-50, -48), new WGS84Point(-51, -49)), precision);
        assertTrue(geoHashBoundingBox.getBoundingBox().contains(new WGS84Point(latitudeOne, longitudeOne)));

        BoundingBoxSampler sampler = new BoundingBoxSampler(geoHashBoundingBox);
        GeoHash currentGeoHash = null;
        boolean foundIt = false;
        do{
            currentGeoHash = sampler.next();
            if(currentGeoHash!=null) {
                if(currentGeoHash.equals(geoHashUnderTest)) {
                    foundIt = true;
                }
            }
        } while(currentGeoHash!=null || foundIt == false);

        assertTrue(foundIt);
    }

}
