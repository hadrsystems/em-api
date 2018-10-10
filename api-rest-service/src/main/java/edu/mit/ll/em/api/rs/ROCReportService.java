package edu.mit.ll.em.api.rs;

import edu.mit.ll.nics.common.constants.SADisplayConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/reports")
public interface ROCReportService {

    public static final String DEFAULT_SEARCH_RANGE_IN_MILES_FOR_WEATHER_DATA = "10.0";

    @GET
    @Path(value = "/1/locationBasedData/")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Input:
     *  longitude : location longitude
     *  latitude : location latitude
     *  locationCRS : CRS of longitude, latitude values
     *  searchRange: search range (in miles) to find closest weather data
     *
     *  Returns: Location, jurisdiction (SRA, DPA, jurisdiction entity), weather data of given location
     *
     */
    public APIResponse getROCFormDataByLocation(@QueryParam("longitude") Double longitude, @QueryParam("latitude") Double latitude,
                                                @DefaultValue(SADisplayConstants.CRS_4326) @QueryParam("CRS") String locationCRS,
                                                @DefaultValue(DEFAULT_SEARCH_RANGE_IN_MILES_FOR_WEATHER_DATA) @QueryParam("searchRangeInMiles") Double searchRange);

    @GET
    @Path(value = "{incidentId}/1/locationBasedData")
    @Produces(MediaType.APPLICATION_JSON)
    public APIResponse rocFormLocationBasedDataForAnIncident(@PathParam("incidentId") Integer incidentId, @DefaultValue(DEFAULT_SEARCH_RANGE_IN_MILES_FOR_WEATHER_DATA) @QueryParam("searchRangeInMiles") Double searchRange);

}
