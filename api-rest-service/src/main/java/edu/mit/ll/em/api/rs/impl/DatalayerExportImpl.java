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
package edu.mit.ll.em.api.rs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;

import edu.mit.ll.em.api.rs.DatalayerExport;
import edu.mit.ll.em.api.rs.export.DatalayerExportFile;
import edu.mit.ll.em.api.rs.export.GetCapabilitiesExportFile;
import edu.mit.ll.em.api.rs.export.KMLExportFile;
import edu.mit.ll.em.api.rs.export.ShapeExportFile;
import edu.mit.ll.em.api.rs.export.WFSGetCapabilitiesExport;
import edu.mit.ll.em.api.rs.export.WMSGetCapabilitiesExport;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.IncidentIncidentType;
import edu.mit.ll.nics.common.geoserver.api.GeoServer;
import edu.mit.ll.nics.nicsdao.impl.CollabRoomDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.IncidentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.dao.DataAccessException;

public class DatalayerExportImpl implements DatalayerExport{
	
	private static final String ALL_FEATURES = "all";
	private static final String POINT = "point";
	private static final String LINE = "line";
	private static final String POLYGON = "polygon";
	private static final String SHAPE = "shape";
	
	private static final String ERROR_FILENAME = "Export_Error";
	private static final String TYPE_ERROR = "There was an error retrieving an export file for format ";
	private static final String PERMISSION_ERROR = "You do not have permissions to download this file.";
	private static final String EXPORT_ERROR = "There was an error processing your request.";
	private static final String INFO_ERROR = "There was an error gathering information for ";	
	private static final String INVALID_TYPE_ERROR = "An invalid geometry type was provided.";
		
	private GeoServer geoserver;
	private String workspaceName;
	private String dataStoreName;
	private String mapserverURL;
	
	public Envelope maxExtent = new Envelope(-14084454.868, -6624200.909, 1593579.354, 6338790.069);
    public Envelope maxExtentLatLon = new Envelope(-126.523, -59.506, 14.169, 49.375);
    
    public static int SRID = 3857;
    public static String SRS_STRING = "EPSG:3857";
	
    private static final IncidentDAOImpl incidentDao = new IncidentDAOImpl();
    private static final CollabRoomDAOImpl collabDao = new CollabRoomDAOImpl();
    private static final UserDAOImpl userDao = new UserDAOImpl();
    
    
	public DatalayerExportImpl(){
		this.loadConfig();
	}
	
	/**
	 * getDatalayer - end point for returning a KML or Shape document
	 * @param userId - the user requesting data
	 * @param collabRoomId - the collboration room
	 * @param incidentId - the incident that the room belongs to
	 * @param type - all/point/line/polygon
	 * @param format - static/dynamic kml or shape
	 * @return Response - including a zip file with datalayer contents
	 */
	public Response getDatalayer(long userId, int collabRoomId, int incidentId, 
			String type, String format, String requestingUser) {
		
		File response = null;
		
		if(userDao.getUserId(requestingUser) != userId){
			//Export Error
			response = this.getErrorReport(PERMISSION_ERROR).getTextFile();
		}
		else{
			if(!this.isValidType(type)){
				response = this.getErrorReport(INVALID_TYPE_ERROR).getTextFile();
			}else if(this.hasPermissions(userId, incidentId, collabRoomId)){
				//Check to see if layer exists
				String layername = this.buildLayername(collabRoomId, type);
				
				if(this.geoserver.getLayer(layername, "text/plain") == null){
					//Build the requested layer
					this.createLayer(layername, type, collabRoomId);
				}
				
				response = this.getExportFile(layername, collabRoomId, incidentId, type, format);
			}else{
				//Export Error
				response = this.getErrorReport(PERMISSION_ERROR).getTextFile();
			}
			
			if(response == null){
				//Export Error
				response = this.getErrorReport(EXPORT_ERROR).getTextFile();
			}
		}
		
		return Response.ok(response, MediaType.APPLICATION_OCTET_STREAM)
			      .header("Content-Disposition", "attachment; filename=\"" + response.getName() + "\"" )
			      .build();
	}
	
	/**
	 * getCapabilities - end point for returning the capabilities for a specific incident
	 * @param userId - the user requesting data
	 * @param incidentId - the incident that the room belongs to
	 * @param format - wms or wfs
	 * @return Response - XML getCapabilities file
	 */
	public Response getCapabilities(int userId, int incidentId, String exportFormat) {
		
		File response = this.getExportFile(userId, incidentId, exportFormat);
		
		if(response == null){
			//Export Error
			response = this.getErrorReport(EXPORT_ERROR).getTextFile();
		}
		
		return Response.ok(response, MediaType.APPLICATION_OCTET_STREAM)
			      .header("Content-Disposition", "attachment; filename=\"" + response.getName() + "\"" )
			      .build();
		
	}
	
