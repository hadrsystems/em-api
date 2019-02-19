package edu.mit.ll.em.api.rs.impl;

import java.util.*;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.model.*;
import edu.mit.ll.em.api.rs.response.ROCLocationBasedDataResponse;
import edu.mit.ll.em.api.service.ROCService;
import org.geotools.referencing.CRS;
import org.springframework.util.CollectionUtils;
import javax.ws.rs.core.Response;

import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.nicsdao.IncidentDAO;

import edu.mit.ll.em.api.rs.ROCReportService;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.response.ROCDataResponse;

public class ROCReportServiceImpl implements ROCReportService {

    private static final String CNAME = ROCReportServiceImpl.class.getName();
    private static final int ROC_FORM_TYPE_ID = 1;
    private IncidentDAO incidentDao = null;
    private ROCService rocService = null;
    private CRSTransformer crsTransformer = null;
    private APILogger logger = null;

    public ROCReportServiceImpl(IncidentDAO incidentDao, ROCService rocService, CRSTransformer crsTransformer, APILogger logger) {
        this.incidentDao = incidentDao;
        this.rocService = rocService;
        this.crsTransformer = crsTransformer;
        this.logger = logger;
    }

    public Response getROCLocationBasedData(Double longitude, Double latitude, String locationCRS, Double searchRange) {
        Map<String, String> errors = validateParams(longitude, latitude, locationCRS, searchRange);
        if(!CollectionUtils.isEmpty(errors)) {
            ValidationErrorResponse errorResponseEntity = new ValidationErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST.name(), errors);
            return Response.ok().entity(errorResponseEntity).build();
        }

        Coordinate coordinate = new Coordinate(longitude, latitude);
        Response response;
        try {
            Double searchRangeInKilometers = searchRange * SADisplayConstants.KM_PER_MILE;
            Coordinate coordinatesIn4326 = crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326);
            ROCLocationBasedData rocLocationBasedData = rocService.getROCLocationBasedData(coordinatesIn4326, searchRangeInKilometers);
            response = Response.ok().entity(new ROCLocationBasedDataResponse(rocLocationBasedData)).build();
        } catch(Exception e) {
            String errorMessage = String.format("Error getting location based data for given location %.20f, %.20f ", longitude, latitude);
            logger.e(CNAME, errorMessage, e);
            APIResponse apiResponse = new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), Response.Status.INTERNAL_SERVER_ERROR.name(), errorMessage + e.getMessage());
            response = Response.ok().entity(apiResponse).build();
        }
        return response;
    }

    public Response getEditROCFormForAnIncident(Integer incidentId, Double searchRange) {
        Incident incident = incidentId == null ? null : this.incidentDao.getIncident(incidentId);
        Map<String, String> errors = this.validateParams(incidentId, incident, searchRange);
        if(!errors.isEmpty()){
            ValidationErrorResponse errorResponseEntity = new ValidationErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST.name(), errors);
            return Response.ok().entity(errorResponseEntity).build();
        }

        Response response = null;
        try {
            ROCForm rocForm = rocService.getEditROCForm(incident, searchRange * SADisplayConstants.KM_PER_MILE);
            response = Response.ok().entity(new ROCDataResponse(rocForm)).build();
        } catch(Exception e) {
            String errorMessage = String.format("Error getting location based data for given incident id %d ", incidentId);
            logger.e(CNAME, errorMessage, e);
            response = Response.ok().entity(new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), Response.Status.INTERNAL_SERVER_ERROR.name(), errorMessage + e.getMessage())).build();
        }
        return response;
    }

    private Map<String, String> validateParams(Integer incidentId, Incident incident, Double searchRange) {
        Map<String, String> errors = new HashMap<String, String>();
        if(searchRange <=0 ) {
            errors.put("searchRangeInMiles", "Please provide valid search range > 0");
        }
        if(incidentId == null || incident == null) {
            errors.put("incidentId", "Invalid incidentId " + incidentId);
        }
        return errors;
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
