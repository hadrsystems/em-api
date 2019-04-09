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

import org.apache.commons.lang.builder.CompareToBuilder;

import java.util.Date;

public class ROCMessageUnused {

    private Date dateCreated;
    private ROCMessage report;

    public ROCMessageUnused(Date dateCreated, ROCMessage report) {
        this.dateCreated = dateCreated;
        this.report = report;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public ROCMessage getRocReport() {
        return report;
    }

    public String getReportType() {
        return this.report == null? null : this.report.getReportType();
    }

    public int compareTo(Object other) {
        ROCMessageUnused otherROCMessage =  (ROCMessageUnused) other;
        if(other == null || "FINAL".equals(this.getRocReport().getReportType())) {
            return 1;
        } else if("FINAL".equals(this.getRocReport().getReportType())) {
            return -1;
        } else {
            return CompareToBuilder.reflectionCompare(this.getDateCreated(), otherROCMessage.getDateCreated());
        }
    }
}
