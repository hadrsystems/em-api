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

import java.util.Collection;

import edu.mit.ll.nics.common.entity.IncidentType;

public class UserProfileResponse {
	
	private String message;
	
	private Collection<IncidentType> incidentTypes;
	
	private int userOrgId;
	
	private int orgId;
	
	private String username;
	
	private String orgName;

	private String orgPrefix;
	
	private int workspaceId;
	
	private int usersessionId;
	
	private int userId;
	
	private String userFirstname;
	
	private String userLastname;
	
	private String jobTitle;
	
	private String rank;
	
	private String description;
	
	private int sysRoleId;
	
	private boolean isSuperUser;
	
	private boolean isAdminUser;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "UserProfileResponse [incidentType=" + incidentTypes + 
				", userOrgId=" + userOrgId + 
				", orgId=" + orgId + 
				", username=" + username + 
				", orgName=" + orgName + 
				", message=" + message + 
				", isSuperUser=" + isSuperUser + 
				", isAdminUser=" + isAdminUser + 
		"]";
	}	
	
	public void setIsSuperUser(boolean isSuperUser){
		this.isSuperUser = isSuperUser;
	}
	
	public boolean getIsSuperUser(){
		return isSuperUser;
	}
	
	public void setIsAdminUser(boolean isAdminUser){
		this.isAdminUser = isAdminUser;
	}
	
	public boolean getIsAdminUser(){
		return isAdminUser;
	}
	
	public void setIncidentTypes(Collection<IncidentType> incidentTypes){
		this.incidentTypes = incidentTypes;
	}
	
	public Collection<IncidentType> getIncidentTypes(){
		return this.incidentTypes;
	}
	
	public void setUserOrgId(int userOrgId){
		this.userOrgId = userOrgId;
	}
	
	public int getUserOrgId(){
		return this.userOrgId;
	}
	
	public void setOrgId(int orgId){
		this.orgId = orgId;
	}
	
	public int getOrgId(){
		return this.orgId;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public void setOrgName(String orgName){
		this.orgName = orgName;
	}
	
	public String getOrgName(){
		return this.orgName;
	}
	
	public void setOrgPrefix(String prefix){
		this.orgPrefix = prefix;
	}
	
	public String getOrgPrefix(){
		return this.orgPrefix;
	}
	
	public void setWorkspaceId(int workspaceId){
		this.workspaceId = workspaceId;
	}
	
	public int getWorkspaceId(){
		return this.workspaceId;
	}
	
	public void setUsersessionId(int usersessionId){
		this.usersessionId = usersessionId;
	}
	
	public int getUsersessionId(){
		return this.usersessionId;
	}
	
	public void setUserId(int userId){
		this.userId = userId;
	}
	
	public int getUserId(){
		return this.userId;
	}

	public void setUserFirstname(String firstname) {
		this.userFirstname = firstname;
	}
	
	public String getUserFirstname() {
		return this.userFirstname;
	}

	public void setUserLastname(String lastname) {
		this.userLastname = lastname;
	}
	
	public String getUserLastname() {
		return this.userLastname;
	}
	
	public void setRank(String rank) {
		this.rank = rank;
	}
	
	public String getRank() {
		return this.rank;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	
	public String getJobTitle() {
		return this.jobTitle;
	}
	
	public void setSysRoleId(int sysRoleId) {
		this.sysRoleId = sysRoleId;
	}
	
	public int getSysRoleId() {
		return this.sysRoleId;
	}
}
