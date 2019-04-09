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
package edu.mit.ll.em.api.dataaccess;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import edu.mit.ll.em.api.rs.impl.TopicBuilder;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.messages.sadisplay.SADisplayMessage;
import edu.mit.ll.nics.common.rabbitmq.client.RabbitProducer;


public class CollabDAO extends BaseDAO {

	private static final String CNAME = CollabDAO.class.getName();
		
	private RabbitProducer producer;

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static CollabDAO instance = new CollabDAO();
	}

	public static CollabDAO getInstance() {
		return Holder.instance;
	}

	CollabDAO() {
		String host = validateRabbitHostName();
		String username = validateRabbitUsername();
		String userpwd = validateRabbitUserpwd();
		//dbf = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();

		producer = new RabbitProducer(username, userpwd, host, 5672);
	}
	/*
	public CollabRoom getCollabRoom(int collabId) throws PhinicsDbException {
		CollabRoom ret = null;
		ret = dbf.readCollabRoom(collabId);
		
		return ret;
	}
	
	public List<CollabRoom> getAccessibleCollabRooms(int userId, int incidentId)
			throws PhinicsDbException {
		List<CollabRoom> ret = null;
		EntityManager em = this.allocEntityManager();
		try {
			ret = dbf.getAccessibleCollabRooms(em, userId, incidentId);
			
		} finally {
			this.freeEntityManager(em);
		}
		return ret;
	}
	
	public List<CollabRoom> getCollabRooms(int incidentId) {
		List<CollabRoom> collabrooms = null;
		EntityManager em = this.allocEntityManager();
		try {
			collabrooms = dbf.getCollabRoomsByIncidentId(em, incidentId);
		} finally {
			this.freeEntityManager(em);
		}
		return collabrooms;
	}

	*/
	private static String validateRabbitHostName() {
		String host = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_HOSTNAME_KEY);
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("Undefined configuration key " +
					APIConfig.RABBIT_HOSTNAME_KEY + 
					". No Rabbit BUS communications will be possible. " +
					"Add or fix entry in file " + APIConfig.API_PROPS_FILE);
		}
		return host;
	}

	private static String validateRabbitUsername() {
		String username = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_USERNAME_KEY);
		if (username == null || username.isEmpty()) {
			username = "guest";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.RABBIT_USERNAME_KEY +
					" in " + APIConfig.API_PROPS_FILE + " file. Using " +
					"guest" + " as Rabbit user name.");
		}
		return username;
	}

	private static String validateRabbitUserpwd() {
		String userpwd = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_USERPWD_KEY);
		if (userpwd == null || userpwd.isEmpty()) {
			userpwd = "guest";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.RABBIT_USERPWD_KEY +
					" in " + APIConfig.API_PROPS_FILE + " file. Using " +
					"guest" + " as Rabbit user name.");
		}
		return userpwd;
	}	
	/*
	private long getMessageSequenceNumber() {
		EntityManager em = this.allocEntityManager();
		BigInteger seqnum = null;
		long retval = -1;
		try {
			seqnum = (BigInteger) em
					.createNativeQuery("select nextval('message_sequence')").getSingleResult();
			if(seqnum != null) {
				retval = seqnum.longValue();
			}
		} finally {
			this.freeEntityManager(em);
		}
		return retval;
	}
	*/
}


