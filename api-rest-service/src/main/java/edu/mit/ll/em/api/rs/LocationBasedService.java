/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
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
