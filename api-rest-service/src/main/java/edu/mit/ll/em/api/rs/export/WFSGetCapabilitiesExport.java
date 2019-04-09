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
package edu.mit.ll.em.api.rs.export;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.mit.ll.nics.common.entity.CollabRoom;

public class WFSGetCapabilitiesExport extends GetCapabilitiesExportFile{
	
	/** Build a filtered getCapabilities document
	 * @param exportFormat - wms or wf
	 * @param mapserverURL - the mapserver to request information from
	 * @param workspaceName
	 * @param userId
	 * @param incident - filter only rooms in this incident
	 */
	public WFSGetCapabilitiesExport(String exportFormat, String mapserverURL, String workspaceName, int userId, int incidentId){
		super(exportFormat, mapserverURL, workspaceName, userId, incidentId);
	}
	
	/**<ROOT>
	 * <ServiceIdentification/>
	 * <ServiceProvider/>
	 * <OperationsMetadata/>
	 * <FeatureTypeList/>
	 * <Filter_Capabilities/>
	 *</ROOT>
	 */
	protected Document getCapabilities(Document document, List<CollabRoom> rooms){
		Node rootNode = this.response.getFirstChild();
		
		Element layers = this.response.createElement("FeatureTypeList");
		
		try{
			this.findNodeAndAppend(document, "//ows:ServiceIdentification", rootNode, true);
			this.findNodeAndAppend(document, "//ows:ServiceProvider", rootNode, true);
			this.findNodeAndAppend(document, "//ows:OperationsMetadata", rootNode, true);
			rootNode.appendChild(layers);
			this.findNodeAndAppend(document, "//fes:Filter_Capabilities", rootNode, true);
			
			this.findAndAppendLayers(document, rooms, layers);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return this.response;
	}
	
	/** getXPath - build xpath for WFS
	 * return XPath
	 */
	protected XPath getXPath(){
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NamespaceContext nsContext = new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
            	if(prefix.equals(GetCapabilitiesExportFile.NS)){
            		return "http://www.opengis.net/wfs/2.0";
            	}
            	if(prefix.equals("ows")){
            		return "http://www.opengis.net/ows/1.1";
            	}
            	if(prefix.equals("fes")){
            		return "http://www.opengis.net/fes/2.0";
            	}
            	return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }

        };
		xPath.setNamespaceContext(nsContext);
		
		return xPath;
	}
}