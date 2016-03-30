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
package edu.mit.ll.em.api.rs;

import java.util.Set;


public class MapMarkup extends APIBean {
	
	public static class Feature {
		public String dashStyle;
		public String featureAttrs;
		public long featureId;
		public String fillColor;
		public String graphic;
		public Double graphicHeight;
		public Double graphicWidth;
		public Boolean isGesture;
		public String ipAddr;
		public Double labelSize;
		public String labelText;
		public String nickname;
		public Long seqNum;
		public String strokeColor;
		public Double strokeWidth;
		public Long seqTime;
		public String time;
		public String topic;
		public String type;
		public Double opacity;
		public Double[][] points;
		public Double radius;
		public Double rotation;
		public Long lastUpdate;
	}
	
	private Integer collabRoomId;
	
	private Feature[] features;
	
	private Set<String> deletedFeatureIds;
	
	private Set<String> movedFeatureIds;
	
	private Integer incidentId;
	
	private Integer senderUserId;
	
	private Long seqTime;
	

	public Integer getSenderUserId() {
		return senderUserId;
	}

	public void setSenderUserId(Integer senderId) {
		this.senderUserId = senderId;
	}

	public Integer getIncidentId() {
		return incidentId;
	}

	public void setIncidentId(Integer incidentId) {
		this.incidentId = incidentId;
	}

	public Long getSeqTime() {
		return seqTime;
	}

	public void setSeqTime(Long seqTime) {
		this.seqTime = seqTime;
	}

	public Integer getCollabRoomId() {
		return collabRoomId;
	}

	public void setCollabRoomId(Integer collabroomId) {
		this.collabRoomId = collabroomId;
	}

	public Feature[] getFeatures() {
		return features;
	}

	public void setFeatures(Feature[] features) {
		this.features = features;
	}

	public Set<String> getDeletedFeatureIds() {
		return deletedFeatureIds;
	}

	public void setDeletedFeatureIds(Set<String> deletedFeatureIds) {
		this.deletedFeatureIds = deletedFeatureIds;
	}

	public Set<String> getMovedFeatureIds() {
		return movedFeatureIds;
	}

	public void setMovedFeatureIds(Set<String> movedFeatureIds) {
		this.movedFeatureIds = movedFeatureIds;
	}
}
