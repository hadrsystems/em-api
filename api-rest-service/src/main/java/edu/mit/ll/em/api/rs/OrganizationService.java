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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.nics.common.entity.Org;

@Path("/orgs/{workspaceId}")
public interface OrganizationService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrganizations(@PathParam("workspaceId") Integer workspaceId, @QueryParam("userId") Integer userId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/admin")
	public Response getAdminOrgs(@PathParam("workspaceId") Integer workspaceId, @QueryParam("userId") Integer userId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/all")
	public Response getAllOrganizations();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/types")
	public Response getOrganizationTypes();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/typemap")
	public Response getOrganizationTypeMap();
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/adminlist/{orgId}")
	public Response getOrgAdminList(@PathParam("orgId") Integer orgId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/org/{orgName}")
	public Response getOrganization(@PathParam("orgName")String orgName);
	

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response postOrganization(Org org);
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/orgtype/add/{orgId}/{orgTypeId}")
	public Response postOrgOrgType(
			@PathParam("orgId") int orgId,
			@PathParam("orgTypeId") int orgTypeId);
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/orgtype/remove/{orgId}/{orgTypeId}")
	public Response removeOrgOrgType(
			@PathParam("orgId") int orgId,
			@PathParam("orgTypeId") int orgTypeId);

}
