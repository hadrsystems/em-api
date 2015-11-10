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

/**
 * Query related options.
 * @author SA23148
 *
 */
public class QueryConstraintParms {
	
	/*
	 * Constraint the dates in the result data set.
	 */
	private Long fromDate;
	
	private Long toDate;
	
	private String dateColumn;
	
	
	/*
	 * Constraint the size of the result data set. Can be used for paging through result set.
	 * Note: The query optimizer takes LIMIT into account when generating a
	 * query plan, so you are very likely to get different plans (yielding 
	 * different row orders) depending on what you give for LIMIT and OFFSET. 
	 * Thus, using different LIMIT/OFFSET values to select different subsets
	 * of a query result will give inconsistent results unless you enforce a
	 * predictable result ordering with ORDER BY. This is not a bug; it is an
	 * inherent consequence of the fact that SQL does not promise to deliver
	 * the results of a query in any particular order unless ORDER BY is used
	 * to constrain the order.   
	 */
	private Integer offset;
	
	private Integer limit;
	
	
	/*
	 * Sorting by some specific column.
	 */
	private String sortOrder;
	
	private String sortByColumn;

	
	/*
	 * Constraint the select clause; i.e., columns to be returned.
	 * Expected to be a comma separated list of column names.
	 */
	private String fields;
	

	public Long getFromDate() {
		return fromDate;
	}

	public Long getToDate() {
		return toDate;
	}

	public void setFromDate(Long fromDate) {
		this.fromDate = fromDate;
	}

	public void setToDate(Long toDate) {
		this.toDate = toDate;
	}

	public Integer getOffset() {
		return offset;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getSortByColumn() {
		return sortByColumn;
	}

	public void setSortByColumn(String sortByColumn) {
		this.sortByColumn = sortByColumn;
	}

	public String getDateColumn() {
		return dateColumn;
	}

	public void setDateColumn(String dateColumn) {
		this.dateColumn = dateColumn;
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}
}
