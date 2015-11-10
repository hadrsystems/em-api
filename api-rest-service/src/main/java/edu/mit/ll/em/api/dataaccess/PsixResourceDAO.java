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
package edu.mit.ll.em.api.dataaccess;

import java.util.Set;

import edu.mit.ll.em.api.rs.PsixResource;


public class PsixResourceDAO extends BaseDAO {
	
	private static final String CNAME = PsixResourceDAO.class.getName();

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static PsixResourceDAO instance = new PsixResourceDAO();
	}

	public static PsixResourceDAO getInstance() {
		return Holder.instance;
	}
		
	public PsixResource createPsixResource(PsixResource psixResource) throws ICSDatastoreException {
		if (psixResource == null) {
			throw new NullPointerException(CNAME + ":createPsixResource called with null psixResource argument");
		}
		// TODO: Insert implementation here.
		return psixResource;
	}	
	
	public Set<Integer> getAllPsixResourceIds() {
		Set<Integer> ret = null;
		// TODO: Insert implementation here. 
		return ret;
	}

	public PsixResource getPsixResourceById(int id) {
		PsixResource ret = null;
		// TODO: Insert implementation here. 
		return ret;
	}

	public void removePsixResource(int id) throws ICSDatastoreException {
		// TODO: Insert implementation here. 
	}
	
	public PsixResource updatePsixResource(int psixResourceId, PsixResource other) throws ICSDatastoreException {
		PsixResource ret = null;
		if (other == null) {
			throw new NullPointerException(CNAME + ":updatePsixResource called with null psixResource argument");
		}
		// TODO: Insert implementation here.
		return ret;
	}

	public int getPsixResourceCount() {
		int count = -1;
		// TODO: Insert implementation here.
		return count;
	}
}


