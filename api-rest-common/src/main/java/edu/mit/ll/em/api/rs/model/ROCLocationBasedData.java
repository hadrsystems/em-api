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
package edu.mit.ll.em.api.rs.model;

public class ROCLocationBasedData {

    private String location; //specific location
    private String county;
    private String state;

    //Jurisdiction info pre populated based on incident location
    private String sra;
    private String dpa;
    private String jurisdiction;

    //Weather data pre populated based on incident location
    private Double temperature;
    private Float relHumidity;
    private Float windSpeed;
    private Double windDirection;

    public ROCLocationBasedData() {
    }

    public String getLocation() {
        return location;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

    public String getSra() {
        return sra;
    }

    public String getDpa() {
        return dpa;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Float getRelHumidity() {
        return relHumidity;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public Double getWindDirection() {
        return windDirection;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setSra(String sra) {
        this.sra = sra;
    }

    public void setDpa(String dpa) {
        this.dpa = dpa;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setRelHumidity(Float relHumidity) {
        this.relHumidity = relHumidity;
    }

    public void setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindDirection(Double windDirection) {
        this.windDirection = windDirection;
    }
}
