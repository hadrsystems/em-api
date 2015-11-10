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
package edu.mit.ll.em.api.rs.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import edu.mit.ll.em.api.util.APIConfig;

public class KMLExportFile extends DatalayerExportFile{
	
	private static String KML_ATTRIBS = "&bbox=-179,-89,179,89&layers=";
	
	private String mapserverURL;
	private String workspace;
	private String exportType;
	private String layername;
	private String kmlTemplate;
	
	public static String DYNAMIC = "dynamic";
	public static String STATIC = "static";
	
	private static final String DYNAMIC_KML_TEMPLATE_ERROR = "The dynamic kml template is not configured properly";
	
	/* KMLExport File
	 * Represents the KML Export of a Collaboration Room (Static)
	 */
	public KMLExportFile(String layername, String type, String mapserverURL, String workspace){
		super(layername);
		
		this.mapserverURL = mapserverURL;
		this.workspace = workspace;
		this.exportType = type;
		this.layername = layername;
		
		/*try{
			this.kmlTemplate = FileUtils.readFileToString(new File(
					APIConfig.getInstance().getConfiguration().getString(APIConfig.KML_TEMPLATE_PATH)));
		}catch(Exception e){
			e.printStackTrace();
		}*/
	}
	
	/* getReponse
	 * Request and add the KML file or the dynamic URL
	 * @return The zip file containing the text file (and) KML
	 */
	@Override
	public File getResponse(){
		if(this.exportType.toLowerCase().equals(STATIC)){
			StringBuffer url = new StringBuffer(mapserverURL);
			url.append(APIConfig.getInstance().getConfiguration().getString(APIConfig.KML_EXPORT_URL));
			url.append(KML_ATTRIBS);
			url.append(this.workspace);
			url.append(":");
			url.append(this.layername);
			
			File doc = this.addFile(this.requestLayer(url.toString()), layername, ZIP);
			if(doc == null){
				this.writeToTextFile("There was an error loading the requested KML document.");
				return this.getTextFile();
			}
		}else if(this.exportType.toLowerCase().equals(DYNAMIC)){
			this.addFile(this.buildDynamicKML(layername));
		}
		return this.getZipFile();
	}
	
	/* buildDynamicKML
	 * Build a KML document to dynamic layer information
	 * @param layername
	 * @return KML
	 */
	private File buildDynamicKML(String layername){
		File file = null;
		String kml = null;
		//geoserverUrl.replaceAll("/geoserver/rest", "")
		if(this.kmlTemplate != null){
			kml = this.kmlTemplate.replaceAll("WORKSPACENAME", this.workspace).replaceAll("LAYERNAME", layername).replaceAll("MAPSERVERURL", this.mapserverURL);
	    }else{
	    	kml = DYNAMIC_KML_TEMPLATE_ERROR;
	    }
		//Write either template or error to file
		try {
			file = this.createTempFile(layername, KML);
			BufferedWriter writer = null;
		       
			writer = new BufferedWriter(new FileWriter(file));
            writer.write(kml);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
		return file;
	}
}