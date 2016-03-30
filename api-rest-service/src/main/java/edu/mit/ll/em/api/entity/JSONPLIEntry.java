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
package edu.mit.ll.em.api.entity;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * NICS JSON PLI Object, with support to convert to NICS 
 * compliant GML ready for geodatafeed-consumer
 *
 */
public class JSONPLIEntry {
	private static Logger LOG = Logger.getLogger(JSONPLIEntry.class);

    /** ID */
	protected String id;

	/** feed name */
	protected String typeName;

	protected String name;

    protected String description;

    protected String srsName;

    protected String coordinates;

    protected String speed;

    protected String course;

    protected String timestamp;

    protected String version;

    protected String extended;

    protected static String nicsSchemaLocationURI;

    protected static String wfsServiceURI;

    protected static String wfsSchemasURI;

	/**
	 * Default Constructor
	 */
	public JSONPLIEntry() {

	}

	/**
	 * Converts to XML using all fields
	 * 
	 * @param format Set to true to enable formatting
	 * 
	 * @return
	 */
	public String toGML(boolean format) {
		return toGML(null, format);
	}
	
	
	/**
	 * Converts to XML, including only the specified fields
	 * 
	 * @param includeFields Comma delimited list of fields to include.  
	 * 	      Field name testing is case insensitive
	 * 
	 * @return
	 */
	public String toGML(String includeFields, boolean format) {
		final String TAB = format ? "\t" : "";
		final String BR = format ? "\n" : "";

        final String lt = TAB + "<";
        final String ltc = "</";
        final String ltn = TAB + "<NICS:";
        final String ltnc = "</NICS:";
		final String gt = ">";
		final String gtc = ">" + BR;
        final String b = "&lt;b&gt;";
        final String bc = "&lt;/b&gt;";
        final String br = "&lt;br/&gt;";

		StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + BR);
        stringBuilder.append("<wfs:FeatureCollection xsi:schemaLocation=\"");
        stringBuilder.append(nicsSchemaLocationURI.replaceAll("&", "&amp;") + " ");
        stringBuilder.append(wfsServiceURI.replaceAll("&", "&amp;") + " http://www.opengis.net/wfs  ");
        stringBuilder.append(wfsSchemasURI + "\" ");
        stringBuilder.append("xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        stringBuilder.append("xmlns:NICS=\"" + nicsSchemaLocationURI + "\" xmlns:wfs=\"http://www.opengis.net/wfs\">");
        LOG.debug("amp== "+wfsServiceURI);

        stringBuilder.append("<gml:featureMember>");
        stringBuilder.append(ltn + typeName);
        stringBuilder.append(gtc);
        stringBuilder.append(ltn + "id" + gt);
        stringBuilder.append(id);
        stringBuilder.append(ltnc + "id" + gtc);
        stringBuilder.append(ltn + "name" + gt);
        stringBuilder.append(name);
        stringBuilder.append(ltnc + "name" + gtc);
        stringBuilder.append(ltn + "description" + gt);
        stringBuilder.append(description);
        if (extended != null && !extended.equals("")) {
            stringBuilder.append(br);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(extended);
                Iterator itr = jsonObject.keys();
                while(itr.hasNext()) {
                    String key = (String)itr.next();
                    String value = jsonObject.getString(key);
                    stringBuilder.append(b + key + ": " + bc + value + br);
                }
            } catch (JSONException e) {
                LOG.error("extended string can't be a JSON string: " + extended);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        stringBuilder.append(ltnc + "description" + gtc);

        stringBuilder.append(ltn + "geom" + gtc);
        stringBuilder.append(lt + "gml:Point srsName=\"" + srsName + "\"" + gtc);
        stringBuilder.append(lt + "gml:coordinates"+ gt);
        stringBuilder.append(coordinates);
        stringBuilder.append(ltc + "gml:coordinates"+ gtc);
        stringBuilder.append(ltc + "gml:Point" + gtc);
        stringBuilder.append(ltnc + "geom" + gtc);


        stringBuilder.append(ltn + "speed" + gt);
        stringBuilder.append(speed);
        stringBuilder.append(ltnc + "speed" + gtc);
        stringBuilder.append(ltn + "course" + gt);
        stringBuilder.append(course);
        stringBuilder.append(ltnc + "course" + gtc);
        stringBuilder.append(ltn + "extended" + gt);
        stringBuilder.append(extended);
        stringBuilder.append(ltnc + "extended" + gtc);
        stringBuilder.append(ltn + "timestamp" + gt);
        stringBuilder.append(timestamp);
        stringBuilder.append(ltnc + "timestamp" + gtc);
        stringBuilder.append(ltn + "version" + gt);
        stringBuilder.append(version);
        stringBuilder.append(ltnc + "version" + gtc);
        stringBuilder.append(ltnc + typeName + gtc);
        stringBuilder.append(ltc + "gml:featureMember" + gtc);
        stringBuilder.append(ltc + "wfs:FeatureCollection" + gtc);

        LOG.info("\nGML: \n" + stringBuilder.toString() + "\n");

		return stringBuilder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("sarAppPLI [Id=");
		builder.append(id);  // NICS spec:  info: using unit_name and constants
//		builder.append(", typeName=");
//		builder.append(typeName);
		builder.append(", name=");
		builder.append(name); // synthesized name from json unit_name
//    builder.append(", description=");
//    builder.append(description);
		builder.append(", srsName=");
		builder.append(srsName);
		builder.append(", coordinates=");
		builder.append(coordinates);
//		builder.append(", speed=");
//		builder.append(speed);
//		builder.append(", course=");
//		builder.append(course);
        builder.append(", timestamp="); // time that bulk msg was received
        builder.append(timestamp);
        builder.append(", version=");
        builder.append(version);
//        builder.append(", extended=");
//        builder.append(extended);
        builder.append(", nicsSchemaLocationURI=");
        builder.append(nicsSchemaLocationURI);
        builder.append(", wfsServiceURI=");
        builder.append(wfsServiceURI);
        builder.append(", wfsSchemasURI=");
        builder.append(wfsSchemasURI);
        builder.append("]");


		return builder.toString();
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNicsSchemaLocationURI() {
        return nicsSchemaLocationURI;
    }

    public void setNicsSchemaLocationURI(String nicsSchemaLocationURI) {
        this.nicsSchemaLocationURI = nicsSchemaLocationURI;
    }

    public String getWfsServiceURI() {
        return wfsServiceURI;
    }

    public void setWfsServiceURI(String wfsServiceURI) {
        this.wfsServiceURI = wfsServiceURI;
    }

    public String getWfsSchemasURI() {
        return wfsSchemasURI;
    }

    public void setWfsSchemasURI(String wfsSchemasURI) {
        this.wfsSchemasURI = wfsSchemasURI;
    }


    public String getExtended() {
        return extended;
    }

    public void setExtended(String extended) {
        this.extended = extended;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
