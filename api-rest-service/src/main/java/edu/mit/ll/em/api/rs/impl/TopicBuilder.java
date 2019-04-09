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
package edu.mit.ll.em.api.rs.impl;

public class TopicBuilder {
	
	
	public static String makeNicsNS(Integer workspaceId) {
		StringBuffer ret = new StringBuffer();
		if (workspaceId == null) {
			workspaceId = new Integer(1);
		}
		ret.append("NICS.ws.").append(workspaceId);
		return ret.toString();
	}
	
	public static String makeNicsIncidentsNS(Integer workspaceId) {
		StringBuffer ret = new StringBuffer(makeNicsNS(workspaceId));
		ret.append(".incidents");
		return ret.toString();
	}
	
	public static String makePubChatTopic(Integer workspaceId, int incidentId,
			int collabroomId) {
		StringBuffer ret = new StringBuffer(makeNicsIncidentsNS(workspaceId));

		// Old topic naming example: "LDDRS.incidents.MAMITLLSanti02.collab.IncidentMap"
		// Migrated topic naming example: "NICS.ws.1.incidents.MAMITLLSanti02.collab.IncidentMap"
		// New topic naming example: "NICS.ws.1.incidents.8.collab.11"
		ret.append(".").append(incidentId).append(".collab.").append(collabroomId);
		return ret.toString();
	}
	
	public static String makeMarkupTopic(Integer workspaceId, int incidentId,
			int collabroomId) {
		return makePubChatTopic(workspaceId, incidentId, collabroomId);
	}
	
	public static String makeOtherRocTopic(Integer workspaceId, int incidentId) {
		StringBuffer ret = new StringBuffer(makeNicsIncidentsNS(workspaceId));
		ret.append(".").append(incidentId).append(".other.roc");
		return ret.toString();
	}	
	
	public static String makePrivateChatTopicNS(Integer workspaceId) {
		StringBuffer ret = new StringBuffer(makeNicsNS(workspaceId));
		ret.append(".private");
		return ret.toString();
	}
}
