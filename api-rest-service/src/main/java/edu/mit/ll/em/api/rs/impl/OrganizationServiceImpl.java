/**
 * Copyright (c) 2008-2015, Massachusetts Institute of Technology (MIT)
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

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.rs.OrganizationService;
import edu.mit.ll.em.api.rs.OrganizationServiceResponse;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.OrgOrgType;
import edu.mit.ll.nics.common.entity.OrgType;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;


/**
 * Service endpoint for SSO management calls like creating and modifying users, and
 * other SSO related management 
 *
 */
public class OrganizationServiceImpl implements OrganizationService {

	/** Class Name */
	private static final String CNAME = OrganizationServiceImpl.class.getName();
	
	/** Organization DAO */
	private static final OrgDAOImpl orgDao = new OrgDAOImpl();
	
	
	/**
	 * Returns a OrganizationServiceResponse with the organizations list
	 * set to all organizations in the database
	 * 
	 * @return	Response
	 * @see OrganizationServiceResponse
	 */
	public Response getAllOrganizations() {
		Response response = null;
		OrganizationServiceResponse organizationResponse = new OrganizationServiceResponse();
		
		List<Org> allOrgs = null;
		try {			
			allOrgs = orgDao.getOrganizations();
			organizationResponse.setOrganizations(allOrgs);
			organizationResponse.setMessage("ok");
			if(allOrgs != null) {
				organizationResponse.setCount(allOrgs.size());
			}			
			response = Response.ok(organizationResponse).status(Status.OK).build();	
		} catch(Exception e) {
			APILogger.getInstance().e("OrganizationServiceImpl", "Unhandled exception while querying getAllOrganizations()");
			organizationResponse.setMessage("failure. Unable to read all Orgs.");
			response = Response.ok(organizationResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}
	
	
	/**
	 * Read and return orgs for specified user and workspace ids
	 * 
	 * @return Response
	 * @see OrganizationResponse
	 */
	public Response getOrganizations(Integer workspaceId, Integer userId) {
		Response response = null;
		OrganizationServiceResponse organizationResponse = new OrganizationServiceResponse();
		List<Org> organizations = null;
		try {			
			organizations = orgDao.getUserOrgs(userId, workspaceId);
			organizationResponse.getOrganizations().addAll(organizations);
			organizationResponse.setMessage("ok");
			if(organizations != null) {
				organizationResponse.setCount(organizationResponse.getOrganizations().size());
			}
			response = Response.ok(organizationResponse).status(Status.OK).build();			
		} catch (DataAccessException e) {			
			organizationResponse.setMessage("data access failure. Unable to read organizations for userid and "
					+ "workspaceid: " + userId + ", " + workspaceId + ": " + e.getMessage());
			organizationResponse.setCount(organizationResponse.getOrganizations().size());
			response = Response.ok(organizationResponse).status(Status.INTERNAL_SERVER_ERROR).build();			
		} catch(Exception e) {
			organizationResponse.setMessage("failure. Unhandled exception reading organizations for "
					+ "userid and workspaceid: "+ userId + ", " + workspaceId + ": " + e.getMessage());
			organizationResponse.setCount(organizationResponse.getOrganizations().size());
			response = Response.ok(organizationResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}
	

	/**
	 * Returns an OrganizationServiceResponse with orgTypes set to all orgTypes
	 * in the database
	 * 
	 * @return Response
	 * @See {@link OrganizationServiceResponse}
	 */
	public Response getOrganizationTypes() {
		Response response = null;
		
		OrganizationServiceResponse orgTypeResponse = new OrganizationServiceResponse();
		List<OrgType> orgTypes = null;
		
		try {			
			orgTypes = orgDao.getOrgTypes();
			if(orgTypes != null && !orgTypes.isEmpty()) {
				orgTypeResponse.setOrgTypes(orgTypes);
				orgTypeResponse.setMessage("ok");
				response = Response.ok(orgTypeResponse).status(Status.OK).build();
			} else {
				APILogger.getInstance().i("OrgService", "OrgDao returned no OrgTypes");
			}
		} catch(Exception e) {
			APILogger.getInstance().e("OrganizationServiceImpl", "Unhandled exception while quertying getOrganizationTypes()");
			orgTypeResponse.setMessage("failure. Unable to read OrgTypes.");
			orgTypeResponse.setCount(orgTypeResponse.getOrgTypes().size());
			response = Response.ok(orgTypeResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}		
		
		return response;
	}


	@Override
	public Response getOrganizationTypeMap() {

		Response response = null;
		
		OrganizationServiceResponse orgTypeMapResponse = new OrganizationServiceResponse();
		List<OrgOrgType> orgTypesMap = null;
		
		try {			
			orgTypesMap = orgDao.getOrgOrgTypes();
			if(orgTypesMap != null && !orgTypesMap.isEmpty()) {
				orgTypeMapResponse.setOrgOrgTypes(orgTypesMap);
				orgTypeMapResponse.setMessage("ok");
				response = Response.ok(orgTypeMapResponse).status(Status.OK).build();
			} else {
				APILogger.getInstance().i("OrgService", "OrgDao returned no OrgOrgTypes");
			}
		} catch(Exception e) {
			APILogger.getInstance().e("OrganizationServiceImpl", "Unhandled exception while quertying getOrgOrgTypes()");
			orgTypeMapResponse.setMessage("failure. Unable to read OrgOrgTypes.");
			orgTypeMapResponse.setCount(orgTypeMapResponse.getOrgOrgTypes().size());
			response = Response.ok(orgTypeMapResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}		
		
		return response;
	}
}
