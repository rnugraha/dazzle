
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
 * @author Thomas Down
 *
 */
package org.biojava.servlets.dazzle;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Filter object for use at the sanger that rewrites inbound HTTP requests with modified dasroot
 *  Typical usage is in situations where a back-end
 * servlet container is running behind an HTTP proxy, but the servlets need
 * to know their externally-visible root dir rather than the name of the
 * internal servlet-running root dir.
 *
 * @author Jonathan Warren
 */
public class SangerProxyFilter implements Filter {
    private String proxydasroot;
    private String realdasroot;
    
    
    public void init(FilterConfig config) 
        throws ServletException
    {
        String pr = config.getInitParameter("proxy-das-root");
        String rr=config.getInitParameter("real-das-root");
       // String p = config.getInitParameter("proxy-port");
        //System.out.println("initiating filter jw");
        if (pr == null) {
            throw new ServletException("Must specify proxy-das-root");
        }
        if (rr == null) {
            throw new ServletException("Must specify real-das-root");
        }
        
        proxydasroot = pr;
        realdasroot=rr;
        
    }
    
    public void destroy() {
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
        throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest) {
        	//System.out.println("filtering jw");
            request = new HttpServletRequestWrapper((HttpServletRequest) request) {
            	
            	public String getPathInfo() {
					//System.out.println(" ServletFilter: the parent pathInfo is: " + super.getPathInfo());
					//if ( path == null )
						return super.getPathInfo();
					//else 
						//return path;
				}

				public String getRequestURI() {
					//System.out.println(" ServletFilter: getRequestURI " + super.getRequestURI());
					return super.getRequestURI();
				}

            	

				public StringBuffer getRequestURL() {
					//System.out.println(" ServletFilter: getRequestURL " + super.getRequestURL());
					//todo modify requested url with the one specified by host here
					StringBuffer originalURL=super.getRequestURL();
					//get original
					//replace real-das-root with the proxy-das-root dir 
					//ie at the sanger the proxy accepts requests as /das/ but we have dazzle running behind running under dastest
					// to seperate it from an old das dazzle installation running old ensembl das sources (Thomas Downs)
					//but we want links to have the das root directory in web pages so users can get back through the proxy as requests with
					// /dastest/ in them will not get through proxy. So we need to kid our servlets into thinking they are running under /das/ as root 
					//System.out.println("real-das-root="+realdasroot);
					//System.out.println("proxy-das-root="+proxydasroot);
					
					//replace realdasroot with proxydasroot
					//System.out.println("originalURL="+originalURL);
					String url=originalURL.toString();
					String newUrl=url.replaceFirst(realdasroot, proxydasroot);
					StringBuffer newURL=new StringBuffer();
					newURL.append(newUrl);
					//System.out.println("new url="+newURL.toString());
					return newURL;
					//return super.getRequestURL();
				
				}
            } ;
        }
        chain.doFilter(request, response);
    }
}
