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
package edu.mit.ll.em.api.service;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.nics.common.constants.SADisplayConstants;

import java.util.concurrent.Callable;

public class JurisdictionLocatorServiceAsync implements Callable {
    private JurisdictionLocatorService jurisdictionLocatorService =  null;
    private Coordinate coordinate = null;

    public JurisdictionLocatorServiceAsync(JurisdictionLocatorService jurisdictionLocatorService, Coordinate coordinate) {
        this.jurisdictionLocatorService = jurisdictionLocatorService;
        this.coordinate = coordinate;
    }

    public Jurisdiction call() {
        Jurisdiction jurisdiction = null;
        try {
            jurisdiction = jurisdictionLocatorService.getJurisdiction(coordinate, SADisplayConstants.CRS_4326);
        } catch (Exception e) {
            System.out.println("Exception getting jurisdiction details: " + e.getMessage());
            e.printStackTrace();
        }
        return jurisdiction;
    }
}
