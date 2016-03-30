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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;

public class TimeUtil {

	public static String getNowAsNICSDate() {
		return getTimeFromMillisAsNICSDate(getNowAsMillis());
	}
	
	public static String getTimeFromMillisAsNICSDate(long timeInMillis) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt.format(getDateFromMillis(timeInMillis));
	}		
	
	public static DateTime getNowAsDateTime() {
		DateTime now = new DateTime();
		return now;
	}
	
	public static Date getNowAsDate() {
		DateTime now = new DateTime();
		return now.toDate();
	}
	
	public static long getNowAsMillis() {
		DateTime now = new DateTime();
		return now.getMillis();
	}
	
	public static boolean snooze(int secs) {
		boolean ret = true;
		try {
			Thread.sleep(1000 * secs);
		} catch (InterruptedException e) {
			ret = false;
		}
		return ret;
	}
	
	public static Date getDateFromMillis(long millis) {
		Date date = null;
		try {
			DateTime dt = new DateTime(millis);
			date = dt.toDate();
		} catch (Exception e) {
		}
		return date;
	}
	
	public static long getMillisFromDate(Date date) {
		long millis = -1;
		if (date != null) {
			try {
				DateTime dt = new DateTime(date);
				millis = dt.getMillis();
			} catch (Exception e) {
			}
		}
		return millis;
	}
	
	public static Long getTimeInMillisFromNicsDateString(String dateString) {
		Long ret = null;
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date;
		try {
			date = (Date)formatter.parse(dateString);
			DateTime dt = new DateTime(date);
			ret = dt.getMillis();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return ret;
	}
}
