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

import java.util.ArrayList;
import java.util.Collection;


public class CollabRoomPermissionResponse {

	private String message;
	
	//Collab Room
	private Collection<?> results = new ArrayList();
	
	private Collection<Integer> failedAdmin = new ArrayList<Integer>();
	private Collection<Integer> failedReadWrite = new ArrayList<Integer>();
	
	private Collection<Integer> adminUsers = new ArrayList<Integer>();
	private Collection<Integer> readWriteUsers = new ArrayList<Integer>();
	
	private long count;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Collection<?> getResults() {
		return results;
	}
	
	public void setResults(Collection<?> results) {
		this.results = results;
	}

	public Collection<Integer> getFailedAdmin() {
		return failedAdmin;
	}

	public void setFailedAdmin(Collection<Integer> failedAdmin) {
		this.failedAdmin = failedAdmin;
	}
	
	public Collection<Integer> getFailedReadWrite() {
		return failedReadWrite;
	}

	public void setFailedReadWrite(Collection<Integer> failedReadWrite) {
		this.failedReadWrite = failedReadWrite;
	}
	
	public void addFailedAdmin(int userId){
		this.failedAdmin.add(userId);
	}
	
	public void addFailedReadWrite(int userId){
		this.failedReadWrite.add(userId);
	}
	
	public void addAdminUser(int userId){
		this.adminUsers.add(userId);
	}
	
	public void addReadWriteUser(int userId){
		this.readWriteUsers.add(userId);
	}
	
	public Collection<Integer> getAdminUsers() {
		return adminUsers;
	}

	public void setAdminUsers(Collection<Integer> adminUsers) {
		this.adminUsers = adminUsers;
	}
	
	public Collection<Integer> getReadWriteUsers() {
		return readWriteUsers;
	}

	public void setReadWriteUsers(Collection<Integer> readWriteUsers) {
		this.readWriteUsers = readWriteUsers;
	}
	
	@Override
	public String toString() {
		return "CollabServiceResponse [message=" + message + ","
				+ "results=" + results + ","
				+ "readWriteUsers=" + readWriteUsers + ","
				+ "adminUsers=" + adminUsers + ","
				+ "failedReadWrite=" + failedReadWrite + ","
				+ "failedAdmin=" + failedAdmin + "]";
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}	
}
