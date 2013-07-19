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
package org.biojava.servlets.dazzle;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.biojava.utils.xml.XMLWriter;
import org.biojava.servlets.dazzle.datasource.InteractionReferenceSource;
import org.biojava.servlets.dazzle.datasource.DazzleDataSource;
import org.biojava.servlets.dazzle.datasource.DataSourceException;

import de.mpg.mpiinf.ag3.dasmi.model.Interaction;
import de.mpg.mpiinf.ag3.dasmi.model.Participant;
import de.mpg.mpiinf.ag3.dasmi.model.Interactor;
import de.mpg.mpiinf.ag3.dasmi.model.Detail;
import de.mpg.mpiinf.ag3.dasmi.model.Range;


/**
 * Handler implementing the DAS 1.53E interaction command
 * @author Hagen Blankenburg, Max Planck Institute for Informatics
 */
public class InteractionHandler extends AbstractDazzleHandler {
	
	//private static final String XMLNS = "http://www.w3.org/1999/xhtml";
	private static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String SCHEMA_LOC = "http://dasmi.de/dasint";

	
	/**
	 * Constructur, sets some configuration strings
	 */
    public InteractionHandler() {
        super(InteractionReferenceSource.class,
            new String[] {"interaction"},
            new String[] {"interaction/1.0"});
    }
    
    /**
     * To be called from the Dazzle servlet in response to a request
     * @param req Servlet request
     * @param resp Servlet resonse
     * @param dds Dazzle data source to be used 
     */
    public void run(DazzleServlet dazzle, DazzleDataSource dds,
    		String cmd, HttpServletRequest req, DazzleResponse resp)
        throws IOException, DataSourceException, ServletException, DazzleException{
    	if("interaction".equals(cmd)) {
        	interactionCommand(req, resp, dds);
        }
    }
    
