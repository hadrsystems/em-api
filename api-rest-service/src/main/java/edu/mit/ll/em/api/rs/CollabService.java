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
package edu.mit.ll.em.api.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.nics.common.entity.CollabRoom;

@Path("/collabroom/{incidentId}")
public interface CollabService {
	
	@GET
	@Path("/subscribe/{collabroomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateSubsription(
			@PathParam("collabroomId") int collabroomId,
			@HeaderParam("CUSTOM-uid") String username);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollabRoom(
			@PathParam("incidentId") int incidentId,
			@DefaultValue("-1") @QueryParam("userId") Integer userId,
			@HeaderParam("CUSTOM-uid") String username);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCollabRoom(
			@QueryParam("userOrgId") int userOrgId,
			@PathParam("incidentId") int incidentId, 
			CollabRoom collabroom,
			@HeaderParam("CUSTOM-uid") String username);

	@GET
	@Path("/{collabroomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollabRoomPresence(
			@PathParam("incidentId") int incidentId,
			@PathParam("collabroomId") int collabroomId);
	
	@POST
	@Path("/{collabroomId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCollabRoomPresence(
			@PathParam("incidentId") int incidentId, 
			@PathParam("collabroomId") int collabroomId,
			CollabPresenceStatus state);
	
	@DELETE
	@Path(value = "/unsecure/{collabroomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response unsecureRoom(
			@PathParam("collabroomId") long collabroomId, 
			@QueryParam("userId") long userId,
			@HeaderParam("CUSTOM-uid") String username);
	
	public Response postCollabRoom(
			@PathParam("incidentId") int incidentId, 
			CollabRoom collabroom);
}