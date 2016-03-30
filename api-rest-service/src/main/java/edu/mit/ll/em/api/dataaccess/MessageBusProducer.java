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
package edu.mit.ll.em.api.dataaccess;

import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.rabbitmq.client.RabbitProducer;


public class MessageBusProducer {

	private static final String CNAME = MessageBusProducer.class.getName();
			
	private RabbitProducer producer;
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static MessageBusProducer instance = new MessageBusProducer();
	}

	public static MessageBusProducer getInstance() {
		return Holder.instance;
	}
	
	public MessageBusProducer() {
		String host = validateRabbitHostName();
		String username = validateRabbitUsername();
		String userpwd = validateRabbitUserpwd();
		producer = new RabbitProducer(username, userpwd, host, 5672);		
	}
	
	public boolean sendMessage(String topic, String message) {
		boolean status = false; 
		
		try {
			status = producer.sendMessage(topic, message);
		} catch(Exception e) {
			APILogger.getInstance().e(CNAME, "Exception sending message:\n" +
				"\ttopic: " + topic + "\n\tmessage: " + message);
		}
		
		return status;
	}
	
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
}
