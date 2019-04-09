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
package edu.mit.ll.em.api.rs;

import java.util.ArrayList;
import java.util.Collection;

import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.FormType;

public class ReportServiceResponse {

	private Integer status;
	private String message;
		
	private Collection<Form> Reports = new ArrayList<Form>();
	private Collection<FormType> Types = new ArrayList<FormType>();
	
	// TODO: Really used for returning a count REST request; i.e., do not get
	// the list of Reports just the count.
	private int count;

	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection<Form> getReports() {
		return Reports;
	}

	public void setReports(Collection<Form> Reports) {
		this.Reports = Reports;
		if (Reports != null) {
			// Goofy, I know, but needed because this attribute is also used for returning
			// the count of Reports, without necessarily reading the Report objects.
			// So, let's keep this attribute meaningful.
			count = Reports.size();
		}
	}
	
	public Collection<FormType> getTypes() {
		return Types;
	}
	
	public void setTypes(Collection<FormType> Types) {
		this.Types = Types;
		if(Types != null) {
			count = Types.size();
		}
	}
	
	public String toString() {
		return "ReportServiceResponse [Reports=" + Reports + ", message="
				+ message + ",Types=" + Types + "]";
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}	
}

