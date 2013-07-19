/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.servlets.dazzle.datasource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.servlet.ServletContext;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.FeatureHolder;

import de.mpg.mpiinf.ag3.dasmi.model.Interaction;
import de.mpg.mpiinf.ag3.dasmi.model.Detail;
import de.mpg.mpiinf.ag3.dasmi.model.Interactor;
import de.mpg.mpiinf.ag3.dasmi.model.Participant;

/**
 * Simple data source that parses interactions from a SIF file 
 * (http://www.cytoscape.org/cgi-bin/moin.cgi/Cytoscape_User_Manual/Network_Formats)
 *
 * For instructions how to set up a SIF reference source, please see http://biojava.org/wiki/Dazzle
 * 
 * @author Hagen Blankenburg, Max Planck Institute for Informatics
 *
 */
public class SIFReferenceSource extends AbstractDataSource implements InteractionReferenceSource {
	private String mapMaster = null;
    private String coordSys = null;
    private String interactorDbSource = null;
    private String interactorDbSourceCvId = null;
    private Set<String> entryPoints = null;
    private List<String[]> tmpInteractions = null;
    private String fileName = null;
    private String attributeFiles = null;
    private Map<String,String[]> attributes = null;
    
         
    /***
     * 
     */
    public void init(ServletContext ctx) throws DataSourceException{
    	super.init(ctx);
    	parseAttributes(this.attributeFiles);
    	parseInteractions(this.fileName);
	}
    
	
	/**
	 * Clean up
	 */
	protected void finalize() throws Throwable {
		this.tmpInteractions = null;
		super.finalize();
	}
	

	/**
	 * Checks if the ids given in the queries array are known to be interacting. If interactions
	 * can be found (one query --> all interactions where this query takes part, two queries -->
	 * all interacitons where both take part at the same time, more than two queries --> same as 
	 * two queries, rest of the queries truncated)
	 * @return The interactions found, null if none could be found
	 */
	public Interaction[] getInteractions(String[] queries, String[][] details, String operation){
    	List<Interaction> interactions = new ArrayList<Interaction>();
    	Iterator<String[]> it = tmpInteractions.iterator();
    	while (it.hasNext()){
    		String[] pair = it.next();
    		boolean found = false;
    		if (queries.length == 1){
    			if (pair[0].equals(queries[0]) || pair[1].equals(queries[0])){
    				found = true;
    			}
    		}else{
    			if (operation != null && operation.equals("INTERSECTION")){
    				if ((pair[0].equals(queries[0]) && pair[1].equals(queries[1])) 
    						|| (pair[0].equals(queries[1]) && pair[1].equals(queries[0]))){
    					found = true;
    				}	
    			}else{
    				for (int i = 0; i < queries.length; i++){
    				if (pair[0].equals(queries[i]) || pair[1].equals(queries[i]))
    					found = true;
    				}
    			}
    		}
    		if (found == true){
    			Interaction interaction = null;
    			if (pair[0].compareTo(pair[1]) > 0){
    				interaction = (Interaction) createElement("Interaction", pair[1]+"-"+pair[0]);
    			}else{
    				interaction = (Interaction) createElement("Interaction", pair[0]+"-"+pair[1]);
    			}
    			if (details != null){
    				boolean remove = true;
	    			// check if the interaction should not be returned due to detail filtering
	    			Iterator<Detail> detIt = interaction.getDetails().iterator();
	    			// check all details of the interaction
	    			while (detIt.hasNext()){
	    				Detail det = detIt.next();
	    				// and for each detail, check if it is equal to one of the filter details
	    				for (int i = 0; i < details.length; i++){
	    					// if the property is the same
	    					if (det.getProperty().equalsIgnoreCase(details[i][0])){
	    						// check if the value is also the same, if its provided
	    						if (details[i][1] != null){
	    							if (det.getValue().equalsIgnoreCase(details[i][1])){
	    								remove = false;
	    							}
	    						}else{
	    							remove = false;
	    						}
	    					}
	    				}
	    			}
    				if (remove == true){
    					continue;
    				}
    			}
    			
    			Interactor interactor = (Interactor) createElement("Interactor", pair[0]);
		    	Participant part = new Participant();
		    	part.setInteractor(interactor);
		    	interaction.addParticipant(part);
		    	
		    	interactor = (Interactor) createElement("Interactor", pair[1]);
		    	part = new Participant();
		    	part.setInteractor(interactor);
		    	interaction.addParticipant(part);
		    	interactions.add(interaction);
    		}
    	}
    	if (interactions.size() == 0){
    		return null;
    	}else{
    		return (Interaction[])interactions.toArray(new Interaction[interactions.size()]);
    	}
    }
	
	
	/**
	 * Parses node and edge attributes (http://www.cytoscape.org/cgi-bin/moin.cgi/Cytoscape_User_Manual/Attributes)
	 * from the given files and stores them in an interanal data structure. The attributes will be converted into
	 * DETAIL elements in the getInteraction method 
	 * @param fileString Node and edge attribute files separated by semi-colon, e.g. "C:/attr1.noa;D:/attr2.eda"
	 * @throws DataSourceException
	 */
	private void parseAttributes(String fileString) throws DataSourceException{
		BufferedReader input = null;
		if (this.attributes == null){
			this.attributes = new HashMap<String, String[]>();
		}
		if (fileString == null){
			return;
		}
		String[] files = fileString.split(";");
		// for each attribute file ...
		for (int i = 0; i < files.length; i++){
			File file = new File(files[i]);
			String attribute = null;
			try {
		    	input = new BufferedReader( new FileReader(file) );
		    	String line = null; 
		    	// the first line is the name of the attribute
		    	if ((attribute = input.readLine()) == null){
		    		break;
		    	}
		    	// the rest of the file has the form "identifier = attribute value"
		    	while (( line = input.readLine()) != null){
			    	String [] temp = null;
			    	temp = line.split(" = ");
			    	temp[0] = temp[0].trim();
			    	temp[1] = temp[1].trim();
			    	// check if the foremost part is the identifier of an interaction
			    	String[] parts = temp[0].split(" ");
			    	if (parts.length == 3){
			    		if (parts[0].compareTo(parts[2]) > 0){
			    			temp[0] = parts[2]+"-"+parts[0];
			    		}else{
			    			temp[0] = parts[0]+"-"+parts[2];
			    		}
			    	}
			    	
			    	// if the id already has some stored attributes add the current
			    	if (this.attributes.containsKey(temp[0])){
			    		String[] attr = this.attributes.get(temp[0]);
			    		String[] newAttr = new String[attr.length + 2];
			    		System.arraycopy( attr, 0, newAttr, 0, attr.length );
			    		newAttr[attr.length] = attribute;
			    		newAttr[attr.length + 1] = temp[1];
			    		this.attributes.put(temp[0], newAttr);
			    		attr = null;
			    	} // no attributes for this id so far, thus create new
			    	else{
			    		String[] attr = new String[2];
			    		attr[0] = attribute;
			    		attr[1] = temp[1];
			    		this.attributes.put(temp[0], attr);
			    	}
			   	}
		    }
		    catch (FileNotFoundException ex) {
		    	ex.printStackTrace();
		    	throw new DataSourceException("A node attribute file cannot be found in the specified location");
		    }
		    catch (IOException ex){
		    	ex.printStackTrace();
		    }
		    finally {
		    	try {
		    		if (input!= null) {
		    			input.close();
		    		}
		    	}
		    	catch (IOException ex) {
		    		ex.printStackTrace();
		    	}
		    }
		}
	}

    
    
