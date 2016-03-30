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
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.em.api.dataaccess.MessageBusAccess;
import edu.mit.ll.em.api.msgbus.MsgBusSubscriptionException;
import edu.mit.ll.em.api.rs.MsgBusResponse;
import edu.mit.ll.em.api.rs.MsgBusService;
import edu.mit.ll.em.api.rs.MsgEnvelope;

public class MsgBusServiceImpl implements MsgBusService {

	public Response getMsgs(long userId) {
		Response response = null;
		MsgBusResponse msgbusResponse = new MsgBusResponse();

		try {
			for (MsgEnvelope e : MessageBusAccess.getInstance().getAllMessages(userId)) {
				msgbusResponse.getBusMsgs().add(e);
			}
			msgbusResponse.setMessage("ok");
		} catch (MsgBusSubscriptionException e) {
			msgbusResponse.setMessage("failure. " + e.getMessage());
		}
		response = Response.ok(msgbusResponse).status(Status.OK).build();			

		return response;
	}

	public Response postMsgs(long userId, Collection<MsgEnvelope> msgs) {
		Response response = null;
		MsgBusResponse msgbusResponse = new MsgBusResponse();
		
		try {
			MessageBusAccess.getInstance().postMessages(userId, msgs);
			msgbusResponse.setMessage("ok");			
		} catch (MsgBusSubscriptionException e) {
			msgbusResponse.setMessage("failure. " + e.getMessage());
		}
		response = Response.ok(msgbusResponse).status(Status.OK).build();
		
		return response;		
	}

	public Response subscribe(Map<String, String> attrs) {
		Response response = null;
		MsgBusResponse msgbusResponse = new MsgBusResponse();
		
		try {
			MessageBusAccess.getInstance().subscribe(attrs);
			msgbusResponse.setMessage("ok");
		} catch (MsgBusSubscriptionException e) {
			msgbusResponse.setMessage("failure. " + e.getMessage());
		}
		response = Response.ok(msgbusResponse).status(Status.OK).build();
		
		return response;		
	}

	public Response unsubscribe(long userId) {
		Response response = null;
		MsgBusResponse msgbusResponse = new MsgBusResponse();
		
		try {
			MessageBusAccess.getInstance().unsubscribe(userId);
			msgbusResponse.setMessage("ok");			
		} catch (MsgBusSubscriptionException e) {
			msgbusResponse.setMessage("failure. " + e.getMessage());
		}
		response = Response.ok(msgbusResponse).status(Status.OK).build();
		
		return response;		
	}
}
