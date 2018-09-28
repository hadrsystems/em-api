package edu.mit.ll.em.api.util;

import com.vividsolutions.jts.geom.Coordinate;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CRSTranformerTest {

    private CRSTransformer crsTransformer = new CRSTransformer();

    @Test
    public void returnsInputCoordinateIfSourceAndTargetCRSAreSame() throws Exception {
        Coordinate inputLocation = new Coordinate(-121.45488739013672, 38.574038169691875);
        assertEquals(crsTransformer.transformCoordinatesToTargetCRS(inputLocation.x, inputLocation.y, "EPSG:4326", "EPSG:4326"), inputLocation);
    }

    @Test
    public void returnsTranformedCoordinates() throws Exception {
        Coordinate locationIn3857CRS = new Coordinate(-13520296.2186244, 4660838.56865971);
        Coordinate tranformedLocationIn4326CRS = new Coordinate(-121.4548873901367, 38.57403816969187);
        assertEquals(crsTransformer.transformCoordinatesToTargetCRS(locationIn3857CRS.x, locationIn3857CRS.y, "EPSG:3857", "EPSG:4326"), tranformedLocationIn4326CRS);
    }

    @Test(expected = RuntimeException.class)
    public void failsToTranformGivenCoordinatesInIncorrectCRS() throws Exception {
        Coordinate locationIn3857CRS = new Coordinate(-13520296.2186244, 4660838.56865971);
        crsTransformer.transformCoordinatesToTargetCRS(locationIn3857CRS.x, locationIn3857CRS.y, "EPSG:4326", "EPSG:3857");
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionOnInvalidSourceCRS() throws Exception {
        crsTransformer.transformCoordinatesToTargetCRS(0.0, 0.0, "EPSG:38577", "EPSG:4326");
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionOnInvalidTargetCRS() throws Exception {
        crsTransformer.transformCoordinatesToTargetCRS(0.0, 0.0, "EPSG:3857", "EPSG:43265");
    }
}
