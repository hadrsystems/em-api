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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.ll.em.api.dataaccess.EntityCacheMgr;
import edu.mit.ll.em.api.dataaccess.ICSDatastoreException;
import edu.mit.ll.em.api.dataaccess.MDTrackDAO;
import edu.mit.ll.em.api.rs.MDTOptionalParms;
import edu.mit.ll.em.api.rs.MDTrack;
import edu.mit.ll.em.api.rs.MDTrackService;
import edu.mit.ll.em.api.rs.MDTrackServiceResponse;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;
import edu.mit.ll.soa.sso.exception.InitializationException;

/**
 * 
 * @AUTHOR sa23148
 *
 */
public class MDTrackServiceImpl implements MDTrackService {

	private static final String CNAME = MDTrackServiceImpl.class.getName();
	
	/**
	 * Read and return all MDTrack items.
	 * @return Response
	 * @see MDTrackResponse
	 */
	public Response getMDTracks(MDTOptionalParms optionalParms) {
		//if(true) {
			return makeUnsupportedOpRequestResponse();
		//}
		/*
		Response response = null;
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();

        Set<MDTrack> mdtracks = null;
        try {
			mdtracks = MDTrackDAO.getInstance().getAllMDTracks(optionalParms);
		//} catch (ICSDatastoreException e) {
        } catch (Exception e) {
			PAPILogger.getInstance().e(CNAME, e.getMessage());
			mdtrackResponse.setMessage("Failure. " + e.getMessage());
			response = Response.ok(mdtrackResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		}
        
		for (MDTrack mdtrack : mdtracks) {
			mdtrackResponse.getMDTracks().add(mdtrack);
		}
		mdtrackResponse.setMessage("ok");
		mdtrackResponse.setCount(mdtracks.size());
		response = Response.ok(mdtrackResponse).status(Status.OK).build();

		return response;*/
	}

