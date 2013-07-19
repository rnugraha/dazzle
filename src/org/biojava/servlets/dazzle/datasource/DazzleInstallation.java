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

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Interface for a namespace of datasources, to be served by the Dazzle
 * framework.
 *
 * @author Thomas Down
 * @version 1.00
 */

public interface DazzleInstallation {
    public void init(ServletConfig ctx)
        throws DataSourceException;
        
    public void destroy();    

    public DazzleDataSource getDataSource(List nameComponents, HttpServletRequest req)
        throws DataSourceException;

    public Set<String> getDataSourceIDs(List nameComponents, HttpServletRequest req)
        throws DataSourceException;
}
