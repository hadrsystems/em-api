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
package edu.mit.ll.em.api.rs;

import java.util.ArrayList;
import java.util.Collection;

import edu.mit.ll.nics.common.entity.DeletedFeature;

public class MapMarkupServiceResponse {

	private String message;
	
	private Collection<MapMarkup> MapMarkups = new ArrayList<MapMarkup>();
	private Collection<DeletedFeature> deleted = new ArrayList<DeletedFeature>();
	
	// TODO: Really used for returning a count REST request; i.e., do not get
	// the list of MapMarkups just the count.
	private int count;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection<MapMarkup> getMapMarkups() {
		return MapMarkups;
	}

	public void setMapMarkups(Collection<MapMarkup> MapMarkups) {
		this.MapMarkups = MapMarkups;
		if (MapMarkups != null) {
			// Goofy, I know, but needed because this attribute is also used for returning
			// the count of MapMarkups, without necessarily reading the MapMarkup objects.
			// So, let's keep this attribute meaningful.
			count = MapMarkups.size();
		}
	}

	
	public Collection<DeletedFeature> getDeleted() {
		return deleted;
	}

	public void setDeleted(Collection<DeletedFeature> deleted) {
		this.deleted = deleted;
	}

	public String toString() {
		return "MapMarkupServiceResponse [MapMarkups=" + MapMarkups + ", message="
				+ message + "]";
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}	
}

