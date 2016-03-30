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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.common.ws.client.XMLRequest;
import edu.mit.ll.nics.nicsdao.impl.CollabRoomDAOImpl;

public abstract class GetCapabilitiesExportFile extends DatalayerExportFile{
	
	public static String WMS = "wms";
	public static String WFS = "wfs";
	public static String NS = "ns";
	
	private String mapserverURL;
	private String workspaceName;
	private String exportFormat;
	private int userId;
	private int incidentId;
	
	protected XPath xPath;
	protected Document response;
	
	private static final CollabRoomDAOImpl collabRoomDao = new CollabRoomDAOImpl();
	
	/** Build a filtered getCapabilities document
	 * @param exportFormat - wms or wf
	 * @param mapserverURL - the mapserver to request information from
	 * @param workspaceName
	 * @param userId
	 * @param incident - filter only rooms in this incident
	 */
	public GetCapabilitiesExportFile(String exportFormat, String mapserverURL, String workspaceName, int userId, int incidentId){
		super("GetCapabilities");
		
		this.mapserverURL = mapserverURL;
		this.workspaceName = workspaceName;
		this.exportFormat = exportFormat;
		this.userId = userId;
		this.incidentId = incidentId;

		this.xPath = this.getXPath();
	}
	
	/** getResponse
	 * Build the getCapabilities document
	 * @return XML Document
	 */
	@Override
	public File getResponse(){
		File responseFile = null;
		try{
			//Build the request URL
			String url = this.buildGetCapsUrl(this.exportFormat);
			
			//Request the get capabilities document
			XMLRequest request = new XMLRequest(XMLRequest.DOCUMENT_FORMAT);
			Document document = (Document) request.getRequest(url.toString());
			
			//Retrieve a list of collab rooms that the user has access to
			List<CollabRoom> rooms = collabRoomDao.getAccessibleCollabRooms(userId, incidentId);
			
			//Build a response document
			this.response = this.getNewDocument(document);
			
			this.getCapabilities(document, rooms);
			
			// write the contents into xml file
			responseFile = this.createTempFile("GetCapabilities_" + this.exportFormat, ".xml");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(this.response);
			StreamResult result = new StreamResult(responseFile);
			transformer.transform(source, result);
		}catch(Exception e){
			e.printStackTrace();
		}
		return responseFile;
	}
	
	/** buildGetCapsUrl - construct the url for requesting the capabilities
	 * @param exportFormat - wms or wfs
	 * @return url
	 */
	private String buildGetCapsUrl(String exportFormat){
		StringBuffer url = new StringBuffer(this.mapserverURL);
		url.append("/");
		url.append(exportFormat);
		url.append("?service=");
		url.append(exportFormat);
		url.append("&REQUEST=getCapabilities");
		url.append("&namespace=");
		url.append(this.workspaceName);
		return url.toString();
	}
	
	/** findNodeAndAppend - find a node and import it into the response document
	 * @param doc - the document to search
	 * @param expression - the search criteria
	 * @param parent - the node to add the result to
	 * @param clone - clone all children of the node
	 * @return node
	 */
	protected Node findNodeAndAppend(Document doc, String expression, Node parent, boolean clone) throws Exception{
		Node node = (Node) this.xPath.compile(expression).evaluate(doc, XPathConstants.NODE);
		if(node != null){
			parent.appendChild(this.response.adoptNode(node.cloneNode(clone)));
		}
		return null;
	}
	
	/** findNodeListAndAppend - find a nodelist and import it into the response document
	 * @param doc - the document to search
	 * @param expression - the search criteria
	 * @param parent - the node to add the result to
	 * @param clone - clone all children of the node
	 * @return nodelist
	 */
	protected NodeList findNodeListAndAppend(Document doc, String expression, Node parent, boolean clone) throws Exception{
		NodeList nodes = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
		for(int i=0; i<nodes.getLength(); i++){
			parent.appendChild(this.response.adoptNode(nodes.item(i).cloneNode(clone)));
		}
		return nodes;
	}
	
	/** findAndAppendLayers - append the filtered list of layers to the document
	 * @param doc - the document to search
	 * @param rooms - list of accessible rooms
	 * @param layers - the node to append the layer to
	 * @return nodelist
	 */
	protected void findAndAppendLayers(Document document, List<CollabRoom> rooms, Node layers) throws Exception{
		for(CollabRoom room : rooms){
			String collabroomId = this.workspaceName + ":R" + room.getCollabRoomId();
			String expression =  "//ns:Name[text()[contains(.,'" + collabroomId +"')]]";
			
			//Find the nodes in the get capablities that match the accessible rooms
			NodeList room_layers = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			for(int i=0; i<room_layers.getLength(); i++){
				Node layer_node = room_layers.item(i);
				String name = layer_node.getTextContent();
				
				//Check for exact match or exact match + _point/polygon/line
				if(name.equals(collabroomId) || name.indexOf(collabroomId + "_") != -1){
					layers.appendChild(this.response.adoptNode(layer_node.getParentNode().cloneNode(true)));
				}
			}
		}
	}
	
	/** getNewDocument - generate a new response document 
	 * @param capabilities
	 * @param rooms
	 * @return
	 */
	protected Document getNewDocument(Document capabilities){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = (Document) docBuilder.newDocument();
			
			//Append the root node from the GetCapabilities document
			Node root_node = (Node) this.xPath.compile("/*").evaluate(capabilities, XPathConstants.NODE);
			doc.appendChild(doc.adoptNode(root_node.cloneNode(false)));
			
			return doc;
		}catch(Exception e){
			return null;
		}
	}
	
	//Build Get Capabilities Document
	abstract Document getCapabilities(Document document, List<CollabRoom> rooms);
	
	//Define XPath
	abstract XPath getXPath();
	
}