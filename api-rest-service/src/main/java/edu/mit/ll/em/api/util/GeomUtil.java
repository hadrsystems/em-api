/**
 * Copyright (c) 2008-2015, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.mit.ll.em.api.util;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * A utility Singleton to handle Geometry based operations.
 * @author SA23148
 *
 */
public class GeomUtil {

	private static final String CNAME = GeomUtil.class.getName();
	//private GeometryFactory geomFactory= new GeometryFactory(new PrecisionModel(), 4326);
	private GeometryFactory geomFactory= new GeometryFactory();
	public static final String NICS_EPSG = "EPSG:3857";
	public static final String ANDROID_EPSG = "EPSG:4326";
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static GeomUtil instance = new GeomUtil();
	}

	public static  GeomUtil getInstance() {
		return Holder.instance;
	}

	/**
	 * Construct a Coordinate from an array of LON, LAT, and possibly ALT.
	 * @param llaArray Array with LON, LAT, and optionally ALT; in this order.
	 * @return A Coordinate corresponding to the elements passed in.
	 * @throws NullPointerException When llaArray is null.
	 * @throws IllegalArgumentException When LON and/or LAT are missing.
	 */
	public Coordinate makeCoordinate(Double[] llaArray)
			throws NullPointerException {
		if (llaArray == null) {
			throw new NullPointerException("Unexpected null argument \"llaArray\"");
		}
		if (llaArray.length < 2) {
			throw new IllegalArgumentException("A LON and LAT must be passed in at minumum.");
		}
		Double alt = (llaArray.length > 2) ? llaArray[2] : null; 
		Coordinate lla = makeCoordinate(llaArray[0], llaArray[1], alt);
		return lla;
	}
	

	/**
	 * Construct a Coordinate from an array of LON, LAT, and possibly ALT.
	 * @param lon Longitude
	 * @param lat Latitude
	 * @param alt Altitude. Can be null.
	 * @return A Coordinate corresponding to the elements passed in.
	 * @throws NullPointerException When llaArray is null.
	 */
	public Coordinate makeCoordinate(double lon, double lat, Double alt)
			throws NullPointerException {
		Coordinate lla = new Coordinate();
		lla.x = lon; // Longitude
		lla.y = lat;  // Latitude
		if (alt != null) {
			lla.z =  lat;
		}
		return lla;
	}
	
	public Geometry createPoint(Double[] llaArray) throws GeomUtilException {	
		Geometry g = null;
		try {
			Coordinate c = GeomUtil.getInstance().makeCoordinate(llaArray);
			g = geomFactory.createPoint(c);
		} catch (Exception e) {
			throw new GeomUtilException("GeomUtil.createPoint-> " + e.getMessage());
		}
		return g;
	}
	
	public Geometry createLine(Double[][] points) throws GeomUtilException {	
		Geometry g = null;
		try {
			if (points == null) {
				throw new NullPointerException("Unexpected null argument \"llaArray\"");
			}
			if (points.length < 2) {
				throw new IllegalArgumentException("A line needs two points at minimum.");
			}				
			Coordinate c1 = GeomUtil.getInstance().makeCoordinate(points[0]);
			Coordinate c2 = GeomUtil.getInstance().makeCoordinate(points[1]);
			g = geomFactory.createLineString(new Coordinate[] {c1, c2});
		} catch (Exception e) {
			throw new GeomUtilException("GeomUtil.createLine-> " + e.getMessage());
		}
		return g;
	}

	public Geometry createPolygon(Double[][] points) throws GeomUtilException {
		Geometry g = null;
		try {
			if (points == null) {
				throw new NullPointerException("Unexpected null argument \"llaArray\"");
			}
			if (points.length < 3) {
				throw new IllegalArgumentException("A polygon needs three points at minimum.");
			}					
			List<Coordinate> coords = new ArrayList<Coordinate>();
			for (int n = 0; n < points.length; ++n) {
				Coordinate c = GeomUtil.getInstance().makeCoordinate(points[n]);
				coords.add(c);
			}
			coords.add(GeomUtil.getInstance().makeCoordinate(points[0])); // To close the poly.
			LinearRing ring = geomFactory.createLinearRing(
					coords.toArray(new Coordinate[0]));
			g = geomFactory.createPolygon(ring);
		} catch (Exception e) {
			throw new GeomUtilException("GeomUtil.createPolygon-> " + e.getMessage());
		}
		return g;
	}

	public Geometry createCircle(Double[] centre, double radius) throws GeomUtilException {
		Geometry g = null;
		try {
			if (centre == null) {
				throw new NullPointerException("Unexpected null argument \"centre\"");
			}			
			Coordinate c_coord = GeomUtil.getInstance().makeCoordinate(centre);
			GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
			shapeFactory.setNumPoints(32);
			shapeFactory.setCentre(c_coord);
			shapeFactory.setSize(radius * 2);
			g = shapeFactory.createCircle();
			// TODO: See if create3DCircle solves the elliptical "circles" problem. 
			//g = create3DCircle(centre[0], centre[1],radius);
		} catch (Exception e) {
			throw new GeomUtilException("GeomUtil.createCircle-> " + e.getMessage());
		}
		return g;
	}
	
	// Lifted from: http://osgeo-org.1560.x6.nabble.com/how-to-programmatically-create-circles-td5001091.html#a5001108
	/*
	private Geometry create3DCircle(double lng, double lat, double radiusNm) { 
        GeodeticCalculator calc = new GeodeticCalculator(DefaultEllipsoid.WGS84); 
        calc.setStartingGeographicPoint(lng, lat); 
        final int SIDES = 32 + 16 * ((int)Math.ceil(radiusNm / 40) / 5);       // Fairly random. 

        double distance = radiusNm * 1852;   // Convert to metres.	1855.3248 
        double baseAzimuth = 360.0 / SIDES; 
        Coordinate coords[] = new Coordinate[SIDES+1]; 
        for( int i = 0; i < SIDES; i++){ 
                double azimuth = 180 - (i * baseAzimuth); 
                calc.setDirection(azimuth, distance); 
                Point2D point = calc.getDestinationGeographicPoint(); 
                coords[i] = new Coordinate(point.getX(), point.getY()); 
        } 
        coords[SIDES] = coords[0]; 
        LinearRing ring = geomFactory.createLinearRing( coords ); 
        Polygon polygon = geomFactory.createPolygon( ring, null );

        return polygon; 
	} 
*/
	
	
	public Double[][] makePointsFromGeom(Geometry geom, MathTransform trans)
			throws GeomUtilException {
		if (geom == null) {
			return null;
		}
		List<Double[]> points = new ArrayList<Double[]>();
		Coordinate[] coords = geom.getCoordinates();
		if (coords == null || coords.length < 1) {
			return points.toArray(new Double[0][]);
		}
		for (int n = 0; n < coords.length; ++n) {	
			Double[] p = new Double[3];
			Coordinate c = coords[n];
			if (trans != null) {
				c = transformCoordinate(c, trans);
			}
			p[0] = c.x;
			p[1] = c.y;
			p[2] = c.z;
			points.add(p);
		}
		return points.toArray(new Double[0][]);
	}
	
	public MathTransform createTransform(String toCRS, String fromCRS)
			throws GeomUtilException {
		int numCrsArgs = 0;
		if (toCRS != null && !toCRS.isEmpty()) numCrsArgs++;
		if (fromCRS != null && !fromCRS.isEmpty()) numCrsArgs++;
		if (numCrsArgs != 2) {
			return null;
		}
		
		MathTransform ret = null;
		try {
			CoordinateReferenceSystem targetCRS = CRS.decode(toCRS);			
			CoordinateReferenceSystem sourceCRS = CRS.decode(fromCRS);
			ret = CRS.findMathTransform(sourceCRS, targetCRS);			
		} catch (NoSuchAuthorityCodeException e) {
			throw new GeomUtilException(CNAME + ":transformCoordinate" + e.getMessage());
		} catch (FactoryException e) {
			throw new GeomUtilException(CNAME + ":transformCoordinate" + e.getMessage());		
		}		
		return ret;
	}
	
	public Coordinate transformCoordinate(Coordinate coord, MathTransform trans)
			throws GeomUtilException {
		if (coord == null) {
			throw new NullPointerException("Unexpected null argument \"coord\".");
		}
		if (trans == null) {
			throw new NullPointerException("Unexpected null argument \"trans\".");
		}
		Point p = geomFactory.createPoint(coord);
		Geometry g = transformGeometry(p, trans);
		return g.getCoordinate();
	}
	
	public Geometry transformGeometry(Geometry in, MathTransform trans)
			throws GeomUtilException {
		Geometry out = null;
		try {
			out = JTS.transform(in, trans);
		} catch (MismatchedDimensionException e) {
			throw new GeomUtilException(CNAME + ":transformCoordinate(): MismatchedDimensionException -> "
					+ e.getMessage());
		} catch (TransformException e) {
			throw new GeomUtilException(CNAME + ":transformCoordinate(): TransformException -> "
					+ e.getMessage());
		} catch (Exception e) {
			throw new GeomUtilException(CNAME + ":transformCoordinate(): Exception -> "
					+ e.getMessage());
		}
		return out;		
	}
}
