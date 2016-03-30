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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


public final class UserInfoValidator {
	private static final String PASSWORD_IS_EMPTY = "Please enter a password.";

	private static final String PASSWORD_REQUIREMENTS = "Your password must be a minimum 6 characters long and a maximum of 20 with at least one digit, " +
			"one upper case letter, one lower case letter and one special symbol (“@#$%-_!”)";

	private static String EMPTY = "";
	
	private static String REG_EXP = "[0-9]*[0-9]+$";
	
	private static String UPDATE_PW_ERROR_MSG = "To update password, please provide current password and new password.";
	
	private static String PW_DO_NOT_MATCH_ERROR_MSG = "Passwords do not match";
	
	private static String OLD_PW_NOT_VALID = "Old Password is not valid.";
	
	private static String NO_CHARS = " Number must not contain characters.";
	
	private static String USERNAME_LENGTH_ERROR_MSG = "Username must be at least 6 characters long";
	
	private static String USERNAME_CHAR_ERROR_MSG = "Username must not contain numbers or special characters.";
	
	private static String CELL_LABEL = "Cell Phone";
	
	private static String HOME_LABEL = "Home Phone";
	
	private static String OFFICE_LABEL = "Office Phone ";
	
	public static String SUCCESS = "success";
	
	private UserInfoValidator(){};
	
	/** validatePhoneNumbers - check to make sure phone numbers do not contain characters
	 *  @param cellPhone
	 *  @param homePhone
	 *  @param officePhone
	 *  @return boolean valid
	 */
	public static boolean validatePhoneNumbers(String cellPhone, String homePhone, String officePhone){
			if(cellPhone.length() != 0 && (cellPhone.length() != 10 || !cellPhone.matches(REG_EXP))){
	    		return false;
	    	}
	    	if(homePhone.length() != 0 && (homePhone.length() != 10 || !homePhone.matches(REG_EXP))){
	    		return false;
	    	}
	    	if(officePhone.length() != 0 && (officePhone.length() != 10 || !officePhone.matches(REG_EXP))){
	    		return false;
	    	}
	    	return true;
	    }
	/** validatePasswordUpdate - ensure the old password entered is valid and the new password
	 *  is confirmed
	 *  @param username user attempting to update password
	 *  @param admin RabbitAdmin used to determine validity of old password
	 *  @param oldPassword
	 *  @param newPassword
	 *  @param confirmPassword matches newPassword
	 *  @return boolean valid
	 */
/*	public static String validatePasswordUpdate(
			    String username, SAUtil util,
	    		String oldPassword, String newPassword, String confirmPassword){
	    	
    	if(!newPassword.equals(EMPTY) || !confirmPassword.equals(EMPTY) ||
    			!oldPassword.equals(EMPTY)){
    		
    		if(!newPassword.equals(EMPTY) && !confirmPassword.equals(EMPTY) &&
        			!oldPassword.equals(EMPTY)){
    			if(!newPassword.equals(confirmPassword)){
    				return PW_DO_NOT_MATCH_ERROR_MSG;
    			}
    	    	
    	    	final String password = util.getRabbitLogin(username);
    			if(!oldPassword.equals(password)){
    				return OLD_PW_NOT_VALID;
    			}
    		}else{
    			return UPDATE_PW_ERROR_MSG;
    		}
    	}
    	return SUCCESS;
    }*/
	/** validatePasswordUpdate - ensure the new and confirmed passwords match
	 *  @param username user attempting to update password
	 *  @param admin RabbitAdmin used to determine validity of old password
	 *  @param newPassword
	 *  @param confirmPassword matches newPassword
	 *  @return boolean valid
	 */
	public static String confirmPasswordUpdate(
			    String newPassword, String confirmPassword){
	    	
		if(!newPassword.equals(confirmPassword)){
			return PW_DO_NOT_MATCH_ERROR_MSG;
		}
    	return SUCCESS;
    }
	
	/** validatePassword- Make sure password meets minimum requirements
	 *  @param newPassword
	 *  @return boolean valid
	 */
	public static String validatePassword(String newPassword){
		if(StringUtils.isEmpty(newPassword)){
			return PASSWORD_IS_EMPTY;
		}
		
		Pattern pattern = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!_-]).{6,20})");
		Matcher matcher = pattern.matcher(newPassword);
		if(!matcher.matches()){
			return PASSWORD_REQUIREMENTS;
		}
		
	    return SUCCESS;
    }
	
	/** validateUsername - make sure the username is at least 6 characters
	 *  @param username new username
	 *  @return boolean valid
	 */
	public static boolean validateUsername(String username){
		//Pattern pattern = Pattern.compile("^[A-Za-z0-9]*[A-Za-z0-9][A-Za-z0-9]*$");
		Pattern pattern = Pattern.compile("[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
		Matcher m = pattern.matcher(username);
		
		if(!m.find()){
			return false;
		}
		return true;
	}
}
