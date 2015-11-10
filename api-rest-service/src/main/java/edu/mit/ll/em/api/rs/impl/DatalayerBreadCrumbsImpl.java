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

import com.vividsolutions.jts.geom.Envelope;

import edu.mit.ll.em.api.rs.DatalayerBreadCrumbs;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.nics.common.geoserver.api.GeoServer;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class DatalayerBreadCrumbsImpl implements DatalayerBreadCrumbs{
	
	private GeoServer geoserver;
	private String workspaceName;
	private String dataStoreName;
	private String mapserverURL;
	
	public Envelope maxExtent = new Envelope(-14084454.868, -6624200.909, 1593579.354, 6338790.069);
    public Envelope maxExtentLatLon = new Envelope(-126.523, -59.506, 14.169, 49.375);
    
    public static int SRID = 3857;
    public static String SRS_STRING = "EPSG:3857";
	
	public DatalayerBreadCrumbsImpl(){
		this.loadConfig();
	}

	@Override
	public Response getBreadcrumbs(String id, String table, String interval) {
		id = this.getValueWithSpaces(id);
		table = this.getValueWithSpaces(table);
		interval = this.getValueWithSpaces(interval);
		
		//Replace spaces for layer name
		String layername = id.replace("-", "_").replace(" ", "_") + "_" + interval.replace(" ", "_");
		
		System.out.println("LAYERNAME: " + layername);
		if(this.geoserver.getLayer(layername, "text/plain") == null){
			//Build the requested layer
			this.createLayer(layername, id, table, interval);
		}
		return Response.ok(layername).status(Status.OK).build();
	}
	
	private String getValueWithSpaces(String value){
		return value.replace("$", " ");
	}
	
	/**
	 * loadConfig - load configuration from the papi-svc.properties file
	 */
	private void loadConfig(){
		this.mapserverURL = APIConfig.getInstance().getConfiguration().getString(APIConfig.EXPORT_MAPSERVER_URL);
		
		//this.dataStoreName = PAPIConfig.getInstance().getConfiguration().getString(COLLABROOM_STORE);
		
		this.dataStoreName = "datafeeds";
		
		//this.workspaceName = PAPIConfig.getInstance().getConfiguration().getString(WORKSPACE_NAME);
		
		this.workspaceName = "nics";
		
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
	private boolean createLayer(String layername, String id, String table, String interval){
		String title = "";
		String sql = this.getSql(id, table, interval);
		
		if (this.geoserver.addFeatureTypeSQL(this.workspaceName, this.dataStoreName, layername, SRS_STRING, sql, "geom", "Geometry", SRID)) {
            this.geoserver.updateLayerStyle(layername, this.workspaceName, "point");
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
	 * getSql - get the sql for this particular view
	 * @param collabRoomId - the collboration room
	 * @param type - all/point/line/polygon
	 * @return String - SQL
	 */
	private String getSql(String id, String table, String interval){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * from ");
		sql.append(table);
		sql.append(" where name='");
		sql.append(id);
		sql.append("' and timestamp >= (now() - interval '");
		sql.append(interval);
		sql.append("')");
		return sql.toString();
	}
}