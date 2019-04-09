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
package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.ROCLocationBasedData;
import edu.mit.ll.nics.common.entity.Jurisdiction;
import edu.mit.ll.nics.common.entity.Weather;

public class ROCLocationBasedDataBuilder {
    private ROCLocationBasedData rocLocationBasedData = new ROCLocationBasedData();

    public ROCLocationBasedDataBuilder buildLocationData(Location location) {
        if(location != null) {
            rocLocationBasedData.setLocation(location.getSpecificLocation());
            rocLocationBasedData.setCounty(location.getCounty());
            rocLocationBasedData.setState(location.getState());
        }
        return this;
    }

    public ROCLocationBasedDataBuilder buildJurisdictionData(Jurisdiction jurisdiction) {
        if(jurisdiction != null) {
            rocLocationBasedData.setSra(jurisdiction.getSra());
            rocLocationBasedData.setDpa(jurisdiction.getDpa());
            rocLocationBasedData.setJurisdiction(jurisdiction.getJurisdiction());
        }
        return this;
    }

    public ROCLocationBasedDataBuilder buildWeatherData(Weather weather) {
        if(weather != null) {
            rocLocationBasedData.setTemperature(weather.getAirTemperature());
            rocLocationBasedData.setRelHumidity(weather.getHumidity());
            rocLocationBasedData.setWindSpeed(weather.getWindSpeed());
            rocLocationBasedData.setWindDirection(weather.getWindDirection());
        }
        return this;
    }

    public ROCLocationBasedData build() {
        return this.rocLocationBasedData;
    }
}