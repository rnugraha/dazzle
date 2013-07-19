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
import javax.servlet.http.*;
import org.biojava.utils.xml.*;

/**
 * Handle the response to a DAS command.
 * This is a subclass of HttpServletResponse with some extra methods for
 * managing the lifecycle of a DAS request
 *
 * @author Thomas Down
 * @since 1.1
 */

public class DazzleResponse extends HttpServletResponseWrapper {
    private final DazzleServlet dazzle;
    private boolean statusIsSet = false;

    public static final boolean displayXSL = false;
    
    DazzleResponse(HttpServletResponse resp, DazzleServlet dazzle) {
        super(resp);
        this.dazzle = dazzle;
    }
    
    
    
    public void setDasStatus(int scode) {
        if (statusIsSet) {
            dazzle.log("Warning: attempt to reset DAS status");
        } else {
            ((HttpServletResponse) getResponse()).setIntHeader("X-DAS-Status", scode);
            statusIsSet = true;
        }
    }
    
    /** Starts the DAS-XML response
     * @param docType the document type of the DAS response
     * @param dtdName the name of the .dtd 
     * 
     * @return the XMLWriter object that is used to write the DAS-XML 
     * @throws IOException 
     */
    public XMLWriter startDasXML(String docType, String dtdName) 
        throws IOException
    {
        setDasStatus(DASStatus.STATUS_OK);
        setContentType(DazzleServlet.XML_CONTENT_TYPE);
        PrintWriter pw = getWriter();
        XMLWriter xw = new PrettyXMLWriter(pw);
        
        xw.printRaw("<?xml version='1.0' standalone='no' ?>");
        if ( displayXSL )
           xw.printRaw("<?xml-stylesheet type=\"text/xsl\" href=\"das.xsl\"?>");   
        xw.printRaw("<!DOCTYPE " + docType + " SYSTEM '" + dtdName + "' >");
        
        return xw;
    }


    /** Starts the DAS-XML response
     * 
     * @return the XMLWriter object that is used to write the DAS-XML 
     * @throws IOException 
     */
    public XMLWriter startDasXML() 
    		throws IOException
    {
    	boolean showXSL = displayXSL;
        return startDasXML(showXSL);
    }
    
    public XMLWriter startDasXML(boolean showXSL) throws IOException{
    	   setDasStatus(DASStatus.STATUS_OK);
           setContentType(DazzleServlet.XML_CONTENT_TYPE);
           PrintWriter pw = getWriter();
           XMLWriter xw = new PrettyXMLWriter(pw);
           
           xw.printRaw("<?xml version='1.0' standalone='no' ?>");
           if ( showXSL )
        	   xw.printRaw("<?xml-stylesheet type=\"text/xsl\" href=\"das.xsl\"?>");    
           return xw;
    	
    }

}