	/**
	 * getExportFile - build an export zip file (specifically for KML/Shape)
	 * @param layername - the name of the file to be downloaded
	 * @param collabRoomId - the collaboration room
	 * @param incidentId - the incident that the room belongs to
	 * @param type - all/point/line/polygon
	 * @param format - static/dynamic kml or shape
	 * @return Response - including a zip file with datalayer contents
	 */
	private File getExportFile(String layername, int collabRoomId, int incidentId, String type, String format){
		DatalayerExportFile exportFile = null;
		List<String> datalayerInfo = null;
		
		try{
			datalayerInfo = this.getDatalayerInfo(incidentId, collabRoomId, layername);
		}catch(Exception e){
			e.printStackTrace(); //There was an error gathering the incident and collabroom information
		}
		
		if(datalayerInfo != null){
			//KML Static File
			if(format.equals(KMLExportFile.STATIC)){
				exportFile = new KMLExportFile(layername, KMLExportFile.STATIC, this.mapserverURL, this.workspaceName);
			}
			//KML Dynamic File - don't allow dynamic exports for now
			/*else if(format.equals(KMLExportFile.DYNAMIC)){
				exportFile = new KMLExportFile(layername, KMLExportFile.DYNAMIC, this.mapserverURL, this.workspaceName);
			}*/
			//Shape File
			else if(format.equals(SHAPE)){
				exportFile = new ShapeExportFile(layername, this.mapserverURL);
			}
			if(exportFile == null){
				//Export Error
				exportFile = this.getErrorReport(TYPE_ERROR + format);
			}else{
				exportFile.writeToTextFile(datalayerInfo);
			}
		}else{
			StringBuffer errorStr = new StringBuffer(INFO_ERROR);
			errorStr.append("incident id");
			errorStr.append(incidentId);
			errorStr.append(" and collabrroom id ");
			errorStr.append(collabRoomId);
			//Export Error
			exportFile = this.getErrorReport(errorStr.toString());
		}
		
		return exportFile.getResponse();
	}
	
	/**
	 * getExportFile - build an export zip file (specifically for GetCapabilities)
	 * @param userId - the user requesting data
	 * @param incidentId - the incident that the room belongs to
	 * @param exportFormat - static/dynamic kml or shape
	 * @return Response - return XML document
	 */
	private File getExportFile(int userId, int incidentId, String exportFormat){
		DatalayerExportFile exportFile = null;
		
		//WMS
		if(exportFormat.equals(GetCapabilitiesExportFile.WMS)){
			exportFile = new WMSGetCapabilitiesExport(exportFormat, this.mapserverURL, this.workspaceName, userId, incidentId);
		}
		//KML Dynamic File
		else if(exportFormat.equals(GetCapabilitiesExportFile.WFS)){
			exportFile = new WFSGetCapabilitiesExport(exportFormat, this.mapserverURL, this.workspaceName, userId, incidentId);
		}
		
		if(exportFile == null){
			//Export Error
			exportFile = this.getErrorReport(TYPE_ERROR + exportFormat);
		}
		
		return exportFile.getResponse();
	}

	/**
	 * getErrorReport - build an error report file
	 * @param  message - error message
	 * @return Response - text file
	 */
	private DatalayerExportFile getErrorReport(String message){
		DatalayerExportFile exportFile = new DatalayerExportFile(ERROR_FILENAME);
		exportFile.writeToTextFile(message);
		return exportFile;
	}
	
	/**
	 * loadConfig - load configuration from the papi-svc.properties file
	 */
	private void loadConfig(){
		this.mapserverURL = APIConfig.getInstance().getConfiguration().getString(APIConfig.EXPORT_MAPSERVER_URL);
		
		this.dataStoreName = APIConfig.getInstance().getConfiguration().getString(APIConfig.EXPORT_COLLABROOM_STORE);
		
		this.workspaceName = APIConfig.getInstance().getConfiguration().getString(APIConfig.EXPORT_WORKSPACE_NAME);
		
		String mapserverUsername = APIConfig.getInstance().getConfiguration().getString(APIConfig.EXPORT_MAPSERVER_USERNAME);
		String mapserverPassword = APIConfig.getInstance().getConfiguration().getString(APIConfig.EXPORT_MAPSERVER_PASSWORD);
		
		this.geoserver = new GeoServer(mapserverURL + APIConfig.EXPORT_REST_URL, mapserverUsername, mapserverPassword);
	}
	