    /**
     * Parse interaction from a SIF file (http://www.cytoscape.org/cgi-bin/moin.cgi/Cytoscape_User_Manual/Network_Formats)
     * and store it in an internal data structure 
     * @param fileName
     */
    private void parseInteractions(String fileName) throws DataSourceException{
		this.tmpInteractions = new ArrayList<String[]>();
		File file = new File(fileName);
	    BufferedReader input = null;
	    Set<String> hash = new HashSet<String>();
	    try {
	    	input = new BufferedReader( new FileReader(file) );
	    	String line = null; 
	    	while (( line = input.readLine()) != null){
		    	String [] temp = null;
		    	// first try to split around tabs, otherwise use spaces
		    	temp = line.split("\t");
		    	if (temp.length == 1){
		    		temp = line.split(" ");
		    	}
		    	// the rest of the line contains at least one binary interactor
		    	for (int i = 2; i < temp.length; i++){
		    		if (temp[i].trim().length() > 0){
		    			// make a hash from the identifiers to check for duplicates
		    			String abbr = null;
		    			if (temp[0].compareTo(temp[i]) > 0){
		    				abbr = temp[i] + temp[0];
		    			}else{
		    				abbr = temp[0] + temp[i];
		    			}
		    			// if the interaction is not yet in there, add it
		    			if (!hash.contains(abbr)){
		    				String[] interaction = new String[2];
		    				interaction[0] = temp[0];
		    				interaction[1] = temp[i];
		    				hash.add(abbr);
		    				tmpInteractions.add(interaction);
		    			}
		    		}
		    	}
	    	}
	    	hash = null;
	    }
	    catch (FileNotFoundException ex) {
	    	ex.printStackTrace();
	    	throw new DataSourceException("The SIF file cannot be founde in the specified location");
	    }
	    catch (IOException ex){
	    	ex.printStackTrace();
	    }
	    finally {
	    	try {
	    		if (input!= null) {
	    			input.close();
	    		}
	    	}
	    	catch (IOException ex) {
	    		ex.printStackTrace();
	    	}
	    }
	}
    
