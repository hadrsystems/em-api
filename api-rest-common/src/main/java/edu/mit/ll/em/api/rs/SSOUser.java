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

import java.util.HashMap;
import java.util.Map;

public class SSOUser extends APIBean {
	
	/** email used as the username for the SSO user */
	private String email;
	
	/** password for user's SSO account */
	private String password;
	
	/** User's first name */
	private String firstName;
	
	/** User's last name */
	private String lastName;
	
	/** Realm on SSO system the user is associated with */
	private String realm;
	
	/** User's SSO attributes */
	private Map<String, String> attributes;

	/**
	 * Default constructor
	 */
	public SSOUser() {
		realm = "/";
	}
	
	/**
	 * Short constructor. Realm is defaulted to "/".
	 * 
	 * @param email user's email address
	 * @param password user's password
	 */
	public SSOUser(String email, String password) {
		this.email = email;
		this.password = password;
		this.realm = "/";
	}	
	
	
	/**
	 * Long constructor. All fields specified except for attributes.
	 * 
	 * @param email user's email address
	 * @param password user's password
	 * @param firstName user's first name
	 * @param lastName user's last name
	 * @param realm realm the user is associated with
	 */
	public SSOUser(String email, String password, String firstName, String lastName, String realm) {
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.realm = realm;
	}
	
	
	/**
	 * Short constructor with specified realm
	 * 
	 * @param email user's email address
	 * @param password user's password
	 * @param firstName user's first name
	 * @param lastName user's last name
	 */
	public SSOUser(String email, String password, String firstName, String lastName) {
		this(email, password, firstName, lastName, "/");
	}

	
	/**
	 * Add a single attribute to the user
	 * 
	 * @param key the name of the attribute
	 * @param value the value of the attribute
	 * 
	 * @return true if successfully added, false if the key or value specified is null or empty
	 */
	public boolean addAttribute(String key, String value) {
		
		if(key == null || key.isEmpty() || value == null || value.isEmpty()) {
			return false;
		}
		
		if(attributes == null) {
			attributes = new HashMap<String, String>();
		}
		
		attributes.put(key, value);
		
		return true;
	}
	
	
	/**
	 * Gets the value of the attribute associated with the specified key
	 * 
	 * @param key the key specifying the name of the desired attribute value
	 * 
	 * @return The value of the specified attribute if successful, false if there was
	 * 		   an authorization issue, or if the attribute was not found
	 */
	public String getAttribute(String key) {
		if(attributes == null) {
			return null;
		}
		
		return attributes.get(key);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SSOUser:").append("\n\temail: " + getEmail())
			.append("\n\tpassword: " + ((getPassword() == null) ? "null" : "********"))
			.append("\n\tfirstName: " + getFirstName())
			.append("\n\tlastName: " + getLastName())
			.append("\n\trealm: " + getRealm())
			.append("\n\tattributes: ");
		
		if(attributes == null) {
			sb.append("null");
		} else {
			for(String key : attributes.keySet()) {
				sb.append("\n\t\t" + key + ":" + attributes.get(key));
			}
		}
		
		sb.append("\n");
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSOUser other = (SSOUser) obj;
		
		if (other.email != email) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
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
		if (realm == null) {
			if (other.realm != null) {
				return false;
			}
		} else if(!realm.equals(other.realm)) {
			return false;
		}
				
		// TODO:SSO bother with comparing all attributes?
		
		return true;
	}
	
	
	// Getters and Setters
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public final Map<String, String> getAttributes() {
		return attributes;
	}

	public final void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
		
}
