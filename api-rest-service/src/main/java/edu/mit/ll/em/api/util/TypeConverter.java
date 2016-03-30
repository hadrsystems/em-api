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
package edu.mit.ll.em.api.util;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import edu.mit.ll.em.api.entity.TaskAssignmentEvent;
import edu.mit.ll.em.api.rs.MobileEvent;
import edu.mit.ll.em.api.rs.MsgEnvelope;
import edu.mit.ll.nics.common.entity.IcsPosition;

public class TypeConverter {
	
	private static final int[] EMPTY_INT_ARRAY = {};
	
	private static Gson gson = new Gson();

	public static MsgEnvelope getMsgEnvelopeFromJson(String json)
			throws JsonParseException {
		return gson.fromJson(json, MsgEnvelope.class);
	}

	public static TaskAssignmentEvent getTaskAssignmentEventFromJson(String json)
			throws JsonParseException {
		return gson.fromJson(json, TaskAssignmentEvent.class);
	}

	public static String getJsonFromMobileEvent(MobileEvent e)
			throws JsonParseException {
		return gson.toJson(e).toString();
	}
	
	/**
	 * Convert an object of some type into a JSON string representation.
	 * For example:
	 * 			HistoryItem historyItem = new HistoryItem();
	 *			Type type = new com.google.gson.reflect.TypeToken<HistoryItem>(){}.getType();
	 * 			String json = TypeConverter.fromJSONString(historyItem, type);
	 * @param obj
	 * @param type
	 * @return
	 * @throws JsonParseException
	 */
	public static String toJSONString(Object obj, Type type)
			throws JsonParseException {
		return gson.toJson(obj, type).toString();
	}

	/**
	 * Convert a JSON string representation of some object back into its
	 * specific Object type.
	 * For example:
	 * 			HistoryItem historyItem = null;
	 *			Type type = new com.google.gson.reflect.TypeToken<HistoryItem>(){}.getType();
	 * 			historyItem = (HistoryItem) TypeConverter.fromJSONString(jsonArchiveMsg, type);
	 * @param json JSON representation of the object to be converted
	 * @param type The object's java.lang.reflection.Type
	 * @return The object
	 * @throws JsonParseException
	 */
	public static Object fromJSONString(String json, Type type)
			throws JsonParseException {
		return gson.fromJson(json, type);
	}
	
	public static Set<IcsPosition> getIcsPositions(int codes[]) {
		Set<IcsPosition> ret = new HashSet<IcsPosition>();
		for (int code : codes) {
			ICSPositionEnum e = ICSPositionEnum.valueOf(code);
			IcsPosition p = new IcsPosition();
			p.setCode(e.getCode());
			p.setDescription(e.getDescription());
			p.setName(e.name());
			ret.add(p);
		}
		return ret;
	}
	
	public static int[] getIcsPositionsAsCodeArray(Set<IcsPosition> set) {
		if (set == null || set.size() < 1) {
			return EMPTY_INT_ARRAY;
		}
		int[] ret = new int[set.size()];
		int offset = 0;
		for (IcsPosition pos : set) {
			ret[offset++] = pos.getCode();
		}
		Arrays.sort(ret);
		return ret;
	}
}