    /**
     * Creates the given element with the given id. Includes all stored attributes as details. 
     * @param obj Interactor or Interaction
     * @param id Id or name of the element
     * @return either an Interactor, an Interaction or null
     */
    private Object createElement(String obj, String id){
    	if (obj.equalsIgnoreCase("Interactor")){
	    	Interactor interactor = new Interactor();
	    	interactor.setDbCoordSys(this.coordSys);
	    	interactor.setDbAccessionId(id);
	    	if (this.attributes.containsKey(id)){
	    		String[] attrs = this.attributes.get(id);
	    		for (int i = 0; i < attrs.length; i=i+2){
	    			// if there is an attribute symbol, do not create an extra detail but include it in the name
	    			if (attrs[i].equalsIgnoreCase("symbol")){
	    				interactor.setName(attrs[i+1]);
	    			}else{
	    				Detail detail = new Detail();
	    				detail.setProperty(attrs[i]);
	    				detail.setValue(attrs[i+1]);
	    				interactor.addDetail(detail);
	    			}
	    		}
	    	}
	    	return interactor;
    	}else if (obj.equalsIgnoreCase("Interaction")){
	    	Interaction interaction = new Interaction();
	    	interaction.setName(id);
	    	if (this.attributes.containsKey(id)){
	    		String[] attrs = this.attributes.get(id);
	    		for (int i = 0; i < attrs.length; i=i+2){
	    			Detail detail = new Detail();
	    			detail.setProperty(attrs[i]);
	    			detail.setValue(attrs[i+1]);
	    			interaction.addDetail(detail);
	    		}
	    	}
	    	return interaction;
    	}else{
    		return null;
    	}
    }
    
    
    /**
     * Return the data source string
     */
	public String getDataSourceType() {
        return "sif";
    }
     
	
	/**
	 * Return the data source version string
	 */
    public String getDataSourceVersion() {
        return "1.00";
    }
    
    /**
     * Set the file name
     * @param f File path
     */
    public void setFileName(String f){
    	this.fileName = f;
    }
    
    public String getFileName(){
    	return this.fileName;
    }
        
    public String getInteractorDbSource(){
    	return this.interactorDbSource;
    }
    
    public void setInteractorDbSource(String interactorDbSource){
    	this.interactorDbSource = interactorDbSource;
    }
    
    public String getInteractorDbSourceCvId(){
    	return this.interactorDbSourceCvId;
    }
    
    public void setinteractorDbSourceCvId(String interactorDbSourceCvId){
    	this.interactorDbSourceCvId = interactorDbSourceCvId;
    }
    
    public String getCoordSys(){
    	return this.coordSys;
    }
    
    public void setCoordSys(String cs){
    	this.coordSys = cs;
    }
    
    public void setMapMaster(String s) {
        this.mapMaster = s;
    }

    public String getMapMaster() {
        return mapMaster;
    }
    
    
    public String getAttributeFiles(){
    	return this.attributeFiles;
    }
    
    public void setAttributeFiles(String files){
    	this.attributeFiles = files;
    }
    
    public Set<String> getEntryPoints() {
    	return this.entryPoints;
    }
    
    public void setEntryPoints(Set<String> ep) {
    	this.entryPoints = ep;
    }
    
    public Sequence getSequence(String ref)
	    throws DataSourceException, NoSuchElementException{
        return null;
    }

    public String getLandmarkVersion(String ref) 
	    throws DataSourceException, NoSuchElementException{
    	return null;
   }

    public FeatureHolder getFeatures(String ref)
	    throws NoSuchElementException, DataSourceException{
        return null;
    }
      
    public Set<String> getAllTypes() {
        return null;
    }
    

    /*
	public static void main(String[] args) throws DataSourceException {
		SIFReferenceSource sif = new SIFReferenceSource();
		sif.parseAttributes("D:/MPII/workspace/Dazzle/testName.noa;D:/MPII/workspace/Dazzle/testSymbol.noa;D:/MPII/workspace/Dazzle/testConf.eda");
		
		sif.parseInteractions("D:/MPII/workspace/Dazzle/test.sif");
		
		String[] queries = {"1212"};
		Interaction[] ints = sif.getInteractions(queries);
		 for (int i = 0 ; i < ints.length ; i++) {
		        System.out.println(ints[i]);
		    }
		//System.out.println(ints.length);
	}
  
    public static void dump(String[] s) {
	    System.out.println("------------");
	    for (int i = 0 ; i < s.length ; i++) {
	        System.out.println(s[i]);
	    }
	    System.out.println("------------");
    }*/
}
