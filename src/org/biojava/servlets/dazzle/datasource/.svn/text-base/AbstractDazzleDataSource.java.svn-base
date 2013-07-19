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
import java.io.*;


/**
 * Abstract DazzleDataSource implementation which provides default implementations
 * for many methods.  This is a useful starting point for writing many
 * data source implementations.
 *
 * @author Thomas Down
 * @version 1.00
 */

public abstract class AbstractDazzleDataSource 
    implements DazzleDataSource    
{
    
    private String name;
    private String description;
    private String version;
    private String stylesheet;
  
    
   
    /**
     * Default destroy method does nothing.
     */
    
    public void destroy() {
    }
    
    
    /**
     * Set the datasource's name string
     */
    
    public void setName(String s) {
        this.name = s;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Set the datasource's description string
     */
    
    public void setDescription(String s) {
        this.description = s;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the datasource's version string
     */
    
    public void setVersion(String s) {
        this.version = s;
    }
    
    public String getVersion() {
        return version;
    }
    
    /**
     * Set the datasource's name stylesheet path
     */
    
    public void setStylesheet(String s) {
        this.stylesheet = s;
    }

    public String getStylesheet() {
        return stylesheet;
    }
    
  

   

    /**
     * Return a description string for a given type.  May be null (indicating
     * that the description is the same as the type ID).
     */

    public String getTypeDescription(String type) {
        return type;
    }

   
  
    /**
     * Default implementation does nothing and returns <code>false</code>.
     */
    
    public boolean doLink(HttpServletRequest req,
		       HttpServletResponse resp,
		       String field,
		       String id)
	    throws ServletException, IOException, DataSourceException, NoSuchElementException
    {
        return false;
    }


}

