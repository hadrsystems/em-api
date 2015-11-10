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

public class User extends APIBean {

	private int userId;
	
	private String firstName;
	
	private String lastName;
	
	private String rank;
	
	private String primaryMobilePhone;
	
	private String primaryHomePhone;
	
	private String primaryOfficePhone;
	
	private String primaryOtherPhone;
	
	private String primaryEmailAddr;
	
	private String homeBaseName;
	
	private String homeBaseStreet;
	
	private String homeBaseCity;
	
	private String homeBaseState;
	
	private String homeBaseZip;
	
	private int[] qualifiedPositions;
	
	private String radioNumber;
	
	private String agency;
	
	private int approxWeight;
	
	private String remarks;
	
	private String oldPw;
	
	private String newPw;
	
	private String jobTitle;
	
	private String jobDesc;
	
	private int sysRoleId;
	
	private int userOrgId;
	
	private String userName;
	
	public User() {
		
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		if (firstName != null && !firstName.isEmpty())		
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		if (lastName != null && !lastName.isEmpty())
			this.lastName = lastName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		
		if (other.userId != userId) {
			return false;
		}
		if (firstName == null) {
			if (other.firstName != null) {
				return false;
			}
		} else if (!firstName.equals(other.firstName)) {
			return false;
		}
		if (lastName == null) {
			if (other.lastName != null) {
				return false;
			}
		} else if (!lastName.equals(other.lastName)) {
			return false;
		}
		if (rank == null) {
			if (other.rank != null) {
				return false;
			}
		} else if (!rank.equals(other.rank)) {
			return false;
		}
		
		// TODO: Do we really need all attributes?
		
		return true;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getPrimaryMobilePhone() {
		return primaryMobilePhone;
	}

	public void setPrimaryMobilePhone(String primaryMobilePhone) {
		this.primaryMobilePhone = primaryMobilePhone;
	}

	public String getPrimaryHomePhone() {
		return primaryHomePhone;
	}

	public void setPrimaryHomePhone(String primaryHomePhone) {
		this.primaryHomePhone = primaryHomePhone;
	}

	public String getPrimaryEmailAddr() {
		return primaryEmailAddr;
	}

	public void setPrimaryEmailAddr(String primaryEmailAddr) {
		this.primaryEmailAddr = primaryEmailAddr;
	}

	public String getHomeBaseStreet() {
		return homeBaseStreet;
	}

	public void setHomeBaseStreet(String homeBaseStreet) {
		this.homeBaseStreet = homeBaseStreet;
	}

	public String getHomeBaseCity() {
		return homeBaseCity;
	}

	public void setHomeBaseCity(String homeBaseCity) {
		this.homeBaseCity = homeBaseCity;
	}

	public String getHomeBaseState() {
		return homeBaseState;
	}

	public void setHomeBaseState(String homeBaseState) {
		this.homeBaseState = homeBaseState;
	}

	public String getHomeBaseZip() {
		return homeBaseZip;
	}

	public void setHomeBaseZip(String homeBaseZip) {
		this.homeBaseZip = homeBaseZip;
	}

	public int[] getQualifiedPositions() {
		return qualifiedPositions;
	}

	public void setQualifiedPositions(int[] qualifiedPositions) {
		this.qualifiedPositions = qualifiedPositions;
	}

	public int getApproxWeight() {
		return approxWeight;
	}

	public void setApproxWeight(int approxWeight) {
		this.approxWeight = approxWeight;
	}

	public String getHomeBaseName() {
		return homeBaseName;
	}

	public void setHomeBaseName(String homeBaseName) {
		this.homeBaseName = homeBaseName;
	}

	public String getPrimaryOfficePhone() {
		return primaryOfficePhone;
	}

	public void setPrimaryOfficePhone(String primaryOfficePhone) {
		this.primaryOfficePhone = primaryOfficePhone;
	}

	public String getPrimaryOtherPhone() {
		return primaryOtherPhone;
	}

	public void setPrimaryOtherPhone(String primaryOtherPhone) {
		this.primaryOtherPhone = primaryOtherPhone;
	}

	public String getRadioNumber() {
		return radioNumber;
	}

	public void setRadioNumber(String radioNumber) {
		this.radioNumber = radioNumber;
	}	
	
	public String getOldPw() {
		return oldPw;
	}

	public void setOldPw(String oldPw) {
		this.oldPw = oldPw;
	}
	
	public String getNewPw() {
		return newPw;
	}

	public void setNewPw(String newPw) {
		this.newPw = newPw;
	}
	
	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	
	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}
	
	public int getSysRoleId() {
		return sysRoleId;
	}

	public void setSysRoleId(int sysRoleId) {
		this.sysRoleId = sysRoleId;
	}
	
	public int getUserOrgId() {
		return userOrgId;
	}

	public void setUserOrgId(int userOrgId) {
		this. userOrgId =  userOrgId;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName =  userName;
	}
}
