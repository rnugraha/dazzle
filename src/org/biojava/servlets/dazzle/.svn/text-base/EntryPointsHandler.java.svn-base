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

import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;
import org.biojava.servlets.dazzle.datasource.*;

/**
 * Handler which implements the DAS entry_points command.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class EntryPointsHandler extends AbstractDazzleHandler {
    public EntryPointsHandler() {
        super(
            DazzleReferenceSource.class,
            new String[] {"entry_points"},
            new String[] {"entry_points/1.0"}
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
        DazzleReferenceSource drs = (DazzleReferenceSource) dds;
        
        XMLWriter xw = resp.startDasXML("DASEP", "dasep.dtd");
        
        try {
            xw.openTag("DASEP");
            xw.openTag("ENTRY_POINTS");
            xw.attribute("href", DazzleTools.fullURL(req));
            xw.attribute("version", drs.getVersion());
            
            Set entryPoints = drs.getEntryPoints();
            for (Iterator i = entryPoints.iterator(); i.hasNext(); ) {
                String ep = (String) i.next();
                xw.openTag("SEGMENT");
                xw.attribute("id", ep);
                xw.attribute("size", "" + drs.getLandmarkLength(ep));
                xw.attribute("subparts", hasSubparts(ep, drs) ? "yes" : "no");
                xw.closeTag("SEGMENT");
            }
            
            xw.closeTag("ENTRY_POINTS");
            xw.closeTag("DASEP");
            xw.close();
        } catch (Exception ex) {
        	ex.printStackTrace();
            throw new DazzleException(ex, "Error writing ENTRY_POINTS document");
        }
    }

    private boolean hasSubparts(String ref, DazzleReferenceSource drs) 
        throws DataSourceException, NoSuchElementException
    {
    	
    		Sequence seq = drs.getSequence(ref);
    		if ( seq == null)
    			return false;
    		
        return (seq.filter(new FeatureFilter.ByClass(ComponentFeature.class), false).countFeatures() > 0);
    }
}
