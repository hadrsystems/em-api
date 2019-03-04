package edu.mit.ll.em.api.rs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/locationBasedData")
public interface LocationBasedService {
    public static String DEFAULT_SEARCH_RANGE_IN_MILES_FOR_WEATHER_DATA = "20.0";
    public static String DEFAULT_SOURCE_CRS = "EPSG:4326";

    @GET
    @Path(value = "/weather")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Input:
     *  longitude : location longitude
     *  latitude : location latitude
     *  locationCRS : CRS of longitude , latitude values
     *  searchRange: search range (in miles) to find closest weather data
     *
     *  Returns: Location, jurisdiction (SRA, DPA, jurisdiction entity), weather data of given location
     *
     */
    public APIResponse getWeatherData(@QueryParam("longitude") Double longitude, @QueryParam("latitude") Double latitude,
                                   @DefaultValue(DEFAULT_SOURCE_CRS) @QueryParam("CRS") String locationCRS,
                                   @DefaultValue(DEFAULT_SEARCH_RANGE_IN_MILES_FOR_WEATHER_DATA) @QueryParam("searchRangeInMiles") Double searchRange);
}
