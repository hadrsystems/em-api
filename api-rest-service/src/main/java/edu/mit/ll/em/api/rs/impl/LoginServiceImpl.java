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


import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.rs.Login;
import edu.mit.ll.em.api.rs.LoginResponse;
import edu.mit.ll.em.api.rs.LoginService;
import edu.mit.ll.em.api.rs.SSOUser;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.WorkspaceDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;
import edu.mit.ll.nics.common.ws.client.JSONRequest;
import edu.mit.ll.soa.sso.exception.InitializationException;

/**
 * 
 * @author sa23148
 *
 */
public class LoginServiceImpl implements LoginService {

	/** User DAO */
	private static final UserDAOImpl userDao = new UserDAOImpl();
	
	/** UserSession DAO */
	private static final UserSessionDAOImpl userSessDao = new UserSessionDAOImpl();
	
	/** UserOrg DAO */
	private static final UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
	
	/** Org DAO */
	private static final OrgDAOImpl orgDao = new OrgDAOImpl();
	
	/** Workspace DAO */
	private static final WorkspaceDAOImpl wsDao = new WorkspaceDAOImpl();
	
	private SSOUtil ssoUtil = null;
	
	/**
	 * Read and return all Login items.
	 * @return Response
	 * @see LoginResponse
	 */
	public Response getLogin() {
		return makeIllegalOpRequestResponse();
	}

	/**
	 * Delete all Login items.
	 * This is an unsupported operation.
	 * @return Response
	 * @see LoginResponse
	 */
	public Response deleteLogin() {
		return makeIllegalOpRequestResponse();
	}

	/**
	 * Bulk creation of Login items.
	 * @param A collection of Login items to be created.
	 * @return Response
	 * @see LoginResponse
	 */
	public Response putLogin() {
		return makeIllegalOpRequestResponse();
	}

	/**
	 *  Creation of a single Login item.
	 * @param Login to be created.
	 * @return Response
	 * @see LoginResponse
	 */	
	public Response postLogin() {		
		return makeIllegalOpRequestResponse();
	}


	/**
	 *  Read a single Login item.
	 * @param ID of Login item to be read.
	 * @return Response
	 * @see LoginResponse
	 */	
	public Response getLogin(String username) {
		return makeIllegalOpRequestResponse();
	}
	
	public Response delete(String username, Cookie cookie){
		return deleteLogin(username,cookie);
	}
	
	/**
	 * Performs logout actions for specified user
	 * 
	 * @param username Username of user to log out
	 * 
	 * @return Response
	 * @see LoginResponse
	 */	
	public Response deleteLogin(String username, Cookie cookie) {
		String token = cookie.getValue();
		
		Response response = null;
		LoginResponse loginResponse = new LoginResponse();
		
		if (username != null && !username.isEmpty()) {			
			try {				
				boolean ssoLogoutStatus = false;
				if(token != null && !token.isEmpty()) {
					ssoLogoutStatus = doSsoLogout(token);
				}
				if(ssoLogoutStatus){
					loginResponse.setMessage("OK");
					loginResponse.setCount(0);
					response = Response.ok(loginResponse).status(Status.OK).build();
				}else{
					loginResponse.setMessage("Error logging out") ;
					response = Response.ok(loginResponse).status(Status.NOT_FOUND).build();	
				}
			} catch (DataAccessException e) {
				loginResponse.setMessage("Data access exception attempting to log User out: " + e.getMessage()) ;
				response = Response.ok(loginResponse).status(Status.NOT_FOUND).build();			
			} catch(Exception e) {
				loginResponse.setMessage("Unhandled exception attempting to log User out: " + e.getMessage()) ;
				response = Response.ok(loginResponse).status(Status.NOT_FOUND).build();			
			}
		}

		return response;
	}
	

	/**
	 *  Update a single Login item.
	 * @param ID of Login item to be read.
	 * @return Response
	 * @see LoginResponse
	 */	
	public Response putLogin(String username) {
		return makeIllegalOpRequestResponse();		
	}
	

