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

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.utils.xml.*;
import org.biojava.servlets.dazzle.datasource.*;
import org.biojava.bio.structure.*;
import org.biojava.bio.program.das.dasalignment.* ;
import org.biojava.bio.Annotation ;


/**
 * Handler which implements the DAS ALIGNMENT command.
 *
 * @author Andreas Prlic
 */

public class AlignmentHandler extends AbstractDazzleHandler {
    
    private final static String NS_ALI = "http://www.efamily.org.uk/xml/das/2004/06/17/alignment.xsd";
    private final static String NS_DASALI = "http://www.efamily.org.uk/xml/das/2004/06/17/dasalignment.xsd";

    public AlignmentHandler() {
        super(
            AlignmentSource.class,
            new String[] {"alignment"},
            new String[] {"alignment/1.0"}
        );
    }

  
    public void run(
        DazzleServlet dazzle,
        DazzleDataSource dds,
        String cmd,
        HttpServletRequest req,
        DazzleResponse resp
    )
        throws IOException, DataSourceException, ServletException, DazzleException
    {
	//System.out.println("run AlignmentHandler");

	AlignmentSource ali_dds = (AlignmentSource)dds;
	
	String queries[] = req.getParameterValues("query") ;
	
	if ( queries == null) {
		throw new DataSourceException("you did not request a query! the spec defines the alignment command to look like das/yourDASSourceName/alignment?query=XXX");
	}
	String query=queries[0];
	String subject = "";
	try {
	    String subjects[] = req.getParameterValues("subject") ;
	    subject = subjects[0] ;
	} catch (NullPointerException e) {
	    // no subject specified... ignore	    
	}

	
	String queryCoordSys = "";
	try {
	    queryCoordSys = req.getParameterValues("querycoordsys")[0] ;
	} catch (NullPointerException e) {
	    // no subject specified... ignore	    
	}
	
	

	String subjectCoordSys = "";
	try {
	    subjectCoordSys = req.getParameterValues("subjectcoordsys")[0] ;
	} catch (NullPointerException e) {
	    // no subject specified... ignore	    
	}

	
	//   System.out.println("query >"+query+"< querycoordsys >"+queryCoordSys 
	//		   + "< subject:>"+subject+"< subjectcoordsys >" 
	//		   + subjectCoordSys + "<");
	
	//ali_dds.setQuery(query);
	    
	//System.out.println("getting alignment");
	Alignment[] alignments ;
	
	try {
	    alignments = ali_dds.getAlignments(query,queryCoordSys,subject,subjectCoordSys);	   
	} catch (Exception e) {
	    //System.out.println("error during parsing of ali");
	    throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS,e);
	}
		
	if ( ( alignments == null ) || alignments.length == 0) {
		throw new DazzleException(DASStatus.STATUS_BAD_REFERENCE,"no alignments found for reference " + query);
	}
	
	//System.out.println("no way back, starting to write response ...");
	XMLWriter xw = resp.startDasXML(false);

	xw.openTag("dasalignment");
	xw.attribute("xmlns",NS_DASALI);
	xw.attribute("xmlns:align",NS_ALI);
	xw.attribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema-instance");
	xw.attribute("xsd:schemaLocation",NS_DASALI + " " + NS_DASALI);
	