	/**
	 * Delete all MDTrack items.
	 * This is an unsupported operation.
	 *  Response
	 *  MDTrackResponse
	 */
	public Response deleteMDTracks() {
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 * Bulk update of MDTrack items.
	 *  
	 *  <p>Deprecated: Never update MDTs, plus will no longer be in DB to update moving forward</p> 
	 *  
	 *  A collection of MDTrack items to be updated
	 *  Response
	 *  MDTrackResponse
	 */
	@Deprecated
	public Response putMDTracks(Collection<MDTrack> mdtracks) {

		//if(true) {
			return makeUnsupportedOpRequestResponse();
		//}
		/*
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();
		Response response = null;
		int errorCount = 0;
		for (MDTrack mdtrack : mdtracks) {
			try {
				MDTrack newMDTrack = MDTrackDAO.getInstance().createMDTrack(mdtrack);
				mdtrackResponse.getMDTracks().add(newMDTrack);
			} catch (ICSDatastoreException e) {
				PAPILogger.getInstance().e(CNAME, e.getMessage());
				++errorCount;
			}			
		}
		
		if (errorCount == 0) {
			mdtrackResponse.setMessage("ok");
			mdtrackResponse.setCount(mdtracks.size());
			response = Response.ok(mdtrackResponse).status(Status.OK).build();			
		} else {
			mdtrackResponse.setMessage("Failures. " + errorCount + " out of " + mdtracks.size() + " were not created.");
			mdtrackResponse.setCount(mdtracks.size() - errorCount);
			response = Response.ok(mdtrackResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return response;*/
	}

	
	/**
	 *  Creation of a single MDTrack item.
	 *  
	 *  Response
	 *  MDTrackResponse
	 */	
	public Response postMDTrack(Cookie cookie, MDTrack mdtrack) {
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();
		Response response = null;
		boolean verified = false;
		if(mdtrack != null) {
			String verifyMsg = null;
			try {
				verifyMsg = verifySender(cookie, mdtrack.getUserId());
				APILogger.getInstance().i("MDTrackService", "\nGot verifyMsg: " + verifyMsg + "\n");
				if(verifyMsg != null) {
					JSONObject verifyJson = new JSONObject(verifyMsg);
					if(verifyJson.getString("status").equals("success")) {
						APILogger.getInstance().i("MDTrackService", "Got status SUCCESS: " + verifyJson.getString("status"));
						verified = true;
						APILogger.getInstance().i("MDTrackService", "Token identity matched NICS identity!");
					} else if(verifyJson.getString("status").equals("fail")){
						APILogger.getInstance().i("MDTrackService", "Got status FAIL: " + verifyJson.getString("status"));
						mdtrackResponse.setMessage(verifyJson.getString("message"));
						mdtrackResponse.setCount(0);
						response = Response.ok(mdtrackResponse).status(Status.EXPECTATION_FAILED).build();
					} else {
						mdtrackResponse.setMessage(verifyJson.getString("message"));
						response = Response.ok(mdtrackResponse).status(Status.EXPECTATION_FAILED).build();
						APILogger.getInstance().i("MDTrackService", "Got unknown status: " + verifyJson.getString("status")); 
					}
				}
			} catch (JSONException e) {
				mdtrackResponse.setMessage("Error processing Identity: " + e.getMessage());
				mdtrackResponse.setCount(0);
				response = Response.ok(mdtrackResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
			if(!verified) {
				return response;
			}
		} else {
			mdtrackResponse.setMessage("Invalid MDTrack sent");
			mdtrackResponse.setCount(0);
			response = Response.ok(mdtrackResponse).status(Status.EXPECTATION_FAILED).build();
			return response;
		}
		
		MDTrack newMDTrack = null;
		try {
			newMDTrack = MDTrackDAO.getInstance().createMDTrack(mdtrack);
			mdtrackResponse.getMDTracks().add(newMDTrack);
			mdtrackResponse.setMessage("ok");
			mdtrackResponse.setCount(1);
			response = Response.ok(mdtrackResponse).status(Status.OK).build();
		} catch (ICSDatastoreException e) {
			APILogger.getInstance().e(CNAME, e.getMessage());
			mdtrackResponse.setMessage("failed to create mdtrack. " + e.getMessage());
			response = Response.ok(mdtrackResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return response;	
	}
		
		
	private String verifySender(Cookie cookie, int userId) throws JSONException {
		JSONObject ret = new JSONObject();
				
		if(cookie == null) {
			ret.put("message", "Error receiving cookie. Cookie was null");
			ret.put("status", "fail");			
			return ret.toString();
		}
		
		String token = cookie.getValue();
		if(token == null || token.isEmpty()) {
			ret.put("message", "Error reading token. Cookie value was null/empty");
			ret.put("status", "fail");			
			return ret.toString();
		}
		
		User user = null;
		try {
			user = EntityCacheMgr.getInstance().getUserEntity(userId);
		} catch (ICSDatastoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String username = null;
		if(user != null && user.getUsername() != null) {
			username = user.getUsername();
		} else {
			ret.put("message", "Error getting user information from userId: " + userId);
			ret.put("status", "fail");
			return ret.toString();
		}
		
		SSOUtil ssoUtil = null;
		try {
			ssoUtil = new SSOUtil();
			ssoUtil.loginAsAdmin();
						
			Map map = ssoUtil.getUserAttributes(token);
			StringBuilder sb = new StringBuilder();
			
			if(map != null && map.containsKey("uid")) {
				//String email = (String)map.get("uid");
				String email = null;
				
				HashSet emailSet = (HashSet)map.get("uid");
				Iterator<String> emailSetIter = emailSet.iterator();
				while(emailSetIter.hasNext()) {
					email = (String) emailSetIter.next();
					APILogger.getInstance().i("MDTrackService:verifySender", "found email in email hash set: " + email);
				}
				
				if(email != null && !email.isEmpty() && email.contains(username)) {
					// matches
					ret.put("message", "User's ID matches Identity");
					ret.put("status", "success");
				} else {
					ret.put("message", String.format("User's ID does not match identity. Sent token belongs to %s, but"
							+ " the userId maps to %s", email, username));
					ret.put("status", "fail");
				}
				
			} else {
				ret.put("message", "Error reading uid from user attributes");
				ret.put("status", "fail");
			}
			
			return ret.toString();
		
		} catch(Exception e) {
			ret.put("message", "Unhanlded exception verifying identity: " + e.getMessage());
			ret.put("status", "fail");
		} finally {
			if(ssoUtil != null) {
				String adminToken = ssoUtil.getTokenIfExists();
				if(token != null && !token.isEmpty()) {
					boolean destroyed = ssoUtil.logout(); 
				}
			}
		}
		
		return ret.toString();
	}


	/**
	 *  Read a single MDTrack item.
	 *  ID of MDTrack item to be read.
	 *  Response
	 *  MDTrackResponse
	 */	
	public Response getMDTrack(int mdtrackId) {
		//if(true) {
			return makeUnsupportedOpRequestResponse();
		//}
		
		/*Response response = null;
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();

		if (mdtrackId < 1) {
			mdtrackResponse.setMessage("Invalid mdtrackId value: " + mdtrackId) ;
			response = Response.ok(mdtrackResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		MDTrack u = null;
		try {
			u = MDTrackDAO.getInstance().getMDTrackById(mdtrackId);
		} catch (ICSDatastoreException e) {
			PAPILogger.getInstance().e(CNAME, e.getMessage());
			mdtrackResponse.setMessage("Failure. " + e.getMessage());
			response = Response.ok(mdtrackResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		}
		
		if (u == null) {
			mdtrackResponse.setMessage("No mdtrack found for mdtrackId value: " + mdtrackId) ;
			response = Response.ok(mdtrackResponse).status(Status.NOT_FOUND).build();
			return response;			
		}

		mdtrackResponse.getMDTracks().add(u);
		mdtrackResponse.setMessage("ok");
		mdtrackResponse.setCount(1);
		response = Response.ok(mdtrackResponse).status(Status.OK).build();

		return response;*/
	}

	/**
	 *  Delete a single MDTrack item.
	 *  ID of MDTrack item to be read.
	 *  Response
	 *  MDTrackResponse
	 */	
	public Response deleteMDTrack(int mdtrackId) {
		/*
		Response response = null;
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();

		if (mdtrackId < 1) {
			mdtrackResponse.setMessage("Invalid mdtrackId value: " + mdtrackId) ;
			response = Response.ok(mdtrackResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		try {
			MDTrackDAO.getInstance().removeMDTrack(mdtrackId);
			mdtrackResponse.setMessage("ok");
			response = Response.ok(mdtrackResponse).status(Status.OK).build();			
		} catch (ICSDatastoreException e) {
			mdtrackResponse.setMessage(e.getMessage()) ;
			response = Response.ok(mdtrackResponse).status(Status.NOT_FOUND).build();			
		}

		return response;
		*/
		return makeUnsupportedOpRequestResponse();		
	}

	/**
	 *  Update a single MDTrack item.
	 *  ID of MDTrack item to be read.
	 *  Response
	 *  MDTrackResponse
	 */	
	public Response putMDTrack(int mdtrackId, MDTrack mdtrack) {
		/*
		Response response = null;
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();

		if (mdtrackId < 1) {
			mdtrackResponse.setMessage("Invalid mdtrackId value: " + mdtrackId) ;
			response = Response.ok(mdtrackResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		if (mdtrack == null) {
			mdtrackResponse.setMessage("Invalid null MDTrack object.") ;
			response = Response.ok(mdtrackResponse).status(Status.BAD_REQUEST).build();
			return response;
		}		

		try {
			MDTrackDAO.getInstance().updateMDTrack(mdtrackId, mdtrack);
			MDTrack u = MDTrackDAO.getInstance().getMDTrackById(mdtrackId);
			mdtrackResponse.getMDTracks().add(u);
			mdtrackResponse.setMessage("ok");
			response = Response.ok(mdtrackResponse).status(Status.OK).build();			
		} catch (ICSDatastoreException e) {
			mdtrackResponse.setMessage(e.getMessage()) ;
			response = Response.ok(mdtrackResponse).status(Status.NOT_FOUND).build();	
		}

		return response;
		*/
		return makeUnsupportedOpRequestResponse();		
	}

	/**
	 *  Post a single MDTrack item.
	 *  This is an illegal operation. 
	 *  ID of MDTrack item to be read.
	 *  Response
	 *  MDTrackResponse
	 */	
	public Response postMDTrack(int mdtrackId) {
		return makeIllegalOpRequestResponse();
	}

	/**
	 *  Return the number of MDTrack items stored. 
	 *  Response
	 *  MDTrackResponse
	 */		
	public Response getMDTrackCount() {
		/*
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();
		mdtrackResponse.setMessage("ok");
		mdtrackResponse.setCount(MDTrackDAO.getInstance().getMDTrackCount());
		mdtrackResponse.setMDTracks(null);
		Response response = Response.ok(mdtrackResponse).status(Status.OK).build();		
		return response;
		*/
		return makeUnsupportedOpRequestResponse();		
	}


	/**
	 *  Search the MDTrack items stored. 
	 *  Response
	 *  MDTrackResponse
	 */		
	public Response searchMDTrackResources() {
		return makeUnsupportedOpRequestResponse();
	}
	
	private Response makeIllegalOpRequestResponse() {
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();
		mdtrackResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Illegal operation requested").
				status(Status.FORBIDDEN).build();
		return response;
	}	
	
	private Response makeUnsupportedOpRequestResponse() {
		MDTrackServiceResponse mdtrackResponse = new MDTrackServiceResponse();
		mdtrackResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Unsupported operation requested").
				status(Status.NOT_IMPLEMENTED).build();
		return response;
	}
	
	/**
	 * Checks to see if the SSOUtil has been properly initialized. If it hasn't, it attempts to
	 * initialize it. If it still fails, a flag is set specifying there was an initialization
	 * failure
	 *
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
	}*/
	
	
	/**
	 * Initializes the SSOUtil with property files in the path specified by
	 * the ssoToolsPropertyPath system property
	 * 
	 * @return true if SSOUtil was successfully initialized, false otherwise
	 * @throws InitializationException
	 *
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
	}*/
}

