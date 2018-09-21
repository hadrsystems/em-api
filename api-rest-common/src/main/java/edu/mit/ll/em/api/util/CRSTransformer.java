package edu.mit.ll.em.api.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class CRSTransformer {

    private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public Coordinate transformCoordinatesToTargetCRS(Double longitude, Double latitude,
                                                             String sourceCRSStr, String targetCRSStr) {
        if(sourceCRSStr.equalsIgnoreCase(targetCRSStr)) {
            return new Coordinate(longitude, latitude);
        }
        Coordinate pointCoordinates = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(pointCoordinates);
        try {
            MathTransform transform = CRS.findMathTransform(CRS.decode(sourceCRSStr, true), CRS.decode(targetCRSStr, true), false);
            return ((Point) JTS.transform(point, transform)).getCoordinate();
        } catch(FactoryException | TransformException e) {
            throw new RuntimeException("Unable to transform given coordinates from %s CRS to %s CRS", e);
        }
    }
}
