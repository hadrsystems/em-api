/**
 * Copyright (c) 2008-2016, Massachusetts Institute of Technology (MIT)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.OrgOrgType;
import edu.mit.ll.nics.common.entity.OrgType;

public class OrganizationServiceResponse {

	private String message;
	
	private List<String> orgAdminList;
	
	private Collection<Org> Organizations = new ArrayList<Org>();
	
	private List<OrgType> orgTypes = new ArrayList<OrgType>();
	
	private List<OrgOrgType> orgOrgTypes = new ArrayList<OrgOrgType>();
	
	// TODO: Really used for returning a count REST request; i.e., do not get
	// the list of Organizations just the count.
	private int count;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection<Org> getOrganizations() {
		return Organizations;
	}

	public void setOrganizations(Collection<Org> Organizations) {
		this.Organizations = Organizations;
		if (Organizations != null) {
			count = Organizations.size();
		}
	}
		
	public List<OrgType> getOrgTypes() {
		return orgTypes;
	}

	public void setOrgTypes(List<OrgType> orgTypes) {
		this.orgTypes = orgTypes;
		if(orgTypes != null) {
			this.count = orgTypes.size();
		}
	}

	public List<OrgOrgType> getOrgOrgTypes() {
		return orgOrgTypes;
	}

	public void setOrgOrgTypes(List<OrgOrgType> orgOrgTypes) {
		this.orgOrgTypes = orgOrgTypes;
		if(orgOrgTypes != null) {
			this.count = orgOrgTypes.size();
		}
	}
	public List<String> getOrgAdminList() {
		return orgAdminList;
	}
	public void setOrgAdminList(List<String> orgAdminList) {
		this.orgAdminList = orgAdminList;
		if(orgAdminList != null) {
			this.count = orgAdminList.size();
		}
	}
	
	public String toString() {
		return "OrganizationServiceResponse [Organizations=" + Organizations + ", message="
				+ message + ", OrgTypes=" + orgTypes + ", OrgOrgTypes=" + orgOrgTypes + ", OrgAdminList=" + orgAdminList + "]";
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}	
}

