/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
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
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.em.api.rs.MsgEnvelope;

@Path("/msgbus")
public interface MsgBusService {
	/**
	 * Get all messages in the user's bus.
	 * @param userId Identifies the user
	 * @return List of messages embedded in the Response. Each message is wrapped in a MsgEnvelope.
	 */
	@GET
	@Path(value = "/{userId}/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMsgs(@PathParam("userId") long userId);

	/**
	 * Post messages to the user's bus.
	 * @param userId Identifies the user
	 * @param msgs 
	 * @return Status embedded in Response.
	 */
	@POST
	@Path(value = "/{userId}/")
	@Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMsgs(@PathParam("userId") long userId, Collection<MsgEnvelope>msgs);

	/**
	 * Creates a short-lived message holder where contents from the Message Bus
	 * will be stored. 
	 * @param msgs 
	 * @return Status embedded in Response.
	 */	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response subscribe(Map<String, String> attrs);
	
	/**
	 * Destroy the message holder and its contents.
	 * @param userId Identifies the user
	 * @return Status embedded in Response.
	 */
	@DELETE
	@Path(value = "/{userId}/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response unsubscribe(@PathParam("userId") long userId);
}
