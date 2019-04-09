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
package edu.mit.ll.em.api.msgbus;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.mit.ll.em.api.rs.MsgEnvelope;
import edu.mit.ll.em.api.rs.StringConstant;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.MapUtil;

public class MsgBusSubscriptionMgr {

	private static final String CNAME = MsgBusSubscriptionMgr.class.getName();

	private ConcurrentHashMap<Long, SubscriptionSession> sessions =
			new ConcurrentHashMap<Long, SubscriptionSession>(500);

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static MsgBusSubscriptionMgr instance = new MsgBusSubscriptionMgr();
	}

	public static MsgBusSubscriptionMgr getInstance() {
		return Holder.instance;
	}

	/**
	 * Starts temporary subscription.
	 * @param attrs Must provide all attributes necessary for subscribing.
	 * @return Subscription ID
	 */
	public void beginSubscription(Map<String, String> attrs) throws
	MsgBusSubscriptionException {
		APILogger.getInstance().d(CNAME, "beginSubscription() - Begin.");

		// Grab all expected attributes.
		String subscriberIdStr = MapUtil.getDefault(
				StringConstant.URIOPT_SUBSCRIBER_ID, attrs, "-1");
		String topicsStr = MapUtil.getDefault(
				StringConstant.URIOPT_TOPICS, attrs, "");
		APILogger.getInstance().d(CNAME, "topicsStr = " + topicsStr);
		String[] topics = topicsStr.split("[+]");
		String timeoutSecsStr = MapUtil.getDefault(
				StringConstant.URIOPT_TIMEOUT, attrs, "600");

		// Subscribe.		
		SubscriptionSession ss = SubscriptionSession.create(
				subscriberIdStr, topics, timeoutSecsStr);
		sessions.putIfAbsent(ss.getSubscriberId(), ss);

		APILogger.getInstance().d(CNAME, "beginSubscription() - End.");
	}

	public void endSubscription(long sid) throws
	MsgBusSubscriptionException {
		SubscriptionSession ss = sessions.remove(sid);
		if (ss != null) {
			ss.terminate();
			APILogger.getInstance().d(CNAME, "Subscription ID terminated: " + sid);
		} else {
			APILogger.getInstance().w(CNAME, "Unsubscribe failed. Subscription ID not found: " + sid);
		}
	}

	public void postToSubscription(long sid, MsgEnvelope msg) throws
	MsgBusSubscriptionException {
		SubscriptionSession ss = sessions.get(sid);
		if (ss != null) {
			ss.post(msg);
			APILogger.getInstance().d(CNAME, "Subscription ID " + sid + " posted messages.");
		} else {
			APILogger.getInstance().w(CNAME, "Post failed. Subscription ID not found: " + sid);
		}		
	}
	
	public void postToSubscription(long sid, Collection<MsgEnvelope> msgs) throws
	MsgBusSubscriptionException {
		SubscriptionSession ss = sessions.get(sid);
		if (ss != null) {
			ss.post(msgs);
			APILogger.getInstance().d(CNAME, "Subscription ID " + sid + " posted messages.");
		} else {
			APILogger.getInstance().w(CNAME, "Post failed. Subscription ID not found: " + sid);
		}		
	}

	public Collection<MsgEnvelope> getFromSubscription(long sid) throws
	MsgBusSubscriptionException {

		Collection<MsgEnvelope>	msgs = null;
		SubscriptionSession ss = sessions.get(sid);
		if (ss != null) {
			msgs = ss.get();
			APILogger.getInstance().d(CNAME, "Subscription ID " + sid + " obtained messages.");
		} else {
			APILogger.getInstance().w(CNAME, "Post failed. Subscription ID not found: " + sid);
		}
		
		return msgs;
	}

	// Hide the default constructor.
	@SuppressWarnings("unused")
	private void SubscritionMgr() {}
}
