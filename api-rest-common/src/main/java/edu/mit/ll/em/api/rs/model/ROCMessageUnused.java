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
