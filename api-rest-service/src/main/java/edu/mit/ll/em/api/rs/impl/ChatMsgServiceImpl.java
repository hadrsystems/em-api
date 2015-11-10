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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import edu.mit.ll.em.api.rs.ChatMsgService;
import edu.mit.ll.em.api.rs.ChatMsgServiceResponse;
import edu.mit.ll.em.api.rs.ChatOptionalParams;
import edu.mit.ll.em.api.rs.QueryConstraintHelper;
import edu.mit.ll.em.api.util.rabbitmq.RabbitFactory;
import edu.mit.ll.em.api.util.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.common.entity.Chat;
import edu.mit.ll.nics.nicsdao.ChatDAO;
import edu.mit.ll.nics.nicsdao.impl.ChatDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.CollabRoomDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.query.QueryConstraint.OrderBy;
import edu.mit.ll.nics.nicsdao.query.QueryConstraint.ResultSetPage;
import edu.mit.ll.nics.nicsdao.query.QueryConstraint.UTCRange;

/**
 * 
 * @AUTHOR sa23148
 *
 */
public class ChatMsgServiceImpl implements ChatMsgService {

	/** Chat DAO */
	private static final ChatDAO chatDao = new ChatDAOImpl();
	
	/** CollabRoom DAO */
	private static final CollabRoomDAOImpl collabDao = new CollabRoomDAOImpl();
	
	/** CollabRoom DAO */
	private static final UserDAOImpl userDao = new UserDAOImpl();
	
	private static final Log logger = LogFactory.getLog(ChatMsgServiceImpl.class);
	
	private RabbitPubSubProducer rabbitProducer;
	
	
	/**
	 * Retrieve chat messages from the specified collab room
	 * 
	 * @param collabroomId
	 * @param optionalParams
	 * 
	 * @return Response ChatMsgServiceResponse containing chat messages
	 * @See ChatMsgServiceResponse
	 */
	public Response getChatMsgs(int collabroomId, ChatOptionalParams optionalParams, String requestingUser) {
		if(!collabDao.hasPermissions(userDao.getUserId(requestingUser), collabroomId)){
			return getInvalidResponse();
		}
		
		UTCRange dateRange = QueryConstraintHelper.makeDateRange(optionalParams);
		OrderBy orderBy = QueryConstraintHelper.makeOrderBy(optionalParams);
		ResultSetPage pageRange = QueryConstraintHelper.makeResultSetRange(optionalParams);
		
		List<Chat> chats = chatDao.getChatMessages(collabroomId, dateRange, orderBy, pageRange);
		
		ChatMsgServiceResponse chatMsgResponse = new ChatMsgServiceResponse();
		chatMsgResponse.setMessage(Status.OK.getReasonPhrase());
		chatMsgResponse.setChats(chats);
		chatMsgResponse.setCount(chats.size());
		Response response = Response.ok(chatMsgResponse).status(Status.OK).build();
		return response;
	}
	
	
	/**
	 * Creates a single chat message
	 * 
	 * @param collabroomId
	 * @param chatMsg
	 * 
	 * @return Response A ChatMsgServiceResponse
	 */
	public Response postChatMsg(int collabroomId, Chat chat, String requestingUser) {
		
		if(!collabDao.hasPermissions(userDao.getUserId(requestingUser), collabroomId)){
			return getInvalidResponse();
		}

		//disallow the client setting some values
		chat.setChatid(-1);
		chat.setCollabroomid(collabroomId);
		chat.setCreated(new Date());
		
		ChatMsgServiceResponse chatMsgResponse = new ChatMsgServiceResponse();
		Response response = null;
		Chat newChat = null;
		try {	
			int newChatId = chatDao.addChat(chat);
			newChat = chatDao.getChatMessage(newChatId);
			
			chatMsgResponse.setMessage(Status.OK.getReasonPhrase());
			chatMsgResponse.setChats(Arrays.asList(newChat));
			chatMsgResponse.setCount(1);
			response = Response.ok(chatMsgResponse).status(Status.OK).build();

		} catch (Exception e) {
			chatMsgResponse.setMessage("Unhandled exception while persisting Chat: " + e.getMessage());
			response = Response.ok(chatMsgResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyChange(newChat);
			} catch (IOException e) {
				logger.error("Failed to publish ChatMsgService message event", e);
			}
		}

		return response;
	}

	private void notifyChange(Chat chat) throws IOException {
		if (chat != null) {
			String topic = String.format("iweb.NICS.collabroom.%s.chat", chat.getCollabroomid());
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(chat);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private RabbitPubSubProducer getRabbitProducer() throws IOException {
		if (rabbitProducer == null) {
			rabbitProducer = RabbitFactory.makeRabbitPubSubProducer();
		}
		return rabbitProducer;
	}
	
	private Response getInvalidResponse(){
		return Response.status(Status.BAD_REQUEST).entity(
				Status.FORBIDDEN.getReasonPhrase()).build();
	}
}

