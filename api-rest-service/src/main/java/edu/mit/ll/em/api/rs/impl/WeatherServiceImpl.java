package edu.mit.ll.em.api.rs.impl;

import javax.ws.rs.core.Response;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.rs.WeatherResponse;
import edu.mit.ll.em.api.rs.WeatherService;
import edu.mit.ll.em.api.rs.model.WeatherModel;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.nics.common.entity.Weather;
import edu.mit.ll.nics.nicsdao.WeatherDAO;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.dao.DataAccessException;
import org.springframework.util.CollectionUtils;

import org.geotools.referencing.CRS;

import java.util.HashMap;
import java.util.Map;

public class WeatherServiceImpl implements WeatherService {
    private static String CNAME = WeatherServiceImpl.class.getName();
    public static final String CRS_4326 = "EPSG:4326";
    public static final double KM_PER_MILE = 1.60934;
    private WeatherDAO weatherDAO = null;
    private CRSTransformer crsTransformer = null;
    private APILogger logger = null;

    public WeatherServiceImpl(WeatherDAO weatherDAO, CRSTransformer crsTransformer, APILogger logger) {
        this.weatherDAO = weatherDAO;
        this.crsTransformer = crsTransformer;
        this.logger = logger;
    }

    public APIResponse getWeatherData(Double longitude, Double latitude, String locationCRS, Double searchRangeInMiles) {
        Map<String, String> errors = validateParams(longitude, latitude, locationCRS, searchRangeInMiles);
        if(!CollectionUtils.isEmpty(errors)) {
            return new ValidationErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid Request, please review the errors for more info.", errors);
        }

        APIResponse response = null;
        try {
            Coordinate latLongInEPSG4326 = crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, CRS_4326);
            Double rangeInKilometers = searchRangeInMiles.doubleValue() * KM_PER_MILE;
            Weather weather = weatherDAO.getWeatherDataFromLocation(latLongInEPSG4326, rangeInKilometers);
            if (weather == null) {
                response = new APIResponse(Response.Status.OK.getStatusCode(), "NO_DATA_FOUND");
            } else {
                response = new WeatherResponse(Response.Status.OK.getStatusCode(), "OK", new WeatherModel(weather));
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to fetch weather data for given location [%.20f, %.20f] %s CRS in search range of %.5f", longitude, latitude, locationCRS, searchRangeInMiles);
            logger.e(CNAME, errorMessage, e);
            response = new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), errorMessage + e.getMessage());
        }

        return response;
    }

    private Map<String, String> validateParams(Double longitude, Double latitude, String locationCRS, Double searchRangeInMiles) {
        Map<String, String> errors = new HashMap<String, String>();

        if(longitude == null) {
            errors.put("longitude", "Longitude is required");
        }
        if(latitude == null) {
            errors.put("latitude", "Latitude is required");
        }
        try {
            CRS.decode(locationCRS);
        } catch(Exception e) {
            errors.put("locationCRS", "No Such CRS exists, please provide a valid CRS");
        }
        if(searchRangeInMiles <= 0) {
            errors.put("searchRangeInMiles", "Please provide valid search range > 0");
        }
        return errors;
    }

}
