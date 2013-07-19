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

import java.util.*;
import java.io.*;
import java.net.URLDecoder;

import javax.servlet.http.*;

/**
 * Wraps up an HttpServletRequest, and allows for handling
 * of query strings separated by ';' as well as '&'.
 *
 * @author Thomas Down, Benjamin Schuster-Boeckler
 */

class DASQueryStringTranslator extends HttpServletRequestWrapper {
    private String queryString = null;
    private Hashtable params = null;
    
    public DASQueryStringTranslator(HttpServletRequest req) {
        super(req);
    }
    
    public void setRequest(HttpServletRequest req) {
        super.setRequest(req);
        
        // Since we cache a few things, these need to be flushed.
        
        this.queryString = null;
        this.params = null;
    }
    
    protected String getQueryStringTranslated() {
        if (queryString != null)
            return queryString;
        
        if (getMethod().equalsIgnoreCase("POST")) {
            try {
                BufferedReader br = new BufferedReader(getReader());
                queryString = br.readLine();
            } catch (Exception ex) {
            }
        } else {
            queryString = getQueryString();
	    if (queryString == null || queryString.length() == 0)
		return null;
            try {               
                queryString = URLDecoder.decode(queryString,"UTF-8");                
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();                
            }
        }
        if (queryString == null || queryString.length() == 0)
            return null;
        if (queryString.indexOf(';') >= 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < queryString.length(); ++i) {
                char c = queryString.charAt(i);
                if (c == ';')
                    c = '&';
                sb.append(c);
            }
            queryString = sb.toString();
        }
        return queryString;
    }
    
    protected Hashtable getParameters() {
        // Only works for GETs
        
        if (params == null) {
            String p = getQueryStringTranslated();
            if (p == null) 
                params = new Hashtable();
            else
                params = HttpUtils.parseQueryString(p);
        }
        
        return params;
    }
    
    public String getParameter(String name) {
        Hashtable params = getParameters();
        String[] vals = (String[]) params.get(name);
        if (vals != null)
            return vals[0];
        return null;
    }
    
    public String[] getParameterValues(String name) {
        Hashtable params = getParameters();
        String[] vals = (String[]) params.get(name);
        return vals;
    }
    
    public Enumeration getParameterNames() {
        Hashtable params = getParameters();
        return params.keys();
    }
}
