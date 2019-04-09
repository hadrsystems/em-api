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

public class WMSGetCapabilitiesExport extends GetCapabilitiesExportFile{
	
	/** Build a filtered getCapabilities document
	 * @param exportFormat - wms or wf
	 * @param mapserverURL - the mapserver to request information from
	 * @param workspaceName
	 * @param userId
	 * @param incident - filter only rooms in this incident
	 */
	public WMSGetCapabilitiesExport(String exportFormat, String mapserverURL, String workspaceName, int userId, int incidentId){
		super(exportFormat, mapserverURL, workspaceName, userId, incidentId);
	}
	
	/**<ROOT>
	 * <Service/>
	 * <Capability>
	 *   <Request/>
	 *   <Exception/>
	 *   <Layer>
	 *   	<CRS/>
	 *      <EX_GeographicBoundingBox/>
	 *   </Layer>
	 * </Capability>
	 *</ROOT>
	 */
	protected Document getCapabilities(Document document, List<CollabRoom> rooms){
		Node rootNode = this.response.getFirstChild();
		
		Element capability = this.response.createElement("Capability");
		Element layers = this.response.createElement("Layer"); //pull in title, abstract, and BB
		
		try{
			//Append to ROOT
			this.findNodeAndAppend(document, "//ns:Service", rootNode, true);
			rootNode.appendChild(capability);
			
			//Append to Capbility
			this.findNodeAndAppend(document, "//ns:Request", capability, true);
			this.findNodeAndAppend(document, "//ns:Exception", capability, true);
			capability.appendChild(layers);
			
			//Append to Layers
			this.findNodeAndAppend(document, "/ns:WMS_Capabilities/ns:Capability/ns:Layer/ns:EX_GeographicBoundingBox", layers, true);
			this.findNodeListAndAppend(document, "/ns:WMS_Capabilities/ns:Capability/ns:Layer/ns:CRS", layers, true);
			
			//Append accessible layers
			this.findAndAppendLayers(document, rooms, layers);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return this.response;
	}
	
	/** getXPath - build xpath for WMS
	 * return XPath
	 */
	protected XPath getXPath(){
		XPath xPath =  XPathFactory.newInstance().newXPath();
		
		xPath.setNamespaceContext(new NamespaceContext() {
			@Override
            public String getNamespaceURI(String prefix) {
                if(prefix.equals(GetCapabilitiesExportFile.NS)){
                	return "http://www.opengis.net/wms";
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
		});
		
		return xPath;
	}
	
}