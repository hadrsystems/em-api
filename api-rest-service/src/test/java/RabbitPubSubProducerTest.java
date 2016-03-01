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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import org.joda.time.DateTime;

import com.google.gson.Gson;

import edu.mit.ll.em.api.entity.TaskAssignmentEvent;
import edu.mit.ll.em.api.rs.MsgEnvelope;


public class RabbitPubSubProducerTest {
	
	private static Gson gson = new Gson();
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

	private static String makeJsonMsg() {
		// Build a list of recipient IDs.
		Set<String> recipients = new LinkedHashSet<String>();
		recipients.add("1");
		recipients.add("7777");
		
		// Specific event created by the Entity Consumer once a
		// Tasking form has been stored.
		TaskAssignmentEvent e = new TaskAssignmentEvent();
		e.setAssignmentRecipientList(recipients);
		e.setIncidentId(99);
		e.setIncidentName("Oil Spill 99");
		e.setMessage("Visit site immediately");
		e.setTaskAssignmentId(1007);
		
		// Wrap in in MsgEnvelope.
		DateTime now = new DateTime();
		MsgEnvelope me = new MsgEnvelope();
		me.setMsgType("TaskAssignmentEvent");
		me.setMsgPayload(gson.toJson(e));
		me.setMsgTimestamp(now.toString());
		
		return gson.toJson(me);
	}

	public static void main(String args[]) throws IOException {
		
		String rabbitHost = (args.length > 0) ? args[0] : "localhost";
		String exchangeName = (args.length > 1) ? args[1] : "XYZ";
		String bindingKeys[] = getBindingKeys(args, 2);
		
		RabbitPubSubProducer p = new RabbitPubSubProducer(
				rabbitHost,  exchangeName);
		for (int n = 0; n < 20; ++n) {
			try {
				for (int j = 0; j < bindingKeys.length; ++j) {
					String topic = bindingKeys[j];
					p.produce(topic, makeJsonMsg());
				}
				Thread.sleep(500);				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Producer is now shutting down...");
		p.destroy();
		System.out.println("Done.");
	}	
}
