package edu.mit.ll.em.api.rs.impl;

import java.util.HashMap;
import java.util.Map;
import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.exception.GeocodeException;
import edu.mit.ll.em.api.gateway.geocode.GeocodeAPIGateway;
import edu.mit.ll.em.api.service.JurisdictionLocatorService;
import org.geotools.referencing.CRS;
import org.springframework.util.CollectionUtils;
import javax.ws.rs.core.Response;

import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.nicsdao.IncidentDAO;
import edu.mit.ll.nics.common.entity.Weather;
import edu.mit.ll.nics.nicsdao.WeatherDAO;

import edu.mit.ll.em.api.rs.ROCReportService;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.rs.model.mapper.ROCDataModelMapper;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.ROCDataResponse;
import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.ROCData;

public class ROCReportServiceImpl implements ROCReportService {

    private static final String CNAME = ROCReportServiceImpl.class.getName();
    private JurisdictionLocatorService jurisdictionLocatorService = null;
    private WeatherDAO weatherDao = null;
    private GeocodeAPIGateway geocodeAPIGateway = null;
    private IncidentDAO incidentDao = null;
    private CRSTransformer crsTransformer = null;
    private ROCDataModelMapper rocDataModelMapper = null;
    private APILogger logger = null;

    public ROCReportServiceImpl(JurisdictionLocatorService jurisdictionLocatorService, WeatherDAO weatherDao, GeocodeAPIGateway geocodeAPIGateway, CRSTransformer crsTransformer, ROCDataModelMapper rocDataModelMapper, APILogger logger) {
        this.jurisdictionLocatorService = jurisdictionLocatorService;
        this.weatherDao = weatherDao;
        this.geocodeAPIGateway = geocodeAPIGateway;
        this.crsTransformer = crsTransformer;
        this.rocDataModelMapper = rocDataModelMapper;
        this.logger = logger;
    }

    public APIResponse getROCFormDataByLocation(Double longitude, Double latitude, String locationCRS, Double searchRange) {
        Map<String, String> errors = validateParams(longitude, latitude, locationCRS, searchRange);
        if(!CollectionUtils.isEmpty(errors)) {
            return new ValidationErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid Request, please review the errors for more info.", errors);
        }

        Coordinate coordinate = new Coordinate(longitude, latitude);
        APIResponse response;
        try {
            Double searchRangeInKilometers = searchRange * SADisplayConstants.KM_PER_MILE;
            Coordinate coordinatesIn4326 = crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326);
            response = this.getROCData(coordinatesIn4326, searchRangeInKilometers, null, null, null, null);
        } catch(Exception e) {
            String errorMessage = String.format("Error getting location based data for given location %.20f, %.20f ", longitude, latitude);
            logger.e(CNAME, errorMessage, e);
            response = new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), errorMessage + e.getMessage());
        }
        return response;
    }

    private APIResponse getROCData(Coordinate coordinatesIn4326, Double rangeInKilometers, Incident incident, String latestReportType, String incidentCause, String generalLocation) {
        APIResponse response;
        try {
            Jurisdiction jurisdiction = jurisdictionLocatorService.getJurisdiction(coordinatesIn4326, SADisplayConstants.CRS_4326);
            Weather weather = weatherDao.getWeatherDataFromLocation(coordinatesIn4326, rangeInKilometers);
            Location location = geocodeAPIGateway.getLocationByGeocode(coordinatesIn4326);
            ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
            response = new ROCDataResponse(rocData);
        } catch(Exception e) {
            String errorMessage = String.format("Error getting location based data for given location %.20f, %.20f ", coordinatesIn4326.x, coordinatesIn4326.y);
            logger.e(CNAME, errorMessage, e);
            response = new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), errorMessage + e.getMessage());
        }
        return response;
    }

    public APIResponse rocFormLocationBasedDataForAnIncident(Integer incidentId, Double searchRange) {
        if(searchRange <=0 ) {
            searchRange = Double.parseDouble(DEFAULT_SEARCH_RANGE_IN_MILES_FOR_WEATHER_DATA);
        }
//        try {
//            Incident incident = this.incidentDao.getIncident(incidentId);
//            Coordinate incidentLocationCoordinates = new Coordinate(incident.getLon(), incident.getLat());
//            Jurisdiction jurisdiction = jurisdictionLocatorService.getJurisdiction(coordinate, CRS_4326);
//            Weather weather = weatherDAO.getWeatherDataFromLocation(coordinate, rangeInKilometers);
//            Location location = null; //fill these details later
//            ROCData rocFormData = ROCDataFactory.convertToROCData(incident, jurisdiction, location, weather, "Update", "Incident Cause", "general location");
//        } catch(Exception e) {
//            String errorMessage = String.format("Error getting location based data for given incident id %d ", incidentId);
//            logger.e(CNAME, errorMessage, e);
//            response = new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), errorMessage + e.getMessage());
//        }
        return null;
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
