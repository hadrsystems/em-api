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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.CookieParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.ll.soa.sso.exception.InitializationException;
import edu.mit.ll.em.api.rs.SSOManagementService;
import edu.mit.ll.em.api.rs.SSOToken;
import edu.mit.ll.em.api.rs.SSOUser;
import edu.mit.ll.em.api.rs.User;
import edu.mit.ll.em.api.rs.UserResponse;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;


/**
 * Service endpoint for SSO management calls like creating and modifying users, and
 * other SSO related management 
 *
 */
public class SSOManagementServiceImpl implements SSOManagementService {
	
	/** 
	 * SSOUtil provides access to SSO related calls on the implemented
	 * SSO system (so far this is only OpenAM) 
	 */
	private SSOUtil ssoUtil = null;
	
	/** Local logging instance */
	private APILogger log = APILogger.getInstance();
	
	/** Flag specifying whether or not there was an initialization failure */
	private boolean initFailure = false;
	
	
	/**
	 * Checks to see if the SSOUtil has been properly initialized. If it hasn't, it attempts to
	 * initialize it. If it still fails, a flag is set specifying there was an initialization
	 * failure
	 */
	private void checkInit() {
		if(ssoUtil == null) {
			try {
				initSSOUtils();
			} catch(InitializationException e) {
				initFailure = true;
				log.e("SSOManagementService", e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Initializes the SSOUtil with property files in the path specified by
	 * the ssoToolsPropertyPath system property
	 * 
	 * @return true if SSOUtil was successfully initialized, false otherwise
	 * @throws InitializationException
	 */
	private boolean initSSOUtils() throws InitializationException {
		boolean success = false;
		if(ssoUtil == null) {
			
			String propPath = APIConfig.getInstance().getConfiguration()
					.getString("ssoToolsPropertyPath", null);
			
			log.i("SSOManagementService", "Initializing SSOUtils with property path: " + propPath);
			
			if(propPath == null) {
				// TODO:SSO throw exception or set flag to return error with sso config
				throw new InitializationException("ssoToolsPropertyPath was not set, so can't configure SSOUtil!");
			} else {
				System.setProperty("ssoToolsPropertyPath", propPath);
				System.setProperty("openamPropertiesPath", propPath);
			}
						
			ssoUtil = new SSOUtil();
			success = true;			
		}
		
		return success;
	}
	

	/**
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#getUsers(String)
	 */
	//@Override
	public Response getUsers(String realm) {
		
		checkInit();
		//ssoUtil.getUsers TODO:SSO doesn't exist on SSOUtil, but does on OpenAmUtils
		
		return Response.ok("Not implemented").status(Status.NOT_IMPLEMENTED).build();
	}

	
	/**
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#getUser(String, String)
	 */
	//@Override
	public Response getUser(String realm, String email) {
				
		
		return Response.ok("Not implemented").status(Status.NOT_IMPLEMENTED).build();
	}	
	
	//@Override
	public Response getUserAttributes(String email, Cookie cookie) {
		Response response = null;
		
		if(cookie == null) {
			// Shouldn't reach this with cookie protection
			return Response.ok("Invalid cookie").status(Status.BAD_REQUEST).build();
		}
		
		String token = cookie.getValue();
		if(token == null || token.isEmpty()) {
			return Response.ok("Unable to read token from cookie").status(Status.BAD_REQUEST).build();
		}
		
		checkInit();
		
		Map map = ssoUtil.getUserAttributes(token);
		StringBuilder sb = new StringBuilder();
		if(map != null) {
			Iterator iter = map.keySet().iterator();
			while(iter.hasNext()) {
				String key = (String) iter.next();
				HashSet value = (HashSet)map.get(key);
				if(value != null) {
					sb.append(key+":"+ value.iterator().next());
				}
				
				//sb.append(key +":"+ map.get(key) + " | ");
				
				
				APILogger.getInstance().i("SSOManagementService", "User Attribute: " + map.get(key));
			}
			
			response = Response.ok(sb.toString()).status(Status.OK).build();
		} else {
			response = Response.ok("Failed to get user attributes: null").status(Status.EXPECTATION_FAILED).build();
		}
		
		return response;
	}
	
	/**
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#putUser(User, String)
	 */
	//@Override
	public Response putUser(SSOUser user) {
		
		if(user == null || user.getEmail() == null) {
			return Response.ok("Invalid user in request").status(Status.BAD_REQUEST).build();
		}
		
		log.i("SSOManagementService", "Got put user: " + user.toString());
				
		/*if(true) {
			return Response.ok("Not implemented").status(Status.NOT_IMPLEMENTED).build();
		}*/
		
		checkInit();
		
		// TODO:SSO need to get from cookie/header request? Otherwise will need to have it included
		// as an endpoint parameter or in the user object
		String tokenId = null; 
						
		// Attribute updates?
		Map currentAttributes = ssoUtil.getAttributes();
		if(currentAttributes == null) {
			// either no user, or authentication failure
		} else {
			
		}
		
		Map mergedAttributes = new HashMap();
		
		// TODO:SSO look into modifying email, password, names, etc, via the sdk... only
		//		thing I see is setting an attribute. Do email and password, etc, count as
		//		an attribute?
		
		// TODO:SSO Allowing email change? probably not?
		
		// First name change
		
		// Last name change
		
		// password change
		
		
		
		Map newAttributes = user.getAttributes();
		
		Iterator iter = newAttributes.keySet().iterator();
		Set newVal = new HashSet();
		while(iter.hasNext()) {
			String key = (String) iter.next();
			String val = (String) currentAttributes.get(key);
			
			if(val != null) {
				//mergedAttributes.put(key, (String)newAttributes.get(key));
				//ssoUtil.setAttribute(key, val);
				newVal = new HashSet();
				newVal.add(val);
				mergedAttributes.put(key, newVal);
			} else {
				// TODO:SSO
				// adding new attribute? Not sure this can necessarily be formally allowed, since the repos
				// need to index attributes
			}
		}
		
		ssoUtil.setAttributes(mergedAttributes);
		
		// TODO:SSO openam-tools only has a setAttribute() for a single attribute... should modify
		// 		to allow for setting a map of attributes
		//ssoUtil.setAttribute(key, val);
		
		
		return Response.ok("TESTING").status(Status.NOT_IMPLEMENTED).build();
	}

	
	/** 
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#postUser(edu.mit.ll.em.api.rs.User, java.lang.String)
	 */
	//@Override
	public Response postUser(SSOUser user) {
		Response response = null;
		String realm = null;
		
		if(user == null) {
			return Response.ok("No User in request").status(Status.BAD_REQUEST).build();
		}
		
		if(user.getRealm() == null || user.getRealm().isEmpty()) {
			realm = "/"; // Set to default realm
		} else {
			realm = user.getRealm();
		}
						
		checkInit();
		
		if(initFailure) {
			return Response.ok("SSOTools initialization failure")
					.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		// Login as usercreator
		//String token = ssoUtil.login("usercreator@nics.ll.mit.edu", "TestPassword!", "/");
		boolean isAdminLogedIn = ssoUtil.loginAsAdmin();
		
		//if(token == null) {
		if(!isAdminLogedIn) {
			log.i("SSOManagementService", "Failed to login, so can't successfully create user");
			return Response.ok("Failed to login with Administrative account, can't create identity")
					.status(Status.PRECONDITION_FAILED).build();
		}
		
		// TODO:SSO if realm specified is different than one that's set in openam-tools, then
		// we'd need to switch to it here
		//boolean switched = ssoUtil.switchOrg(realm);
		
		String created = ssoUtil.createUser(user.getEmail(), 
			user.getPassword(), user.getFirstName(), 
			user.getLastName(), false);
		
		JSONObject createdResponse = null;
		try {
			createdResponse = new JSONObject(created);
			String status = createdResponse.optString("status", null);
			String message = createdResponse.optString("message", null);
			
			if(status == null) {
				return Response.ok("Unknown status of identity creation for user: " + user.getEmail()).build();
			}else if(status.equals("success")) {
				return Response.ok("SSO user created successfully").status(Status.CREATED).build();
			} else {		
				return Response.ok("Failed to create SSO user: " + message).build();
			}
		} catch(JSONException e) {
			// Assume failure
			return Response.ok("failed to create SSO user").build();
		}
	}

	
	/**
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#enableUser(SSOToken)
	 *
	@Override
	public Response enableUser(SSOToken token) {
		String tokenid = token.getToken();
		return null;
	}*/
	
	/**
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#enableUser(String, String, String)
	 */
	@Override
	public Response enableUser(String email, String flag, int userOrgWorkspaceId, String username) {
		
		UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
		UserResponse userResponse = new UserResponse();
		
		int systemRoleId = userOrgDao.getSystemRoleId(username, userOrgWorkspaceId);
		
		if(((systemRoleId == SADisplayConstants.ADMIN_ROLE_ID ||
				systemRoleId == SADisplayConstants.SUPER_ROLE_ID)) ||
				userOrgDao.isUserRole(username, SADisplayConstants.SUPER_ROLE_ID)){
		
			if(email == null || email.isEmpty() 
					|| flag == null || flag.isEmpty()) {
				return Response.ok("Invalid parameters").status(Status.BAD_REQUEST).build();
			}
			
			String iNetUserStatus = null;
			
			if(flag.equals("enable")) {
				iNetUserStatus = "Active";
			} else if(flag.equals("disable")) {
				iNetUserStatus = "Inactive";
			} else {		
				return Response.ok("Must specify 'enable' or 'disable'").status(Status.BAD_REQUEST).build();
			}
			
			checkInit();
			
			// loginAsAdmin assumes user and pass are encrypted in sso-tools.properties file
			if(ssoUtil.loginAsAdmin()) {
				System.out.println("\n!!!Successfully logged in as admin");
			} else {
				System.out.println("\n!!!FAILED to log in as admin");
			}
			
			String toggleResult = ssoUtil.toggleUserStatus(email, (iNetUserStatus.equals("Active") ? true : false));
					
			//Map userAttributes = ssoUtil.setAttribute("iNetUserStatus", iNetUserStatus);
			
			/*
			boolean success = false;
			if(userAttributes == null || userAttributes.isEmpty()) {
				// error/no attributes changed
			} else {
				// TODO:SSO verify inetuserstatus is now set to iNetUserStatus as specified
				
				Set val = (Set)userAttributes.get("inetUserStatus");
				result = (String)val.iterator().next();
				System.out.println("Got iNetUserStatus returned from set attribute call: " + result);
				if(result.equals(iNetUserStatus)) {
					// success
					success = true;
				} else {
					// failed
				}
			}
			*/
			ssoUtil.destroyToken(ssoUtil.getTokenIfExists());
			userResponse.setMessage(toggleResult);
			
			return Response.ok(userResponse).status((toggleResult.contains("Success")) ? Status.OK : Status.EXPECTATION_FAILED).build();
		}
		return Response.ok(userResponse).status(Status.BAD_REQUEST).build();
	}
	
		
	/**
	 * Utility method for setting attribute
	 * 
	 * TODO:SSO need to implement another call that specifies a token
	 * 		so the system knows which user we want to set an attribute on?
	 * 
	 * @param key The name of the attribute to add
	 * @param value the value of the specified attribute
	 * 
	 * @return true if successful, false if auth error or failure
	 */
	private boolean setUserAttribute(String key, String value) {
		boolean success = false;
		
		try {
			// TODO:SSO what user is this using if it's just setting a key? Assuming cached user?		
			Map resultMap = ssoUtil.setAttribute(key, value);
			if(resultMap != null && resultMap.containsKey(key)
					&& resultMap.get(key).equals(value)) {
				success = true;
			}
		} catch(Exception e) {
			
		}
		
		return success;
	}



	/**
	 * @see edu.mit.ll.em.api.rs.SSOManagementService#deleteUser(String)
	 */
	//@Override
	public Response deleteUser(String email) {
		
		return Response.ok("Not implemented").status(Status.NOT_IMPLEMENTED).build();
	}
	

}
