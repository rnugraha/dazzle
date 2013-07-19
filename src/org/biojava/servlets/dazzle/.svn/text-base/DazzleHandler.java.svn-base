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
 * Handler which implements one or more DAS commands.
 */

public interface DazzleHandler {
    public boolean accept(DazzleDataSource dds);
    
    public String[] capabilities(DazzleDataSource dds);
    
    public String[] commands(DazzleDataSource dds);
    
    public void run(
        DazzleServlet dazzle,
        DazzleDataSource dds,
        String cmd,
        HttpServletRequest req,
        DazzleResponse resp
    )
        throws IOException, ServletException, DataSourceException, DazzleException;
}
