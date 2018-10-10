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

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;

public class APIConfig {

	private static final String CNAME = APIConfig.class.getName();
	
	
	public static final String API_CONFIG_XML = "em-api-config.xml";
	public static final String API_PROPS_FILE = "em-api.properties";
	
	public static final String CACHE_USER_REFRESHMINUTES = "em.api.cache.user.refreshminutes";
	
	public static final String NEW_USER_ALERT_EMAIL = "em.api.user.alert.email";
	public static final String NEW_USER_ENABLED_EMAIL = "em.api.newuser.enabled.email";
	public static final String NEW_USER_BODY_EMAIL = "em.api.newuser.enabled.body";
	public static final String NEW_USER_BODY_TEMPLATE = "em.api.newuser.enabled.template";
	public static final String NEW_USER_BODY_SUBJECT = "em.api.newuser.enabled.subject";
	public static final String NEW_INCIDENT_USERS_EMAIL = "em.api.new.incident.emails";
	public static final String NEW_REGISTERED_USER_EMAIL = "em.api.new.registered.users.emails";
	public static final String EMAIL_ALERT_TOPIC = "em.api.alert.topic";
	public static final String SYSTEM_ADMIN_ALERT_EMAILS = "em.api.sysadmin.alert.emails";
	
	public static final String RABBIT_HOSTNAME_KEY = "em.api.rabbitmq.hostname";
	public static final String RABBIT_USERNAME_KEY = "em.api.rabbitmq.username";
	public static final String RABBIT_USERPWD_KEY = "em.api.rabbitmq.userpwd";
	public static final String RABBIT_EXCHANGENAME_KEY = "em.api.exchange.name";
	public static final String RABBIT_MAX_CONN_TRIES = "em.api.rabbitmq.maxconntries";
	public static final String RABBIT_FAILOVER_HOSTNAME = "em.api.rabbitmq.failover.hostname";
	public static final String RABBIT_BINDING_KEYS = "em.api.rabbitmq.bindingkeys";
	public static final String RABBIT_MSG_VERSION = "em.api.rabbitmq.msgver";
	
	public static final String CHAT_STALEMSG_FACTOR_STRING = "em.api.resource.chat.stalemsg.factor.string";
	public static final String CHAT_STALEMSG_FACTOR_MINS = "em.api.resource.chat.stalemsg.factor.mins";
	
	public static final String MDT_TOPIC = "em.api.service.mdt.topic";
	public static final String MDT_NICS_SCHEMA_URI = "em.api.service.mdt.nicsSchemaLocationURI";
	public static final String MDT_TYPE_NAME = "em.api.service.mdt.typeName";
	public static final String MDT_SRS_NAME = "em.api.service.mdt.srsName";
	public static final String MDT_WFS_SCHEMA_URI = "em.api.service.mdt.wfsSchemaURI";
	public static final String MDT_WFS_SERVICE_URI = "em.api.service.mdt.wfsServiceURI";
	
	public final static String DB_MAX_ROWS = "em.api.db.get.maxrows";
	
	// Reports
	public static final String REPORTS_SR_STORAGEPATH = "em.api.resource.report.sr.storagepath";
	public static final String REPORTS_SR_URL = "em.api.resource.report.sr.url";
	public static final String REPORTS_SR_PATH = "em.api.resource.report.sr.path";
	
	public static final String REPORTS_DR_STORAGEPATH = "em.api.resource.report.dmgrpt.storagepath";
	public static final String REPORTS_DR_URL= "em.api.resource.report.dmgrpt.url";
	public static final String REPORTS_DR_PATH = "em.api.resource.report.dmgrpt.path";

	public static final String REPORTS_UXO_STORAGEPATH = "em.api.resource.report.uxo.storagepath";
	public static final String REPORTS_UXO_URL= "em.api.resource.report.uxo.url";
	public static final String REPORTS_UXO_PATH = "em.api.resource.report.uxo.path";
	
	// FILE UPLOAD
	public static final String FILE_UPLOAD_URL = "em.api.service.file.upload.url";
	public static final String FILE_UPLOAD_PATH = "em.api.service.file.upload.path";
	public static final String KML_UPLOAD_PATH = "em.api.service.kml.upload.path";
	public static final String KMZ_UPLOAD_PATH = "em.api.service.kmz.upload.path";
	public static final String KMZ_TMP_UPLOAD_PATH = "em.api.service.kmz.tmp.upload.path";
	public static final String GPX_UPLOAD_PATH = "em.api.service.gpx.upload.path";
	public static final String JSON_UPLOAD_PATH = "em.api.service.geojson.upload.path";
	
	// KML Export
	public static final String KML_EXPORT_URL = "em.api.service.export.kmlExportURL";
	public static final String KML_TEMPLATE_PATH = "em.api.service.export.kmlTemplatePath";
	
	// DataLayerBreadCrumbs
	public static final String EXPORT_MAPSERVER_URL = "em.api.service.export.mapserverURL";
	public static final String EXPORT_MAPSERVER_USERNAME = "em.api.service.export.mapserverUsername";
	public static final String EXPORT_MAPSERVER_PASSWORD = "em.api.service.export.mapserverPassword";
	public static final String EXPORT_COLLABROOM_STORE = "em.api.service.export.collabroomStore";
	public static final String EXPORT_WORKSPACE_NAME = "em.api.service.export.workspaceName";
	public static final String EXPORT_REST_URL = "/rest";
	public static final String EXPORT_WEBSERVER_URL = "em.api.service.export.webserverURL";
	
	public static final String IMPORT_SHAPEFILE_WORKSPACE = "em.api.service.import.shapefileWorkspace";
	public static final String IMPORT_SHAPEFILE_STORE = "em.api.service.import.shapefileStore";


	public static final String INCIDENT_MAP = "em.api.collabroom.incident.map";
	
	// Passwords
	public static final String PASSWORD_PATTERN = "em.api.password.pattern";
	public static final String PASSWORD_REQUIREMENTS = "em.api.password.requirements";
    public static final String GEOCODE_API_URL = "geocode.api.url";
    public static final String GEOCODE_API_KEY = "geocode.api.key";
	
	private Configuration config;
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static APIConfig instance = new APIConfig();
	}
	
	public static APIConfig getInstance() {
		return Holder.instance;
	}
	
	public Configuration getConfiguration() {
		return config;
	}
	
	protected APIConfig() {
		loadConfig();
	}

	private void loadConfig() {
		DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
		builder.setFile(new File(API_CONFIG_XML));	
		try {
			config = builder.getConfiguration(true);
			System.out.println("After config build");
		} catch (ConfigurationException e) {
			String msg = "Could not find/read initialization file " + 
					API_CONFIG_XML + "Error: " +
					e.getCause().getLocalizedMessage();
			APILogger.getInstance().e(CNAME, msg);
			throw new ExceptionInInitializerError(msg);
		}	
	}	
}
