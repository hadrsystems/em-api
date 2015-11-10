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
package edu.mit.ll.em.api.util.rabbitmq;

import java.io.IOException;

import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;

public class RabbitFactory {

	private static final String CNAME = RabbitFactory.class.getName();
	
	private static RabbitPubSubProducer producer = null;
	
	public static RabbitPubSubProducer makeRabbitPubSubProducer() throws IOException {
		if(producer == null){
			String host = validateRabbitHostName();
			String exchange = validateRabbitExchange();
			String username = validateRabbitUsername();
			String userpwd = validateRabbitUserpwd();
			
			try {
				producer = new RabbitPubSubProducer(host, exchange, username, userpwd);
			} catch (IOException e) {
				throw new IOException("Failure trying to connect to " + host + "/" +
						exchange + ". " + e.getMessage());
			}
		}
		return producer;
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
	
	private static String validateRabbitExchange() {
		String exchange = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_EXCHANGENAME_KEY);
		if (exchange == null || exchange.isEmpty()) {
			exchange = RabbitClient.AMQ_TOPIC;
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.RABBIT_EXCHANGENAME_KEY +
					" in " + APIConfig.API_PROPS_FILE + " file. Using " +
					RabbitClient.AMQ_TOPIC + " as Rabbit Exchange name.");
		}
		return exchange;
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
