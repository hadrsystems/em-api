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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.nicsdao.query.QueryConstraint;

public class QueryConstraintHelper {
	
	private static final String CNAME = QueryConstraintHelper.class.getName();
	
	public static QueryConstraint.UTCRange makeDateRange(QueryConstraintParms opts) {
		if(opts != null && opts.getDateColumn() != null && opts.getFromDate() != null){
			QueryConstraint.UTCRange ret = new QueryConstraint.UTCRange(
					opts.getDateColumn(), opts.getFromDate(), opts.getToDate());
			return ret;
		}
		return null;
	}
	
	public static QueryConstraint.ResultSetPage makeResultSetRange(QueryConstraintParms opts) {
		QueryConstraint.ResultSetPage ret = new QueryConstraint.ResultSetPage(
				opts.getOffset(), opts.getLimit());
		return ret;
	}
	
	public static QueryConstraint.OrderBy makeOrderBy(QueryConstraintParms opts) {
		QueryConstraint.OrderByType type = null;
		if (opts.getSortOrder() == null ||
			QueryConstraint.OrderByType.ASC.toString().equalsIgnoreCase(opts.getSortOrder())) {
			type = QueryConstraint.OrderByType.ASC;
		} else if (QueryConstraint.OrderByType.DESC.toString().equalsIgnoreCase(opts.getSortOrder())) {
			type = QueryConstraint.OrderByType.DESC;
		} else {
			APILogger.getInstance().w(CNAME, "Unsupported sort order ["
					+ opts.getSortOrder() + "]. Switched to "
					+ QueryConstraint.OrderByType.ASC.toString());
			type = QueryConstraint.OrderByType.ASC;			
		}
		QueryConstraint.OrderBy ret = new QueryConstraint.OrderBy(
				opts.getSortByColumn(), type);
		return ret;
	}
	
	public static Set<String> makeColumnSelectionSet(QueryConstraintParms opts) {
		Set<String> ret = null;		
		String s = opts.getFields();
		if (s == null || s.isEmpty()) {
			ret = new LinkedHashSet<String>();
		} else {
			String fieldsArray[] = org.apache.commons.lang.StringUtils.split(s,",");
			ret = new LinkedHashSet<String>(Arrays.asList(fieldsArray));
		}
		return ret;
	}
	
	public static Map<String, Object> parseOptions(QueryConstraintParms parms) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put(QueryConstraint.KEY_DATE_RANGE, makeDateRange(parms));
		ret.put(QueryConstraint.KEY_ORDER_BY, makeOrderBy(parms));
		ret.put(QueryConstraint.KEY_RESULTSET_RANGE, makeResultSetRange(parms));
		ret.put(QueryConstraint.KEY_COLUMN_SELECTION, makeColumnSelectionSet(parms));		
		return ret;
	}
}
