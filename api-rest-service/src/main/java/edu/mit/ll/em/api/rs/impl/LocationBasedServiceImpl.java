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
package edu.mit.ll.em.api.rs.impl;

import javax.ws.rs.core.Response;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.*;
import edu.mit.ll.em.api.rs.model.WeatherModel;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Weather;
import edu.mit.ll.nics.nicsdao.WeatherDAO;

import org.geotools.referencing.CRS;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

public class LocationBasedServiceImpl implements LocationBasedService {
    private static String CNAME = LocationBasedServiceImpl.class.getName();

    private CRSTransformer crsTransformer = null;
    private WeatherDAO weatherDAO = null;
    private APILogger logger = null;

    public LocationBasedServiceImpl(WeatherDAO weatherDAO, CRSTransformer crsTransformer, APILogger logger) {
        this.weatherDAO = weatherDAO;
        this.crsTransformer = crsTransformer;
        this.logger = logger;
    }

    public APIResponse getWeatherData(Double longitude, Double latitude, String locationCRS, Double searchRange) {
        Map<String, String> errors = validateParams(longitude, latitude, locationCRS, searchRange);
        if(!CollectionUtils.isEmpty(errors)) {
            return new ValidationErrorResponse(Response.Status.OK.getStatusCode(), "BAD_REQUEST", errors);
        }

        APIResponse response = null;
        try {
            Coordinate latLongInEPSG4326 = crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326);
            Double rangeInKilometers = searchRange.doubleValue() * SADisplayConstants.KM_PER_MILE;
            Weather weather = weatherDAO.getWeatherDataFromLocation(latLongInEPSG4326, rangeInKilometers);
            if (weather == null) {
                response = new APIResponse(Response.Status.OK.getStatusCode(), "NO_DATA_FOUND");
            } else {
                response = new WeatherResponse(Response.Status.OK.getStatusCode(), "OK", new WeatherModel(weather));
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to fetch weather data for given location [%.20f, %.20f] %s CRS in search range of %.5f", longitude, latitude, locationCRS, searchRange);
            logger.e(CNAME, errorMessage, e);
            response = new APIResponse(Response.Status.OK.getStatusCode(), "INTERNAL_SERVER_ERROR", errorMessage + e.getMessage());
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
