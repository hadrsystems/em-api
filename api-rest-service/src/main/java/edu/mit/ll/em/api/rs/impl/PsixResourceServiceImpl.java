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

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.em.api.dataaccess.ICSDatastoreException;
import edu.mit.ll.em.api.dataaccess.PsixResourceDAO;
import edu.mit.ll.em.api.rs.PsixResource;
import edu.mit.ll.em.api.rs.PsixResourceService;
import edu.mit.ll.em.api.rs.PsixResourceServiceResponse;
import edu.mit.ll.em.api.util.APILogger;

/**
 * 
 * @AUTHOR sa23148
 * Quick attempt to hook up to PSIX but unsuccessful :(
 *
 */
public class PsixResourceServiceImpl implements PsixResourceService {

	private static final String CNAME = PsixResourceServiceImpl.class.getName();
	
	/**
	 * Read and return all PsixResource items.
	 * @return Response
	 * @see PsixResourceResponse
	 */
	public Response getPsixResources() {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/* 
		Response response = null;
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();

        Set<Integer> psixResourceIds = PsixResourceDAO.getInstance().getAllPsixResourceIds();
		for (Integer psixResourceId : psixResourceIds) {
			psixResourceResponse.getPsixResources().add(PsixResourceDAO.getInstance().getPsixResourceById(psixResourceId));
		}
		psixResourceResponse.setMessage("ok");
		psixResourceResponse.setCount(psixResourceIds.size());
		response = Response.ok(psixResourceResponse).status(Status.OK).build();

		return response;
		*/
	}

