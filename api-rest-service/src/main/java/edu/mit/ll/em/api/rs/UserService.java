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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.em.api.rs.RegisterUser;
import edu.mit.ll.em.api.rs.User;

@Path("/users/{workspaceId}")
public interface UserService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(@PathParam("workspaceId") int workspaceId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/systemroles")
	public Response getSystemRoles();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/active")
	public Response getActiveUsers(@PathParam("workspaceId") int workspaceId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/admin/{userOrgId}")
	public Response isAdmin(@PathParam("userOrgId") int userOrgId);
	
	@GET
	@Path(value = "/username/{username}/userOrgId/{userOrgId}/orgId/{orgId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserProfile(
			@PathParam("username")String username, 
			@PathParam("userOrgId")int userOrgId, 
			@PathParam("workspaceId")int workspaceId, 
			@PathParam("orgId") int orgId,
			@QueryParam("requestingUserOrgId") int rUserOrgId,
			@HeaderParam("CUSTOM-uid") String requestingUser);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/updateprofile")
	public Response postUserProfile(User user, @HeaderParam("CUSTOM-uid") String requestingUser, @QueryParam("requestingUserOrgId") int rUserOrgId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public Response findUser(
			@QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName,
			@QueryParam("exact") boolean exact);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/enabled/{orgId}")
	public Response getEnabledUsers(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("orgId") int orgId);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/disabled/{orgId}")
	public Response getDisabledUsers(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("orgId") int orgId);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/enable/{userOrgWorkspaceId}/userid/{userId}")
	public Response setUserEnabled(
			@PathParam("userOrgWorkspaceId") int userOrgWorkspaceId, 
			@PathParam("userId") int userId,
			@PathParam("workspaceId") int workspaceId,
			@QueryParam("enabled") boolean enabled,
			@HeaderParam("CUSTOM-uid") String requestingUser);


	/*@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUsers();

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putUsers(Collection<User> users);*/
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUser(@PathParam("workspaceId") int workspaceId, RegisterUser user);

	/*@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUser(User user);*/

	/*@GET
	@Path(value = "/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserCount(@PathParam("workspaceId") int workspaceId);
	
	@GET
	@Path(value = "/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchUserResources(@QueryParam("") UserSearchParams searchParams);*/
	
	@GET
	@Path(value = "/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("userId") int userId);

	/*@DELETE
	@Path(value = "/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("userId") int userId);

	@PUT
	@Path(value = "/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response putUser(@PathParam("userId") int userId, User user);

	@POST
	@Path(value = "/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUser(@PathParam("userId") int userId);*/
	
	@GET
	@Path(value = "/userOrgs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserOrgs(@PathParam("workspaceId") int workspaceId, 
			@QueryParam("userName") String username,
			@HeaderParam("CUSTOM-uid") String requestingUser);
	
	@GET
	@Path(value = "/systemStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoginStatus(@PathParam("workspaceId") int workspaceId, @QueryParam("userName") String username);
	
	@POST
	@Path(value= "/createsession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUserSession(@QueryParam("userId") long userId, @QueryParam("displayName") String displayName,
			@QueryParam("userOrgId") int userOrgId, @QueryParam("systemRoleId") int systemRoleId, 
			@PathParam("workspaceId") int workspaceId, @QueryParam("sessionId") String sessionId,
			@HeaderParam("CUSTOM-uid") String requestingUser);
	
	@POST
	@Path(value= "/updatesession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserSession(@QueryParam("userId") long userId, @QueryParam("displayName") String displayName,
			@QueryParam("userOrgId") int userOrgId, @QueryParam("systemRoleId") int systemRoleId, 
			@PathParam("workspaceId") int workspaceId, @QueryParam("sessionId") String sessionId,
			@HeaderParam("CUSTOM-uid") String requestingUser);
	
	@POST
	@Path(value= "/removesession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeUserSession(@PathParam("workspaceId") int workspaceId, @QueryParam("currentUserSessionId") long currentUserSessionId);
	
	@POST
	@Path(value= "/userorg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUserToOrg(Collection<Integer> userIds,  @QueryParam("orgId") int orgId, @PathParam("workspaceId") int workspaceId);
	
	@GET
	@Path(value = "/contactinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsersContactInfo(@PathParam("workspaceId") int workspaceId, 
			@QueryParam("userName") String userName);
	
	@POST
	@Path(value = "/updatecontactinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addContactInfo(@PathParam("workspaceId") int workspaceId, 
			@QueryParam("userName") String userName,
			@QueryParam("contactTypeId") int contactTypeId,
			@QueryParam("value") String value,
			@HeaderParam("CUSTOM-uid") String requestingUser);
	
	@DELETE
	@Path(value = "/deletecontactinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteContactInfo(@PathParam("workspaceId") int workspaceId, 
			@QueryParam("userName") String userName,
			@QueryParam("contactTypeId") int contactTypeId,
			@QueryParam("value") String value,
			@HeaderParam("CUSTOM-uid") String requestingUser);
	
}
