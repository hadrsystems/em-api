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

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.exception.DuplicateCollabRoomException;
import edu.mit.ll.nics.common.entity.Incident;

@Path("/incidents/{workspaceId}")
public interface IncidentService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncidents(
			@PathParam("workspaceId") Integer workspaceId,
			@QueryParam("accessibleByUserId") Integer userId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/getincidenttree")
	public Response getIncidentsTree(
			@PathParam("workspaceId") Integer workspaceId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/incidentorgs")
	public Response getIncidentOrgs(
			@PathParam("workspaceId") Integer workspaceId);
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/archived/{orgId}")
	public Response getArchivedIncidents(
			@PathParam("workspaceId") Integer workspaceId,
			@PathParam("orgId") Integer orgId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/active/{orgId}")
	public Response getActiveIncidents(
			@PathParam("workspaceId") Integer workspaceId,
			@PathParam("orgId") Integer orgId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/find")
	public Response findArchivedIncidents(
			@PathParam("workspaceId") Integer workspaceId, 
			@QueryParam("orgPrefix") String orgPrefix, 
			@QueryParam("name") String name);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/update")
	public Response updateIncident(
			@PathParam("workspaceId") Integer workspaceId,
			Incident incident);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/archive/{incidentId}")
	public Response archiveIncident(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("incidentId") int incidentId,
			@HeaderParam("CUSTOM-UID") String user);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/activate/{incidentId}")
	public Response activateIncident(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("incidentId") int incidentId,
			@HeaderParam("CUSTOM-UID") String user);
	
	/*@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteIncidents(@PathParam("workspaceId") Integer workspaceId);*/

	/*@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putIncidents(
			@PathParam("workspaceId") Integer workspaceId,
			Collection<Incident> incidents);*/

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postIncident(
			@PathParam("workspaceId") Integer workspaceId,
			@QueryParam("orgId") Integer orgId,
			@QueryParam("userId") Integer userId,
			Incident incident)
			throws DataAccessException, DuplicateCollabRoomException, Exception;
	
	@GET
	@Path(value = "/getincidentbyname")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncident(
			@PathParam("workspaceId") Integer workspaceId,
			@QueryParam("incidentName") String incidentName);

	/*@GET
	@Path(value = "/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncidentCount(@PathParam("workspaceId") Integer workspaceId);

	@GET
	@Path(value = "/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchIncidentResources();	
	
	@GET
	@Path(value = "/{incidentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncident(
			@PathParam("workspaceId") Integer workspaceId,
			@PathParam("incidentId") int incidentId);
	
	@GET
	@Path(value = "/{incidentId}/notification")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncidentNotificationForm(
			@PathParam("workspaceId") Integer workspaceId,
			@PathParam("incidentId") int incidentId);

	@DELETE
	@Path(value = "/{incidentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteIncident(
			@PathParam("workspaceId") Integer workspaceId,
			@PathParam("incidentId") int incidentId);

	@PUT
	@Path(value = "/{incidentId}")
    @Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response putIncident(
			@PathParam("workspaceId") Integer workspaceId,
			@PathParam("incidentId") int incidentId, Incident incident);*/
}

