package edu.mit.ll.em.api.rs.model;

import org.apache.commons.lang.StringUtils;

public class DirectProtectionArea {
    private String dpaGroup;
    private String agreements;

    public DirectProtectionArea(String dpaGroup, String agreements) {
        this.dpaGroup = dpaGroup;
        this.agreements = agreements;
    }

    public String getDirectProtectionAreaGroup() {
        return StringUtils.capitalize(StringUtils.lowerCase(dpaGroup));
    }

    public boolean isContractCounty() {
        return StringUtils.isBlank(this.agreements) ? false : this.agreements.toLowerCase().contains("contract county");
    }
}