	if ( alignments != null ) {
	    for (int alinr=0;alinr<alignments.length;alinr++){
	    
		Alignment alignment = alignments[alinr] ;
	    	
		String aligType = ali_dds.getAlignmentType();
		xw.openTag("alignment");
		xw.attribute("alignType",aligType);
		Annotation[] objects = alignment.getObjects() ;
		for ( int o = 0 ; o < objects.length;o++) {
		    Annotation object = objects[o];
		    xw.openTag("alignObject");
		    xw.attribute("dbAccessionId",(String)object.getProperty("dbAccessionId"));	    
		    xw.attribute("intObjectId",(String)object.getProperty("intObjectId"));	    
		    xw.attribute("objectVersion",(String)object.getProperty("objectVersion"));	    
		    if ( object.containsProperty("type")) {
			xw.attribute("type",(String)object.getProperty("type"));	    
		    }
		    xw.attribute("dbSource",(String)object.getProperty("dbSource"));	    
		    xw.attribute("dbVersion",(String)object.getProperty("dbVersion"));	    
		    if ( object.containsProperty("dbCoordSys")) {
			xw.attribute("dbCoordSys",(String)object.getProperty("dbCoordSys"));	
		    }
		    
		    if ( object.containsProperty("details")){
			List details = (List) object.getProperty("details");
			for ( int det = 0 ; det< details.size();det++) {
			    Annotation detanno = (Annotation) details.get(det);
			    xw.openTag("alignObjectDetail");
			
			    xw.attribute("dbSource",(String)object.getProperty("dbSource"));
			    xw.attribute("property",(String)detanno.getProperty("property"));
			    xw.print((String)detanno.getProperty("detail"));
			    xw.closeTag("alignObjectDetail");
			}
		    }

		    xw.closeTag("alignObject");
		}
	    

	    
		// get the scores ...
		Annotation[] scores = alignment.getScores();
		for ( int s = 0 ; s < scores.length;s++) {

		    Annotation score = scores[s];
		    String scorename  = (String) score.getProperty("methodName");
		    String scorevalue = (String) score.getProperty("value");

		    xw.openTag("score");
		    xw.attribute("methodName",scorename);
		    xw.attribute("value",scorevalue);
		    xw.closeTag("score");
		} 
	    

		// and now the alignment blocks ...
		Annotation[] blocks = alignment.getBlocks();
		for ( int b = 0 ; b < blocks.length;b++) {
		    Annotation block = blocks[b] ;
		    xw.openTag("block");
		    xw.attribute("blockOrder",(String)block.getProperty("blockOrder"));
		    if (block.containsProperty("blockScore")) {
			xw.attribute("blockScore",(String)block.getProperty("blockScore"));
		    }
		
		    List segments = (List)block.getProperty("segments");
		    for ( int s = 0 ; s < segments.size();s++) {

			Annotation segment = (Annotation)segments.get(s);
			xw.openTag("segment");
			xw.attribute("intObjectId",(String)segment.getProperty("intObjectId"));
			if ( segment.containsProperty("start")) 
			    xw.attribute("start",(String)segment.getProperty("start"));
			if ( segment.containsProperty("end")) 
			    xw.attribute("end",(String)segment.getProperty("end"));
			if ( segment.containsProperty("strand")) 
			    xw.attribute("strand",(String)segment.getProperty("strand"));
		    
			if ( segment.containsProperty("cigar")) {
			    xw.openTag("cigar") ;
			    xw.print((String)segment.getProperty("cigar"));
			    xw.closeTag("cigar");

			}		    		    
			xw.closeTag("segment");
		    }
		    xw.closeTag("block");
		}

		// geo3D part
		// still missing ...
		// do vector stuff
		Annotation[] vectors  = alignment.getVectors();
		//System.out.println("vectors length:" + vectors.length);
		Annotation[] matrices = alignment.getMatrices();
		for ( int i = 0 ; i < vectors.length; i++) {
		    Annotation vector = vectors[i];
		    xw.openTag("geo3D");
		    String intObjectId = (String)vector.getProperty("intObjectId") ;
		    xw.attribute("intObjectId",intObjectId);
		    xw.openTag("vector");
		
		    Atom vec = (Atom) vector.getProperty("vector") ;

		    String x = vec.getX()+"";
		    xw.attribute("x",x) ;

		    String y = vec.getY()+"";  ;
		    xw.attribute("y",y) ;

		    String z = vec.getZ()+"";  
		    xw.attribute("z",z) ;

		    xw.closeTag("vector");
		
		    // get the correct matrix
		    for ( int j=0;j<matrices.length;j++){

			Annotation matrix = matrices[j];

			String intId = (String)matrix.getProperty("intObjectId");
		    
			if ( intId.equals(intObjectId)) {
			    xw.openTag("matrix");

			    for ( int u=1; u<=3; u++){
				for ( int v=1; v<=3; v++){
				    String mat = "mat"+u+v;
				    String val = (String) matrix.getProperty(mat) ;
				    xw.attribute(mat,val);
				}
			    }
			    xw.closeTag("matrix");
			}
		    }
		
		    xw.closeTag("geo3D");
		}
		xw.closeTag("alignment");
	    } 
	}
	xw.closeTag("dasalignment");
	xw.close();
	
    }

}
    
