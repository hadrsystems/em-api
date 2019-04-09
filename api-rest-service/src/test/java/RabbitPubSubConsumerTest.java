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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubConsumer;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubMsg;

public class RabbitPubSubConsumerTest {
	
	private static String DEFAULT_BINDING_KEY = "LDDRS.notifications.forms";
	
	private static String[] getBindingKeys(String args[], int keyOffset) {
		List<String> keyList = new ArrayList<String>();
		while (keyOffset < args.length) {
			keyList.add(args[keyOffset++]);
		}
		String[] ret = null;
		if (keyList.size() > 0) {
			ret = keyList.toArray(new String[keyList.size()]);
		} else {
			ret = new String[1];
			ret[0] = DEFAULT_BINDING_KEY;
		}
		return ret;
	}

	public static void main(String args[]) 
			throws IOException, ShutdownSignalException, 
			ConsumerCancelledException, InterruptedException {
		String rabbitHost = (args.length > 0) ? args[0] : "localhost";
		String exchangeName = (args.length > 1) ? args[1] : "XYZ";
		String bindingKeys[] = getBindingKeys(args, 2);
		
		RabbitPubSubConsumer c = new RabbitPubSubConsumer(
				rabbitHost, exchangeName, bindingKeys);
		
		for (int n = 0; n < 40; ++n) {			
			RabbitPubSubMsg m = c.consume();
			System.out.println(" [x] Received '" + m.getMsg() + "'" + 
					" with routingKey: " + m.getRoutingKey());
		}
		System.out.println("Consumer is now shutting down...");
		c.destroy();
		System.out.println("Done.");		
	}
}
