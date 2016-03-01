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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.em.api.rs.SSOUser;

@Path("/sso")
public interface SSOManagementService {

	/**
	 * Get listing of users belonging to realm
	 * 
	 * TODO:SSO Should probably paginate/limit results? Also, this shouldn't be allowed
	 * 		to just anyone who hits the service with an authenticated token. It should probably
	 * 		require that they have admin privs?
	 * 
	 * @param realm
	 * @return
	 */
	/*@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(@QueryParam("realm") String realm);
	*/
	
	/**
	 * Get User information for the specified user
	 * 
	 * @param realm
	 * @param email
	 * @return
	 */
	/*@GET
	@Path("/users/{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@QueryParam("realm") String realm, @PathParam("email") String email);
	*/
	@GET
	@Path("/users/{email}/attributes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserAttributes(@PathParam("email") String email, @CookieParam("iPlanetDirectoryPro") Cookie cookie);
	
	/**
	 * Create/register a user in the SSO system
	 *  
	 * @param user User to register, with first, last, email required
	 * @param realm SSO realm to register the user with
	 * @return
	 */
	/*@POST
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postUser(SSOUser user);*/
	
	
	/**
	 * Update a user
	 * 
	 * @param user User to modify
	 * @param realm SSO Realm the user is registered with
	 * @return
	 */
	/*@PUT
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putUser(SSOUser user);*/
	
	// @PATCH partial update to existing user
	
	
	/**
	 * Delete an SSO account
	 * <br/>
	 * TODO restrict to admin user capability
	 * 
	 * @param email
	 * @return
	 */
	/*@DELETE
	@Path("/users/{email}")	
	public Response deleteUser(@PathParam("email") String email);*/
	
	// TODO:SSO Need to restrict this to NICS admin users somehow... via openam policy?	
	@POST
	@Path("/users/{flag}/{email}/{userorgworkspaceid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enableUser(@PathParam("email") String email, @PathParam("flag") String flag,
			@PathParam("userorgworkspaceid") int userorgWorkspaceId, @HeaderParam("CUSTOM-uid") String username);
	
	
	// TODO:SSO more methods we'll want to implement
	
	// public Response getAttributes(String token);
	
	// public Response isTokenValid(String token);
}
