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



import org.biojava.servlets.dazzle.datasource.*;

/**
 * Handler which implements the DAS link command.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class LinkHandler extends  AbstractDazzleHandler {
    public LinkHandler() {
        super(
            DazzleDataSource.class,
            new String[] {"link"},
            new String[] {"link/1.0"}
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
        String field = req.getParameter("field");
        String id = req.getParameter("id");
        if (field == null || id == null) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "Missing parameter");
        }
        
        try {
            boolean response = dds.doLink(req, resp, field, id);
            if (! response) {
                resp.setHeader("X-DAS-Status", "200");
                resp.setContentType("text/html");
                PrintWriter pw = resp.getWriter();
                pw.println("<h1>No extra information about this object</h1><p>field: " + field + "<br />id: " + id + "</p>");
                pw.close();
            }
        } catch (NoSuchElementException ex) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "Link failed");
        } catch (DataSourceException ex) {
            throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
        }
    }
}
