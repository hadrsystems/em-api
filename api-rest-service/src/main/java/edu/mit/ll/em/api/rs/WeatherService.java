package edu.mit.ll.em.api.rs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/weather")
public interface WeatherService {
    public static String DEFAULT_SEARCH_RANGE_IN_MILES = "10.0";
    public static String DEFAULT_SOURCE_CRS = "EPSG:3857";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public APIResponse getWeatherData(@QueryParam("longitude") Double longitude, @QueryParam("latitude") Double latitude,
                                   @DefaultValue(DEFAULT_SOURCE_CRS) @QueryParam("CRS") String locationCRS,
                                   @DefaultValue(DEFAULT_SEARCH_RANGE_IN_MILES) @QueryParam("searchRangeInMiles") Double searchRangeInMiles);
}