   /**
    * 
    * @param req Servlet request
    * @param resp Servlet resonse
    * @param dds Dazzle data source to be used 
    * @throws IOException
    * @throws DataSourceException
    * @throws ServletException
    * @throws DazzleException
    */
    private void interactionCommand(HttpServletRequest req, DazzleResponse resp, DazzleDataSource dds)
	    throws IOException, DataSourceException, ServletException, DazzleException {
    	String[] queryInteractors = null;
    	String[] tmpQueryDetails = null;
    	String[] tmpQueryOperation = null;
    	String[][] queryDetails = null;
    	
    	Map<String,Interactor> interactors = new HashMap<String,Interactor>();  //map for all interactors
    	InteractionReferenceSource irs = (InteractionReferenceSource) dds; // convert the datasource to an interaction source
    	
    	String queryOperation = "INTERSECTION"; // if no operation parameter is used we will use the intersection
    	// first get all parameters from the servlet request
    	// start with the mandaroty interactor
    	try{
    		queryInteractors = req.getParameterValues("interactor") ;
    	} catch (Exception e){
    		throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "No interaction partners specified for interaction command");
    	}
    	// if no interactor was specified (actually wouldn't happen because dazzle would fail before)
        if (queryInteractors != null && queryInteractors.length == 0) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "No interaction partners specified for interaction command");
        }
        
    	// now try optional details
    	try{
    		tmpQueryDetails = req.getParameterValues("detail") ;
    	}catch(Exception e){
    		// can be left out
    	}
    	
    	// and try the optional operation parameter. 
    	try{
    		tmpQueryOperation = req.getParameterValues("operation") ;
    	}catch(Exception e){
    		//if none is provided, we will stick with the intersection
       	}
    	
    	
    	// process the detail parameters, i.e., extract property value combinations
    	// details can have the form detail=property:aproperty or detail=property:aproperty,value:avalue
    	if (tmpQueryDetails!= null){
    		List<String[]> details = new ArrayList<String[]>();
    		// iterate over all details
    		for (int i = 0; i < tmpQueryDetails.length; i++){
    			// split property and value part that is separated by a comma
    			String[] bigParts = tmpQueryDetails[i].split(",");
    			String[] props = null;
    			String[] vals = null;
    			String[] detail = new String[2];
    			// split the property part
    			props = bigParts[0].split(":");
    			if (props.length > 1 && props[1] != null && props[0].equalsIgnoreCase("property")){
    				detail[0] = props[1];
    			}
    			// some problem with the detail property, e.g. a missing sepeartor, try the next one
				// TODO alternatively throw DazzleException
    			else{
    				continue;
    			}
    			// a specific value has been assigned to the property, extract it as well
    			if (bigParts.length > 1 && bigParts[1] != null){
    				vals = bigParts[1].split(":");
    				if (vals.length > 1 && vals[1] != null && vals[0].equalsIgnoreCase("value")){
    					detail[1] = vals[1];
    					for (int g = 2; g < vals.length; g++ ){
    						detail[1] += ":"+vals[g];
    					}
    				}
    			}
    			details.add(detail); 
    		}
    		// convert our temporary detail list into an array
    		Iterator<String[]> it = details.iterator();
    		queryDetails = new String[details.size()][2];
    		int i = 0;
    		while (it.hasNext()){
    			queryDetails[i]= it.next();
    			//System.out.println("Detail: " + queryDetails[i][0] + queryDetails[i][1]);
    			i++;
    		}
    		// if there have only been faulty detail parameters, use none 
    		// TODO alternatively throw a DazzleException
    		if (i == 0){
    			queryDetails = null;
    		}
    	} // end of detail parsing stuff ...
    	
    	
    	// set the operation, default is intersection
    	if (tmpQueryOperation != null){
    		if (tmpQueryOperation[0].equalsIgnoreCase("union") || tmpQueryOperation[0].equalsIgnoreCase("and")){
    			queryOperation = "UNION";
    		}else if (tmpQueryOperation[0].equalsIgnoreCase("intersection") || tmpQueryOperation[0].equalsIgnoreCase("or")){
    			queryOperation = "INTERSECTION";
    		}
    	} 
    	
    	// now request the intercations with our given parameters from the data source
        List<Interaction> interactionResults = new ArrayList<Interaction>();
        try {
            Interaction[] interactions = irs.getInteractions(queryInteractors, queryDetails, queryOperation);
            // if there are any interactions
            if (interactions != null){
            	// for each interaction add the interaction and store the particiapting interactors
            	for (int j = 0; j < interactions.length; j++) {
	           		Interaction interaction = interactions[j];
	           		interactionResults.add(interaction);
	           		for (Iterator<Participant> partIt = interaction.getParticipants().iterator(); partIt.hasNext();){
	           			// needed to print all the interactors at teh beginning of the xml
	           			Interactor inter = partIt.next().getInteractor();
	           			interactors.put(inter.hashify(), inter); 
	           		}
	          	}
            }
           
        }catch (NoSuchElementException ex) {
        	throw new DazzleException(DASStatus.STATUS_BAD_REFERENCE, ex);
        }


        // start the response xml, currently without stylesheet support
        XMLWriter xw = resp.startDasXML(false);
        try {
        	xw.openTag("DASINT");
        	xw.attribute("xmlns", SCHEMA_LOC);
            xw.attribute("xmlns:xsi", XSI);
            xw.attribute("xsi:schemaLocation", SCHEMA_LOC+".xsd");
        	
            // TODO do some better error handling and tell teh user, why there are no results
            if (interactionResults.size() < 1){
            
            }else{
            	// firstly print all the interactors 
            	for (Iterator<String> interactorIt = interactors.keySet().iterator(); interactorIt.hasNext();){
            		String hashKey = (String) interactorIt.next();
            		Interactor interactor = (Interactor)interactors.get(hashKey);
            		xw.openTag("INTERACTOR");
                  	xw.attribute("intId", hashKey);
                	xw.attribute("shortLabel", interactor.getName());
                	xw.attribute("dbSource", interactor.getDbSource());
                	if (interactor.getDbSourceCvId().trim().length() > 0){
                		xw.attribute("dbSourceCvId", interactor.getDbSourceCvId());
                	}
                	xw.attribute("dbVersion", interactor.getDbVersion());
                	xw.attribute("dbAccessionId", interactor.getDbAccessionId());
                	xw.attribute("dbCoordSys", interactor.getDbCoordSys());

                	// then write all all the interactor details
                	writeDetails(xw, interactor.getDetails().iterator());
                	xw.closeTag("INTERACTOR");
            	}
            	
            	// new print all the interactions 
            	for (Iterator<Interaction> i = interactionResults.iterator(); i.hasNext(); ) {
            		Interaction interaction = (Interaction) i.next();
	                xw.openTag("INTERACTION");
	                xw.attribute("name", interaction.getName());
	                xw.attribute("dbSource", interaction.getDbSource());
	                if (interaction.getDbSourceCvId() != null){
	                	xw.attribute("dbSourceCvId", interaction.getDbSourceCvId());
	                }
	                xw.attribute("dbVersion", interaction.getDbVersion());
	                xw.attribute("dbAccessionId", interaction.getDbAccessionId());
	                // add the interaction details  	  
	                writeDetails(xw, interaction.getDetails().iterator());
	                
	                // and add the participants
	                List<Participant> participants = interaction.getParticipants();
	                for (Iterator<Participant> j = participants.iterator(); j.hasNext();) {
	                	Participant participant = (Participant)j.next();
	                	xw.openTag("PARTICIPANT");
	                	xw.attribute("intId", participant.getInteractor().hashify());
	                	// write details for participants
	                  	writeDetails(xw,participant.getDetails().iterator());
	                  	xw.closeTag("PARTICIPANT");
	                }
	                xw.closeTag("INTERACTION");
	            }
            }
            xw.closeTag("DASINT");
            xw.close(); // thats it
        } catch (Exception ex) {
            throw new DazzleException(ex, "Error writing interaction document");
        }
    }
    
    
    /**
     * Writs all the details contained in the iterator to the passed xml writer. 
     * @param xw xml output stream writer for interaction respnse
     * @param it Iterator over details, can belong to interactions, interactors, or participants
     * @throws IOException
     */
    private void writeDetails(XMLWriter xw, Iterator<Detail> it) throws IOException{
    	while(it.hasNext()){
        	Detail detail = (Detail) it.next();
        	xw.openTag("DETAIL");
        	xw.attribute("property", detail.getProperty());
        	if(detail.getPropertyCvId().trim().length() > 0){
        		xw.attribute("propertyCvId", detail.getPropertyCvId());
        	}
        	xw.attribute("value", detail.getValue());
        	if (detail.getValueCvId().trim().length() > 0){
        		xw.attribute("valueCvId", detail.getValueCvId());
        	}
        	// if ther detail has a range assigned to it, print it as well
        	if (detail.getRange() != null){
        		Range range = detail.getRange();
        		xw.openTag("RANGE");
        		xw.attribute("start", String.valueOf(range.getStart()));
        		if (range.getStartStatus().trim().length() > 0){
        			xw.attribute("startStatus", range.getStartStatus());
        		}
        		if (range.getStartStatusCvId().trim().length() > 0){
        			xw.attribute("startStatusCvId", range.getStartStatusCvId());
        		}
        		xw.attribute("end", String.valueOf(range.getEnd()));
        		if (range.getEndStatus().trim().length() > 0){
        			xw.attribute("endStatus", range.getEndStatus());
        		}
        		if (range.getEndStatusCvId().trim().length() > 0){
        			xw.attribute("endStatusCvId", range.getEndStatusCvId());
        		}
        		xw.closeTag("RANGE");
        	}
        	xw.closeTag("DETAIL");
        }
    }
}
