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
 * Created on 10.5.2004
 */

package org.biojava.servlets.dazzle.datasource;

//import java.util.*;
//import java.io.*;
//import java.lang.String.* ;

//import javax.servlet.*;
//import javax.servlet.http.*;

//import org.biojava.utils.xml.*;

//import org.biojava.bio.structure.io.*;
import org.biojava.bio.program.das.dasalignment.* ;



/** 
 * Interface for Alignment sources
 * called by  AlignmentHandler 
 *
 * @author Andreas Prlic
 * @version 1.01
 */

public interface  AlignmentSource extends DazzleDataSource {
    
    /** get alignments for a query (can be several) only the ID of the
     * query is mandatory, all other arguments are optional and can be
     * null.  This also depends on the type of data the alignment
     * source is providing.  e.g. for a UniProt PDB alignment source
     * the typical entry would be query=1a4a. It would return a list
     * of all alignments against UniProt sequences. Since we do not
     * know in advance against this PDB code is aligned the subject is
     * null and the two coordinate systems also are set to null.
     * 
     * @param query (mandatory) the ID of the query e.g. 22 for
     * chromosome 22
     * @param queryCoordSys (optional, can be null) the coordinate
     * system of the query.  e.g. human26 for assembly 26 of human
     * @param subject (optional, can be null) the ID of the
     * subject. e.g. 3 for chromosome 3
     * @param subjectCoordSys (optional,can be null) the coordinate
     * system of the subject.  e.g. mouse 20 for assembly 20 of mouse.
     *
     *
 */
    public Alignment[] getAlignments(String query,
				     String queryCoordSys,
				     String subject,
				     String subjectCoordSys)  
	throws DataSourceException       ;

     
    /** get and set the Alignment Type - is part of the XML response */
    public  void        setAlignmentType(String aliType);

    /** get and set the Alignment Type - is part of the XML response */
    public  String      getAlignmentType();

 }
