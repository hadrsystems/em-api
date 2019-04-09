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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.mit.ll.nics.common.ws.client.BasicRequest;

public class DatalayerExportFile {
	
	private File textFile;
	private String tempDirectory;
	
	protected String name;
	protected List<File> files;
	
	public static final String SHAPE = ".shp";
	public static final String ZIP = ".zip";
	public static String TXT = ".txt";
	public static String KML = ".kml";
	
	/* DatalayerExportFile
	 * @param name - the name of the file
	 */
	public DatalayerExportFile(String name){
		this.name = name;
		this.files = new ArrayList<File>();
		try{
			this.tempDirectory = System.getProperty("java.io.tmpdir");
			this.textFile = this.createTempFile(this.name, TXT);
			this.files.add(this.textFile);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/** writeToTextFile
	 * 
	 * @param text
	 */
	public void writeToTextFile(String text){
		try{
			FileWriter fw = new FileWriter(this.textFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(text);
			bw.close();
			fw.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/** writeToTextFile
	 * 
	 * @param text
	 */
	public void writeToTextFile(List<String> text){
		try{
			FileWriter fw = new FileWriter(this.textFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(Iterator<String> itr = text.iterator(); itr.hasNext();){
				bw.write(itr.next());
				bw.write(" ");
				bw.write("\n");
				bw.newLine();
			}
			bw.close();
			fw.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/** addFile
	 * 
	 * @param f - File to add
	 */
	public void addFile(File f){
		this.files.add(f);
	}
	
	/** addFile
	 * 
	 * @param layer - bytes to be converted into a file
	 * @param filename - name of file
	 * @param ext
	 * @return File
	 */
	public File addFile(byte[] layer, String filename, String ext){
		File layerFile = null;
		
		if(layer == null || layer.length == 0){
			System.out.println("No Layer was found");
		}else{
			try{
				layerFile = this.createTempFile(filename, ext);
		        
				//convert array of bytes into file
			    FileOutputStream fileOutputStream = 
		                  new FileOutputStream(layerFile); 
			    fileOutputStream.write(layer);
			    fileOutputStream.close();
				this.files.add(layerFile);
			}catch(IOException ioe){
				ioe.printStackTrace();
			}catch(NullPointerException npe){
				npe.printStackTrace();
			}
		}
		return layerFile;
	}
	
	/** getTextFile
	 * 
	 * @return the textfile for this export
	 */
	public File getTextFile(){
		return this.textFile;
	}
	
    /** getZipFile
     *  Zip up all files as well as the text file
     * 
     * @return Zip File
     */
	public File getZipFile(){
		try{
    		File zipFile = this.createTempFile("NICS_" + this.name, ZIP);
    		FileOutputStream fos = new FileOutputStream(zipFile);
    		ZipOutputStream zos = new ZipOutputStream(fos);
    		
    		for(Iterator<File> itr = this.files.iterator(); itr.hasNext();){
    			byte[] buffer = new byte[1024];
    			File f = itr.next();
    			
	    		ZipEntry ze= new ZipEntry(f.getName());
	    		zos.putNextEntry(ze);
	    		FileInputStream in = new FileInputStream(f);
	 
	    		int len;
	    		while ((len = in.read(buffer)) > 0) {
	    			zos.write(buffer, 0, len);
	    		}
	 
	    		in.close();
    		}
    		zos.closeEntry();
    		zos.close();
    		return zipFile;
    	}catch(IOException ex){
    	   ex.printStackTrace();
    	}
    	return null;
	}
	
	/** getResponse
	 *  Most likely overwritten. Returns textFile. Should maybe return zipFile?
	 * @return textFile
	 */
	public File getResponse(){
		return this.textFile;
	}
	
	/** requestLayer
	 *  
	 * @param url
	 * @return
	 */
	protected byte[] requestLayer(String url){
		BasicRequest request = new BasicRequest(BasicRequest.BYTES);
		return (byte[]) request.getRequest(url, new HashMap<String, String>());
	}
	
	/** createTempFile
	 *  
	 * @param name
	 * @param ext
	 * @return file
	 */
	protected File createTempFile(String name, String ext) throws IOException, NullPointerException{
		StringBuffer filePath = new StringBuffer(this.tempDirectory);
		filePath.append(File.separator);
		filePath.append(name);
		filePath.append(ext);
		
		File f = new File(filePath.toString());
		f.deleteOnExit();
		
		return f;
	}
}