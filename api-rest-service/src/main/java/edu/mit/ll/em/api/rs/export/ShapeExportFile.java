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
package edu.mit.ll.em.api.rs.export;

import java.io.File;

public class ShapeExportFile extends DatalayerExportFile{
	
	private String layername;
	private String mapserverURL;
	
	private static final String WFS_MAPSERVER_URL = "/wfs";
	private static final String SHAPE_URL = "?service=WFS&version=1.0.0&request=GetFeature&outputFormat=SHAPE-ZIP&srsName=EPSG:4326&typeName=";
	
	/* ShapeExport File
	 * Represents the Shape Export of a Collaboration Room (Static)
	 */
	public ShapeExportFile(String layername, String mapserverURL){
		super(layername);
		this.layername = layername;
		this.mapserverURL = mapserverURL;
	}
	
	/* getReponse
	 * @return - a zip file containing the shape zip file
	 */
	@Override
	public File getResponse(){
		StringBuffer url = new StringBuffer(mapserverURL);
		url.append(WFS_MAPSERVER_URL);
		url.append(SHAPE_URL);
		url.append(layername);
		
		File doc = this.addFile(this.requestLayer(url.toString()), layername, ZIP);
		if(doc == null){
			this.writeToTextFile("There was an error retrieving the document.");
			return this.getTextFile();
		}
		
		return this.getZipFile();
	}
}