	/**
	 *  Post a single Login item.   
	 *  
	 * @param login Login object expected to be populated with valid username, and workspaceId,
	 * 			if workspaceId <= 0, then the default of 1 is assumed
	 * 
	 * @return Response
	 * @see LoginResponse
	 */	
	public Response postLogin(Login login) {
		Response response = null;
		LoginResponse loginResponse = new LoginResponse();
		
		String username = login.getUsername().toLowerCase();
		
		int wsId = login.getWorkspaceId();
		String wsName = wsDao.getWorkspaceName(wsId);
		boolean isWsIdValid = (wsName != null && !wsName.isEmpty()) ? true : false;
		if(!isWsIdValid) {
			APILogger.getInstance().w("LoginServiceImpl", "!!!Invalid workspaceId: " + wsId + ". "
					+ "Using a default of 1 instead.");
			
			wsId = 1; // Defaulting to 1 if it's not set
		}
		
		Login newLogin = new Login();
				
		if (username != null && !username.isEmpty()) {			
			try {
				newLogin.setUsername(username);
								
				User u = userDao.getUser(username);
				if(u == null) {
					loginResponse.setMessage("Could not find username: " + username);
					response = Response.ok(loginResponse).status(Status.NO_CONTENT).build();
					return response;
				}
				
				APILogger.getInstance().d("LoginServiceImpl", "Login found user: " + u.getUsername() + "(" +
						u.getUserId() + ")");
				
				int userId = u.getUserId();
				if(userId <= 0) {
					loginResponse.setMessage("Invalid userId for username " + username + ": " + userId);
					response = Response.ok(loginResponse).status(Status.NO_CONTENT).build();
					return response;
				}
				
				if(!userSessDao.hasCurrentUserSession(wsId, userId)) {
					// TODO: Generate/get a proper sessionid? Maybe have caller pass one in if they have
					//		an http session id they can pass
					String sessionid = "API-USER-" + userId + "-" + new Date().getTime();
					int userorgid = -1;
					String displayname = u.getFirstname() + " " + u.getLastname();
					int systemroleId = -1;
					int orgid = -1;
					List<Org> orgs = orgDao.getUserOrgs(userId, wsId);
					if(orgs != null && !orgs.isEmpty()) {
						// TODO: Needs updated to use the default org, but the rest of the system
						//		 needs to adopt the default field usage first
						orgid = orgs.get(0).getOrgId();
					}
					
					UserOrg userOrg = null;
					if(orgid != -1) {
						userOrg = userOrgDao.getUserOrgById(orgid, userId, wsId);
						// TODO: validate
						userorgid = userOrg.getUserorgid();
						systemroleId = userOrg.getSystemroleid();
					}
					
					// create() creates both current and user sessions
					int userSessionId = userSessDao.create(sessionid, userorgid, displayname, 
							userId, systemroleId, wsId);					
					
					newLogin.setUserId(userId);
					newLogin.setWorkspaceId(wsId);
					newLogin.setUserSessionId(userSessionId);
				} else { // has currentusersession
					// Reuse it
					CurrentUserSession curUserSession = userSessDao.getCurrentUserSession(wsId, userId);
					if(curUserSession != null) {
						newLogin.setUserId(userId);
						newLogin.setWorkspaceId(curUserSession.getWorkspaceid());
						newLogin.setUserSessionId(curUserSession.getUsersessionid());
						// TODO: update any timestamps on the currentusersessions?
						try {
							userSessDao.updateLastSeen(userId);
						} catch(Exception e) {
							APILogger.getInstance().e("LoginService:postLogin", "Unhandled exception attempting to "
									+ "update Last Seen on currentusersession for userId: " + userId);
						}
					}
				}
				
				
				if (newLogin.getUserSessionId() <= 0) {
					loginResponse.setMessage("Login failed for user: " + username);
					response = Response.ok(loginResponse).status(Status.NO_CONTENT).build();
					return response;	
				} else {
					loginResponse.getLogins().add(newLogin);
					loginResponse.setMessage("ok");
					loginResponse.setCount(1);
					response = Response.ok(loginResponse).status(Status.OK).build();
				}
			} catch (Exception e) {
				loginResponse.setMessage("Unhandled exception logging in: " + e.getMessage());
				response = Response.ok(loginResponse).status(Status.PRECONDITION_FAILED).build();
			}
		}

		return response;
	}
	

	/**
	 *  Return the number of Login items stored. 
	 * @return Response
	 * @see LoginResponse
	 */		
	public Response getLoginCount() {
		return makeUnsupportedOpRequestResponse();
	}
	

	public Response searchLoginResources() {
		return makeUnsupportedOpRequestResponse();
	}
	
	
	/**
	 * 
	 * @param token
	 * @return
	 */
	private boolean doSsoLogout(String token) { // TODO: May add or replace token to logout with
		
		APILogger.getInstance().i("LoginServiceImpl:doSsoLogout", "Logging out user's SSO Token: "
				+ token);
		
		boolean status = false;
		
		// TODO: implement SSO Logout
		//SSOUtil ssoUtil = new SSOUtil();
		
		// TODO: this just logs out who logged in, which would have given a new token...
		//		need to maybe login as admin and add a logoutUserByToken() or something similar
		//      which is not in openam-utils. Perhapse it can be added, otherwise we can hit the
		//		rest service directly, given the token
		//ssoUtil.logout(); 
		
		/*
		  Legacy rest endpoint example:
		  curl --request POST --data "subjectid=AQIC5w...*AAJTSQACMDE.*"
			https://openam.example.com:8443/openam/identity/logout
			
		  Newer json api example:
		  	curl
			 --request POST
			 --header "iplanetDirectoryPro: AQIC5wM2...U3MTE4NA..*"
			 "https://openam.example.com:8443/openam/json/sessions/?_action=logout"
		 */
		
		
		try {
			
			//====================================================================================
			// This block taken from SSOManagementServiceImpl, to init ssoutil
			if(ssoUtil == null) {
				
				String propPath = APIConfig.getInstance().getConfiguration()
						.getString("ssoToolsPropertyPath", null);
				
				if(propPath == null) {
					// TODO:SSO throw exception or set flag to return error with sso config
					throw new InitializationException("ssoToolsPropertyPath was not set, so can't configure SSOUtil!");
				} else {
					System.setProperty("ssoToolsPropertyPath", propPath);
					System.setProperty("openamPropertiesPath", propPath);
				}
							
				ssoUtil = new SSOUtil();						
			}
			// End BLOCK
			//====================================================================================
			
			if(ssoUtil != null) {
				ssoUtil.destroyToken(token);
				status = true;
			} else {
				APILogger.getInstance().i("LoginServiceImpl", 
						"SSOUtil not configured properly, can't call destroyToken on token: " + token);
			}
			
		} catch(Exception e) {
			APILogger.getInstance().i("LoginServiceImpl", 
					"Unhandled exception attempting to destroy token: " + e.getMessage());
			e.printStackTrace();
		}
		
		
		return status;
	}


	private Response makeIllegalOpRequestResponse() {
		LoginResponse mdtrackResponse = new LoginResponse();
		mdtrackResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Illegal operation requested").
				status(Status.FORBIDDEN).build();
		return response;
	}	
	
	private Response makeUnsupportedOpRequestResponse() {
		LoginResponse mdtrackResponse = new LoginResponse();
		mdtrackResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Unsupported operation requested").
				status(Status.NOT_IMPLEMENTED).build();
		return response;
	}
}
