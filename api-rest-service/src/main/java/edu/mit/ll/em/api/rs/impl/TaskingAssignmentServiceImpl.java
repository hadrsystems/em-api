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

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.em.api.dataaccess.EntityCacheMgr;
import edu.mit.ll.em.api.dataaccess.ICSDatastoreException;
import edu.mit.ll.em.api.rs.QueryConstraintHelper;
import edu.mit.ll.em.api.rs.TaskingAssignmentOptParms;
import edu.mit.ll.em.api.rs.TaskingAssignmentService;
import edu.mit.ll.em.api.rs.TaskingAssignmentServiceResponse;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.Assignment;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.nicsdao.impl.TaskingDAOImpl;

/**
 * 
 * @AUTHOR sa23148
 *
 */
public class TaskingAssignmentServiceImpl implements TaskingAssignmentService {

	private static final String CNAME = TaskingAssignmentServiceImpl.class.getName();
	
	private static final TaskingDAOImpl taskingDao = new TaskingDAOImpl();
	
	TaskingAssignmentServiceImpl() {
	}

	/**
	 * Read and return all TaskingAssignment items.
	 * @return Response
	 * @see TaskingAssignmentResponse
	 */
	public Response getTaskingAssignments(String userName, TaskingAssignmentOptParms optParms) {
		Response response = null;
		TaskingAssignmentServiceResponse taskResponse = new TaskingAssignmentServiceResponse();

		// Provide some reasonable defaults where needed.
		if (optParms.getDateColumn() == null) {
			optParms.setDateColumn("seqtime");
		}
		if (optParms.getSortByColumn() == null) {
			optParms.setSortByColumn("seqtime");
		}
		if (optParms.getSortOrder() == null) {
			optParms.setSortOrder("DESC");
		}
		int maxRowsLimit = APIConfig.getInstance().getConfiguration().getInt(APIConfig.DB_MAX_ROWS, 1000);
		if (optParms.getLimit() == null || optParms.getLimit() > maxRowsLimit) {
			APILogger.getInstance().i(CNAME, "Rewriting max. rows LIMIT as " + maxRowsLimit);			
			optParms.setLimit(maxRowsLimit);
		}

		int userId = -1;
		if (userName != null && !userName.isEmpty()) {
			User user = null;
			try {
				user = EntityCacheMgr.getInstance().getUserEntityByUsername(userName);
				if (user != null) {
					userId = user.getUserId();
				} else {
					APILogger.getInstance().e(CNAME, "Could not find user with username " + userName);
					taskResponse.setMessage("Failed. Unable to find user with username = " + userName);
					taskResponse.setTaskingAssignments("[]");
					taskResponse.setCount(0);
					response = Response.ok(taskResponse).status(Status.EXPECTATION_FAILED).build();
					return response;					
				}
			} catch (ICSDatastoreException e) {
				APILogger.getInstance().e(CNAME, e.getMessage());
				taskResponse.setMessage("Failed. Unable to find user with username = " + userName);
				taskResponse.setTaskingAssignments("[]");
				taskResponse.setCount(0);
				response = Response.ok(taskResponse).status(Status.EXPECTATION_FAILED).build();
				return response;
			}
		}

		// Collect optional parameters common to all resources.		
		Map<String, Object> queryConstraints = QueryConstraintHelper.parseOptions(optParms);
		// The "incidentId" is a parameter specific to this resource so QueryConstraintHelper
		// does not parse it.
		if (optParms.getActiveOnly() != null) {
			queryConstraints.put("activeOnly", optParms.getActiveOnly());
		}

		List<Assignment> assignments = null;
		//EntityManager em = dbi.getEntityManagerFactory().createEntityManager();
		try {
			//assignments = TaskingAssignmentDAO.getInstance()
				//	.getTaskingAssignments(userId, queryConstraints, em);
			// TODO: handle query constraints
			assignments = taskingDao.getAssignments(userId, queryConstraints);
			
			taskResponse.setTaskingAssignments(assignmentListToJSON(assignments));
			taskResponse.setMessage("ok");
			taskResponse.setCount(assignments.size());
			response = Response.ok(taskResponse).status(Status.OK).build();
		} catch (Exception e) {
			APILogger.getInstance().e(CNAME, e.getMessage());
			taskResponse.setMessage("Failed. Datastore exception, failed to publish form.");
			taskResponse.setTaskingAssignments("[]");			
			taskResponse.setCount(0);
			response = Response.ok(taskResponse).status(Status.INTERNAL_SERVER_ERROR).build();			
		} 
		return response;
	}

	private String assignmentListToJSON(List<Assignment> assignmentList) {
		String json = null;
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean isFirst = true;
		for (Assignment a : assignmentList) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(", ");
			}
			String jsonStr = a.toJSONObject().toString();
			sb.append(jsonStr);
		}
		sb.append("]");
		json = sb.toString();
		return json;
	}

	/**
	 * Delete all TaskingAssignment items.
	 * This is an unsupported operation.
	 *  Response
	 *  TaskingAssignmentResponse
	 */
	public Response deleteTaskingAssignments() {
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 *  Read a single TaskingAssignment item.
	 *  ID of TaskingAssignment item to be read.
	 *  Response
	 *  TaskingAssignmentResponse
	 */	
	public Response getTaskingAssignment(long assignmentId) {
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 *  Delete a single TaskingAssignment item.
	 *  ID of TaskingAssignment item to be read.
	 *  Response
	 *  TaskingAssignmentResponse
	 */	
	public Response deleteTaskingAssignment(long assignmentId) {
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 *  Post a single TaskingAssignment item.
	 *  This is an illegal operation. 
	 *  ID of TaskingAssignment item to be read.
	 *  Response
	 *  TaskingAssignmentResponse
	 */	
	public Response postTaskingAssignment(long assignmentId) {
		return makeIllegalOpRequestResponse();
	}

	/**
	 *  Return the number of TaskingAssignment items stored. 
	 *  Response
	 *  TaskingAssignmentResponse
	 */		
	public Response getTaskingAssignmentCount() {
		return makeIllegalOpRequestResponse();		
	}


	/**
	 *  Search the TaskingAssignment items stored. 
	 *  Response
	 *  TaskingAssignmentResponse
	 */		
	public Response searchTaskingAssignmentResources() {
		return makeUnsupportedOpRequestResponse();
	}

	private Response makeIllegalOpRequestResponse() {
		TaskingAssignmentServiceResponse reportResponse = new TaskingAssignmentServiceResponse();
		reportResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Illegal operation requested").
				status(Status.FORBIDDEN).build();
		return response;
	}

	private Response makeUnsupportedOpRequestResponse() {
		TaskingAssignmentServiceResponse reportResponse = new TaskingAssignmentServiceResponse();
		reportResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Unsupported operation requested").
				status(Status.NOT_IMPLEMENTED).build();
		return response;
	}
}

