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
package edu.mit.ll.em.api.rs.impl;


import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.*;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.rs.Login;
import edu.mit.ll.em.api.rs.LoginResponse;
import edu.mit.ll.em.api.rs.LoginService;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;
import edu.mit.ll.soa.sso.exception.InitializationException;

/**
 * 
 * @author sa23148
 *
 */
public class LoginServiceImpl implements LoginService {
	/** User DAO */
	private UserDAO userDao;
	/** UserSession DAO */
	private UserSessionDAOImpl userSessionDao;
	/** UserOrg DAO */
	private UserOrgDAO userOrgDao;
	/** Org DAO */
	private OrgDAO orgDao;
	/** Workspace DAO */
	private WorkspaceDAO wsDao;
	private SSOUtil ssoUtil = null;
    private RabbitPubSubProducer rabbitProducer;

    private static String CLASS_NAME = LoginServiceImpl.class.getSimpleName();

    public LoginServiceImpl(UserDAO userDao, UserSessionDAOImpl userSessionDao, UserOrgDAO userOrgDao, OrgDAO orgDao, WorkspaceDAO wsDao, RabbitPubSubProducer rabbitProducer) {
        this.userDao = userDao;
        this.userDao = userDao;
        this.userSessionDao = userSessionDao;
        this.userOrgDao = userOrgDao;
        this.orgDao = orgDao;
        this.wsDao = wsDao;
        this.rabbitProducer = rabbitProducer;
    }
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
	 * @return Response
	 * @see LoginResponse
	 */
	public Response putLogin() {
		return makeIllegalOpRequestResponse();
	}

	/**
	 *  Creation of a single Login item.
	 * @return Response
	 * @see LoginResponse
	 */	
	public Response postLogin() {		
		return makeIllegalOpRequestResponse();
	}


	/**
	 *  Read a single Login item.
	 * @param username of Login item to be read.
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
	 * @param username of Login item to be read.
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
     *          if workspaceId <= 0, then the default of 1 is assumed
     *
     * @return Response
     * @see LoginResponse
    */
    public Response postLogin(Login login) {
        Response response = null;
        LoginResponse loginResponse = null;

        String username = (login.getUsername() == null) ? null : login.getUsername().trim().toLowerCase();
        try {
            User u = StringUtil.isBlank(username) ? null : userDao.getUser(username);
            if( u == null ) {
                APILogger.getInstance().w(CLASS_NAME, "Invalid Username: " + username);
                loginResponse = new LoginResponse("Invalid username: " + username + ", Please provide valid username.");
                response = Response.ok(loginResponse).status(Status.BAD_REQUEST).build();
                return response;
            }

            APILogger.getInstance().d(CLASS_NAME, "Login found user: " + u.getUsername() + "(" + u.getUserId() + ")");
            int userId = u.getUserId();

            int workspaceId = login.getWorkspaceId();
            if(workspaceId < 0 || StringUtil.isBlank(wsDao.getWorkspaceName(workspaceId))) {
                APILogger.getInstance().w(CLASS_NAME, "!!!Invalid workspaceId: " + workspaceId + ". Using a default of 1 instead.");
                workspaceId = 1; // Defaulting to 1 if it's not set
            }

            // TODO: Generate/get a proper sessionid? Maybe have caller pass one in if they have
            //      an http session id they can pass
            String sessionId = "API-USER-" + userId + "-" + new Date().getTime();
            int userOrgId = -1, systemRoleId = -1;
            String displayName = u.getFirstname() + " " + u.getLastname();
            List<Org> organizations = orgDao.getUserOrgs(userId, workspaceId);
            UserOrg userOrg = null;
            if(CollectionUtils.isNotEmpty(organizations)) {
                //logout any existing sessions for currently logging in user
                response = cleanupExistingSessionsIfExists(workspaceId, userId);
                if(response != null) {
                    return response;
                }

                // TODO: Needs updated to use the default org, but the rest of the system
                //      needs to adopt the default field usage first
                userOrg = userOrgDao.getUserOrgById(organizations.get(0).getOrgId(), userId, workspaceId);
                // TODO: validate
                int userSessionId = userSessionDao.create(sessionId, userOrg.getUserorgid(), displayName, userId, userOrg.getSystemroleid(), workspaceId);
                if (userSessionId > 0) {
                    Login newLogin = new Login(username, userId, userSessionId, workspaceId);
                    loginResponse = new LoginResponse("ok", Collections.singletonList(newLogin));
                    response = Response.ok(loginResponse).status(Status.OK).build();
                } else {
                    APILogger.getInstance().e(CLASS_NAME, "Unable to create new CurrentUserSession. Failed to login user with username: " + username);
                    loginResponse = new LoginResponse("We are not able to process your request currently. Please try again later.");
                    response = Response.ok(loginResponse).status(Status.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                loginResponse = new LoginResponse("No user organizations found for user, Failed to login user with username: " + username);
                response = Response.ok(loginResponse).status(Status.PRECONDITION_FAILED).build();
            }
        } catch (Exception e) {
            APILogger.getInstance().e(CLASS_NAME, "Exception logging in user with username : " + username, e);
            loginResponse = new LoginResponse("Exception logging in user with username : " + username + " Exception: " + e.getMessage());
            response = Response.ok(loginResponse).status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    private Response cleanupExistingSessionsIfExists(int workspaceId, int userId) {
        CurrentUserSession currentUserSession = userSessionDao.getCurrentUserSession(workspaceId, userId);
        //if current user session exists for currently logging in user
        if(currentUserSession != null) {
            //logout existing sessions of this user
            if(this.userSessionDao.removeUserSession(currentUserSession.getCurrentusersessionid())) {
                try {
                    userSessionDao.updateLoggedOutToNow(currentUserSession.getUsersessionid());
                    this.notifyLogout(currentUserSession.getWorkspaceid(), currentUserSession.getCurrentusersessionid());
                } catch (Exception e) {
                    APILogger.getInstance().e(CLASS_NAME, "Unable to logout active user session with currentusersessionid : " + currentUserSession.getCurrentusersessionid(), e);
                }
            } else {
                APILogger.getInstance().e(CLASS_NAME, "Failed to delete currentUserSession with currentUserSessionId : " + currentUserSession.getCurrentusersessionid());
                LoginResponse loginResponse = new LoginResponse("We are not able to process your request currently. Please try again later.");
                return Response.ok(loginResponse).status(Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return null;
    }

    private void notifyLogout(int workspaceId, long currentUserSessionId) throws IOException {
        String topic = String.format("iweb.NICS.%d.logout", workspaceId);
        this.rabbitProducer.produce(topic, Long.toString(currentUserSessionId));
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
