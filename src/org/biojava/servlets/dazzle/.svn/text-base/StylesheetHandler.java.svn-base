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
import javax.servlet.*;
import javax.servlet.http.*;
import org.biojava.servlets.dazzle.datasource.*;

/**
 * Handler which implements the DAS stylesheet command.
 */

public class StylesheetHandler extends AbstractDazzleHandler {
    public StylesheetHandler() {
        super(
            DazzleDataSource.class,
            new String[] {"stylesheet"},
            new String[] {"stylesheet/1.0"}
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
        String stylesheetPath = dds.getStylesheet();
        InputStream styleSheet = null;
        
        if (stylesheetPath == null) {
            stylesheetPath = DazzleServlet.DEFAULT_STYLESHEET;
        }
        styleSheet = dazzle.getServletContext().getResourceAsStream(stylesheetPath);
        if (styleSheet == null) {
            throw new DazzleException(DASStatus.STATUS_BAD_STYLESHEET, "Couldn't find stylesheet");
        }   
        
        resp.setDasStatus(DASStatus.STATUS_OKAY);
        resp.setContentType(DazzleServlet.XML_CONTENT_TYPE);
        resp.setHeader("Content-Encoding", "plain");
        OutputStream os = resp.getOutputStream();
        byte[] buffer = new byte[256];
        int bufMax = 0;
        while (bufMax >= 0) {
            bufMax = styleSheet.read(buffer);
            if (bufMax > 0)
                os.write(buffer, 0, bufMax);
        }
        os.flush();
    }
}