	/**
	 * createLayer - create layer if it does not exist
	 * @param layername - the name of the layer
	 * @param type - all/point/line/polygon
	 * @param collabRoomId - the collboration room
	 * @return boolean - layer was successfully created
	 */
	private boolean createLayer(String layername, String type, int collabroomId){
		String title = "";
		String sql = this.getSql(collabroomId, type);
		
		if (this.geoserver.addFeatureTypeSQL(this.workspaceName, this.dataStoreName, layername, SRS_STRING, sql, "the_geom", "Geometry", SRID)) {
            this.geoserver.updateLayerStyle(layername, this.workspaceName, "collabRoomStyle");
            this.geoserver.updateFeatureTypeTitle(layername, this.workspaceName, this.dataStoreName, title);
            this.geoserver.updateFeatureTypeBounds(this.workspaceName, this.dataStoreName, layername, maxExtent, maxExtentLatLon, SRS_STRING);
            this.geoserver.updateFeatureTypeEnabled(this.workspaceName, this.dataStoreName, layername, true);
            this.geoserver.updateLayerEnabled(layername, this.workspaceName, true);
            return true;
        } else {
            return false;
        }
	}
	
	/**
	 * buildLayername - build name of layer in geoserver
	 * @param collabRoomId - the collboration room
	 * @param type - all/point/line/polygon
	 * @return String - layername
	 */
	private String buildLayername(int collabroomId, String type){
		StringBuffer layername = new StringBuffer("R");
		layername.append((new Integer(collabroomId)).toString());
		if(!type.toLowerCase().equals(ALL_FEATURES)){
			layername.append("_");
			layername.append(type.toLowerCase());
		}
		return layername.toString();
	}
	
	/**
	 * getSql - get the sql for this particular view
	 * @param collabRoomId - the collboration room
	 * @param type - all/point/line/polygon
	 * @return String - SQL
	 */
	private String getSql(int collabroomId, String type){
		String sql = "SELECT f.* from Feature f, CollabroomFeature cf WHERE cf.featureid=f.featureid and cf.collabroomid=" + collabroomId;
		if(type.toLowerCase().equals(POINT)){
			sql += " and f.type='point'";
		}else if(type.toLowerCase().equals(POLYGON)){
			sql += " and f.type in('polygon','hexagon','circle','box', 'triangle')";
		}else if(type.toLowerCase().equals(LINE)){
			sql += " and f.type='sketch'";
		}
		return sql;
	}
	
	/**
	 * hasPermissions - verify the user has permissions to access room
	 * @param userId - the user requesting information
	 * @param incidentId - the incident that the collaboration room belongs to
	 * @param collabRoomId - the collaboration room
	 * @return boolean
	 */
	private boolean hasPermissions(long userId, int incidentId, int collabRoomId){
		try{
			//List<CollabRoom> rooms = CollabDAO.getInstance().getAccessibleCollabRooms(userId, incidentId);
			
			List<CollabRoom> rooms = collabDao.getAccessibleCollabRooms(userId, incidentId);
			for(Iterator<CollabRoom> itr = rooms.iterator(); itr.hasNext();){
				if(itr.next().getCollabRoomId() == collabRoomId){
					return true;
				}
			}
		}catch(Exception e){
			return false;
		}
		return false;
	}
	
	/**
	 * getDatalayerInfo - collect some information about the datalayer to include in the text file
	 * @param incidentId - the incident id
	 * @param collabroomId - the collaboration room
	 * @param layername - the layername (R + collabroomid + type)
	 * @return boolean
	 */
	private List<String> getDatalayerInfo(int incidentId, int collabRoomId, String layername) 
			throws DataAccessException, Exception {
		List<String> info = new ArrayList<String>();
				
		//Incident incident = IncidentDAO.getInstance().getIncidentById(incidentId);
		Incident incident = incidentDao.getIncident(incidentId);
		
		Set<IncidentIncidentType> types = incident.getIncidentIncidenttypes();
		Iterator<IncidentIncidentType> itr = types.iterator();
		
		info.add("Incident Name: " + incident.getIncidentname());
		
		StringBuffer typesStr = new StringBuffer("Incident Type: ");
		if(itr.hasNext()){
			typesStr.append((itr.next().getIncidentType().getIncidentTypeName()));
		}
		while(itr.hasNext()){
			typesStr.append(",");
			typesStr.append((itr.next().getIncidentType().getIncidentTypeName()));
		}
		info.add(typesStr.toString());
		String roomName = "(Failed to retrieve room name)";
		try {
			//CollabDAO.getInstance().getCollabRoom(collabRoomId).getName());
			roomName = collabDao.getCollabRoomById(collabRoomId).getName();
		} catch(Exception e) {
			System.out.println("WARNING: Failed to fetch CollabRoom name with id: " + collabRoomId);
		}
		info.add("Collaboration Room: " + roomName);
		info.add("Layer Name: " + layername);
		info.add("TIMESTAMP: " + Calendar.getInstance().getTime());
		
		return info;
	}
	
	private boolean isValidType(String type){
		if(type.toLowerCase().equals(POLYGON) ||
				type.toLowerCase().equals(LINE) ||
				type.toLowerCase().equals(POINT) ||
				type.toLowerCase().equals(ALL_FEATURES)){
			return true;
		}
		return false;
	}
}