	/**
	 * Delete all PsixResource items.
	 * This is an unsupported operation.
	 *  Response
	 *  PsixResourceResponse
	 */
	public Response deletePsixResources() {
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 * Bulk creation of PsixResource items.
	 *  A collection of PsixResource items to be created.
	 *  Response
	 *  PsixResourceResponse
	 */
	public Response putPsixResources(Collection<PsixResource> psixResources) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();
		Response response = null;
		int errorCount = 0;
		for (PsixResource psixResource : psixResources) {
			try {
				PsixResource newPsixResource = PsixResourceDAO.getInstance().createPsixResource(psixResource);
				psixResourceResponse.getPsixResources().add(newPsixResource);
			} catch (ICSDatastoreException e) {
				PAPILogger.getInstance().e(CNAME, e.getMessage());
				++errorCount;
			}			
		}
		
		if (errorCount == 0) {
			psixResourceResponse.setMessage("ok");
			psixResourceResponse.setCount(psixResourceIds.size());
			response = Response.ok(psixResourceResponse).status(Status.OK).build();			
		} else {
			psixResourceResponse.setMessage("Failures. " + errorCount + " out of " + psixResources.size() + " were not created.");
			response = Response.ok(psixResourceResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return response;
		*/
	}

	/**
	 *  Creation of a single PsixResource item.
	 *  A collection of PsixResource items to be created.
	 *  Response
	 *  PsixResourceResponse
	 */	
	public Response postPsixResources(PsixResource psixResource) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();
		Response response = null;

		PsixResource newPsixResource = null;
		try {
			newPsixResource = PsixResourceDAO.getInstance().createPsixResource(psixResource);
			psixResourceResponse.getPsixResources().add(newPsixResource);
			psixResourceResponse.setMessage("ok");
			psixResourceResponse.setCount(psixResourceIds.size());
			response = Response.ok(psixResourceResponse).status(Status.OK).build();
		} catch (ICSDatastoreException e) {
			psixResourceResponse.setMessage("failed to create psixResource.");
			response = Response.ok(psixResourceResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return response;
		*/
	}


	/**
	 *  Read a single PsixResource item.
	 *  ID of PsixResource item to be read.
	 *  Response
	 *  PsixResourceResponse
	 */	
	public Response getPsixResource(int psixResourceId) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		Response response = null;
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();

		if (psixResourceId < 1) {
			psixResourceResponse.setMessage("Invalid psixResourceId value: " + psixResourceId) ;
			response = Response.ok(psixResourceResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		PsixResource u = PsixResourceDAO.getInstance().getPsixResourceById(psixResourceId);
		if (u == null) {
			psixResourceResponse.setMessage("No psixResource found for psixResourceId value: " + psixResourceId) ;
			response = Response.ok(psixResourceResponse).status(Status.NOT_FOUND).build();
			return response;			
		}

		psixResourceResponse.getPsixResources().add(u);
		psixResourceResponse.setMessage("ok");
		psixResourceResponse.setCount(1);
		response = Response.ok(psixResourceResponse).status(Status.OK).build();

		return response;
		*/
	}

	/**
	 *  Delete a single PsixResource item.
	 *  ID of PsixResource item to be read.
	 *  Response
	 *  PsixResourceResponse
	 */	
	public Response deletePsixResource(int psixResourceId) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		Response response = null;
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();

		if (psixResourceId < 1) {
			psixResourceResponse.setMessage("Invalid psixResourceId value: " + psixResourceId) ;
			response = Response.ok(psixResourceResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		try {
			PsixResourceDAO.getInstance().removePsixResource(psixResourceId);
			psixResourceResponse.setMessage("ok");
			response = Response.ok(psixResourceResponse).status(Status.OK).build();			
		} catch (ICSDatastoreException e) {
			psixResourceResponse.setMessage(e.getMessage()) ;
			response = Response.ok(psixResourceResponse).status(Status.NOT_FOUND).build();			
		}

		return response;
		*/
	}

	/**
	 *  Update a single PsixResource item.
	 *  ID of PsixResource item to be read.
	 *  Response
	 *  PsixResourceResponse
	 */	
	public Response putPsixResource(int psixResourceId, PsixResource psixResource) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		Response response = null;
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();

		if (psixResourceId < 1) {
			psixResourceResponse.setMessage("Invalid psixResourceId value: " + psixResourceId) ;
			response = Response.ok(psixResourceResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		if (psixResource == null) {
			psixResourceResponse.setMessage("Invalid null PsixResource object.") ;
			response = Response.ok(psixResourceResponse).status(Status.BAD_REQUEST).build();
			return response;
		}		

		try {
			PsixResourceDAO.getInstance().updatePsixResource(psixResourceId, psixResource);
			PsixResource u = PsixResourceDAO.getInstance().getPsixResourceById(psixResourceId);
			psixResourceResponse.getPsixResources().add(u);
			psixResourceResponse.setMessage("ok");
			response = Response.ok(psixResourceResponse).status(Status.OK).build();			
		} catch (ICSDatastoreException e) {
			psixResourceResponse.setMessage(e.getMessage()) ;
			response = Response.ok(psixResourceResponse).status(Status.NOT_FOUND).build();	
		}

		return response;
		*/
	}

	/**
	 *  Post a single PsixResource item.
	 *  This is an illegal operation. 
	 *  ID of PsixResource item to be read.
	 *  Response
	 *  PsixResourceResponse
	 */	
	public Response postPsixResource(int psixResourceId) {
		// Illegal as per RESTful guidelines.
		return makeIllegalOpRequestResponse();
	}

	/**
	 *  Return the number of PsixResource items stored. 
	 *  Response
	 *  PsixResourceResponse
	 */		
	public Response getPsixResourceCount() {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();
		psixResourceResponse.setMessage("ok");
		psixResourceResponse.setCount(PsixResourceDAO.getInstance().getPsixResourceCount());
		psixResourceResponse.setPsixResources(null);
		Response response = Response.ok(psixResourceResponse).status(Status.OK).build();		
		return response;
		*/
	}


	/**
	 *  Search the PsixResource items stored. 
	 *  Response
	 *  PsixResourceResponse
	 */		
	public Response searchPsixResourceResources() {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
	}
	
	private Response makeIllegalOpRequestResponse() {
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();
		psixResourceResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Illegal operation requested").
			status(Status.FORBIDDEN).build();
		return response;
	}
	
	private Response makeUnsupportedOpRequestResponse() {
		PsixResourceServiceResponse psixResourceResponse = new PsixResourceServiceResponse();
		psixResourceResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Unsupported operation requested").
			status(Status.NOT_IMPLEMENTED).build();
		return response;
	}		
}

