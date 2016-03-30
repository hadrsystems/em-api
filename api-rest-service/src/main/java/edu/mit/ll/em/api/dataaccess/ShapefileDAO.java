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
package edu.mit.ll.em.api.dataaccess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;


public class ShapefileDAO extends BaseDAO {

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static ShapefileDAO instance = new ShapefileDAO();
	}

	public static ShapefileDAO getInstance() {
		return Holder.instance;
	}

	private DataStore lazyDatastore;

	ShapefileDAO() {
	}

	private DataStore getDataStore() throws IOException {
		if (lazyDatastore == null) {
			Map<String, String> dbParams = new HashMap<String, String>();
			dbParams.put( "dbtype", "postgis");
			dbParams.put( "jndiReferenceName", "java:comp/env/jboss/shapefileDatasource");
			lazyDatastore = DataStoreFinder.getDataStore(dbParams);
		}
		return lazyDatastore;
	}

	public void insertFeatures(String tableName, SimpleFeatureSource featureSource) throws IOException, FactoryException {
		Query query = new Query();
		query.setCoordinateSystemReproject(CRS.decode("EPSG:3857"));
		SimpleFeatureCollection featcollection = featureSource.getFeatures(query);
		
		DataStore datastore = getDataStore();
		
		//create the new schema with postgisTableName
		SimpleFeatureType newSchema = createWithNewName(featureSource.getSchema(), tableName);
		datastore.createSchema(newSchema);
		
		//connect to feature store and create a new table for the shape file
		SimpleFeatureStore featStore = (SimpleFeatureStore) datastore.getFeatureSource(tableName);
		
		Transaction t = new DefaultTransaction("add");
		featStore.setTransaction(t);
		try {
			featStore.addFeatures(featcollection);
			t.commit();
		} catch (Exception e) {
			t.rollback();
			throw e;
		} finally {
			t.close();
		}
	}

	public void removeFeaturesTable(String tableName) throws IOException {
		getDataStore().removeSchema(tableName);
	}
	
	private SimpleFeatureType createWithNewName(SimpleFeatureType featureType, String newTypeName) {
		SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
		tb.init(featureType);
		tb.setName(newTypeName);
		tb.setDefaultGeometry(featureType.getGeometryDescriptor().getLocalName());
		return tb.buildFeatureType();
	}
}

