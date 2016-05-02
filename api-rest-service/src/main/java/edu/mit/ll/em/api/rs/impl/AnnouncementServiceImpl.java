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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.nics.common.entity.Log;
import edu.mit.ll.nics.nicsdao.impl.LogDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.em.api.rs.AnnouncementService;
import edu.mit.ll.em.api.rs.LogServiceResponse;
import edu.mit.ll.em.api.util.SADisplayConstants;

/**
 * 
 * @AUTHOR st23429
 *
 */
public class AnnouncementServiceImpl implements AnnouncementService {
	
	private static String POST_ERROR_MESSAGE = "An error occurred while attempting to post a new announcement.";
	private static String GET_ERROR_MESSAGE = "An error occurred while attempting to retrieve announcements.";
	private static String DELETE_ERROR_MESSAGE = "An error occurred while attempting to delete an announcement.";
	
	/** User DAO */
	private static final UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
	private static final LogDAOImpl logDao = new LogDAOImpl();

	@Override
	public Response postAnnouncement(int workspaceId, Log log, String username) {
		
		if(userOrgDao.isUserRole(username, SADisplayConstants.ADMIN_ROLE_ID) ||
				userOrgDao.isUserRole(username, SADisplayConstants.SUPER_ROLE_ID)){
			if(logDao.postLog(workspaceId, log)){
				return Response.status(Status.OK).entity(Status.OK.getReasonPhrase()).build(); 
			}else{
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(POST_ERROR_MESSAGE).build();
			}
		}
		
		return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
	}

	@Override
	public Response getAnnouncements(int workspaceId, String username) {
		LogServiceResponse logResponse = new LogServiceResponse();
		logResponse.setResults(logDao.getLogs(workspaceId, SADisplayConstants.ANNOUNCEMENTS_LOG_TYPE));
		if(logResponse.getResults() == null){
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(GET_ERROR_MESSAGE).build();
		}
		return Response.status(Status.OK).entity(logResponse).build();
	}

	@Override
	public Response deleteAnnouncement(int workspaceId, int logId, String username) {
		if(userOrgDao.isUserRole(username, SADisplayConstants.ADMIN_ROLE_ID) ||
				userOrgDao.isUserRole(username, SADisplayConstants.SUPER_ROLE_ID)){
			if(logDao.deleteLog(logId)){
				return Response.status(Status.OK).entity(Status.OK.getReasonPhrase()).build(); 
			}else{
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(DELETE_ERROR_MESSAGE).build();
			}
		}
		
		return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
	}
}

