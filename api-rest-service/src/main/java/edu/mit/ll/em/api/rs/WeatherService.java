package edu.mit.ll.em.api.rs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/weather")
public interface WeatherService {
    public static String DEFAULT_SEARCH_RANGE_IN_MILES = "10.0";
    public static String DEFAULT_SOURCE_CRS = "EPSG:3857";

    public APIResponse getWeatherData(@QueryParam("latitude") Double latitude, @QueryParam("longitude") Double longitude,
                                   @DefaultValue(DEFAULT_SOURCE_CRS) @QueryParam("CRS") String locationCRS,
                                   @DefaultValue(DEFAULT_SEARCH_RANGE_IN_MILES) @QueryParam("searchRangeInMiles") Double searchRangeInMiles);
}
