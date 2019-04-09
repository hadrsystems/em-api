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
package edu.mit.ll.em.api.util;

import java.util.concurrent.ConcurrentHashMap;


public enum ICSPositionEnum {
	/**
	 * Various Role Types.
	 */
	IC_IC(100, "Incident Commander", "Command"),
	IC_PIO(110, "Public Information Officer", "Communications"),
	IC_LO(120, "Liaison Officer", "Communications"),
	IC_SO(130, "Safety Oficer", "Safety"),
	
	/**
	 * Incident Operation Roles.
	 */
	IO_OSC(200, "Operations Section Chief", "Operations"), 
	IO_SAM(210, "Staging Area Manager", "Operations"),
	IO_AOD(220, "Air Operations Director", "Operations"),
	IO_BD(230, "Branch Director", "Operations"),
	IO_DS(240, "Division Supervisor", "Operations"), 
	IO_GS(250, "Group Supervisor", "Operations"), 
	IO_TFM(260, "Task Force Member", "Operations"), 
	IO_STM(270, "Strike Team Member", "Operations"), 
	IO_FO(280, "Field Observer", "Operations"), 
	
	/**
	 * Incident Planning Roles.
	 */
	IP_RUL(300, "Resources Unit Leader", "Planning"),
	IP_SUL(310, "Situation Unit Leader", "Planning"),
	IP_DOCUL(320, "Documentation Unit Leader", "Planning"),
	IP_DEMOBUL(330, "Demobilization Unit Leader", "Planning"),
	IP_EUL(340, "Environmental Unit Leader", "Planning"),
	IP_MTSRUL(350,	"Maritime Transportation System Recovery Unit Leader", "Planning"),
	
	/**
	 * Incident Logistic Roles.
	 */
	IL_SVCBD(400, "Service Branch Director", "Logistics"),
	IL_CUL(410, "Communications Unit Leader", "Logistics"),
	IL_MUL(420, "Medical Unit Leader", "Logistics"),
	IL_FOODUL(430, "Food Unit Leader", "Logistics"),
	IL_SUPBD(440, "Support Branch Direction", "Logistics"),
	IL_SUL(450, "Supply Unit Leader", "Logistics"),
	IL_FACILUL(460, "Facilities Unit Leader", "Logistics"),
	IL_GSUL(470, "Ground Support Unit Leader", "Logistics"),
	IL_VSIL(480, "Vessel Support Unit Leader", "Logistics");

    private int code;
    
    private String description;
    
    private String type; 
    
    private static ConcurrentHashMap<Integer, ICSPositionEnum> codeToEnumCache
    	= new ConcurrentHashMap<Integer, ICSPositionEnum>();
    
	ICSPositionEnum() { }
	
    ICSPositionEnum(int code, String description, String type) {
        this.code = code;
        this.setDescription(description);
        this.setType(type);
    }

	public static ICSPositionEnum valueOf(int code) {
		
		ICSPositionEnum e = ICSPositionEnum.codeToEnumCache.get(code);
		if (e != null) {
			return e;
		}
		
		for (ICSPositionEnum pos : ICSPositionEnum.values()) {
			if (pos.getCode() == code) {
				ICSPositionEnum.codeToEnumCache.putIfAbsent(code, pos);
				return pos;
			}
		}
		return null;
	}
	
    public int getCode() {
		return code;
	}
    
    public void setCode(int code) {
    	this.code = code;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
};
