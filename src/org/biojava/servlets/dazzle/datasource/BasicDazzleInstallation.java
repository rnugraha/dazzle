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

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.utils.xml.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 * General purpose DAS server installation gateway.  Provides a flat
 * namespace containing an arbitrary set of datasources configured
 * via an XMLBeans file.
 *
 * @author Thomas Down
 * @version 1.00
 */

public class BasicDazzleInstallation implements DazzleInstallation {
	
	public static final String CONFIG_FILE = "/dazzlecfg.xml";
	
	private class DataSourceHolder {
		private final Element config;
		private final String id;
		private DazzleDataSource source;
		
		public DataSourceHolder(String id, Element config) {
			this.id = id;
			this.config = config;
		}
		
		public String getID() {
			return id;
		}
		
		public Element getConfigXML() {
			return config;
		}
		
		public synchronized DazzleDataSource getDataSource() 
		throws DataSourceException
		{
			if (source == null) {
				source = initDataSource(config, id);
			}
			return source;
		}
		
		public synchronized void destroy() {
			if (source != null) {
				source.destroy();
				source = null;
			}
		}
	}
	
	private ServletContext ctx;
	
	private Map<String,DataSourceHolder> dataSources;
	private Map<String,String> aliases;
	private Map<String,Gate> gates;
	
	{
		dataSources = new HashMap<String, DataSourceHolder>();
		aliases = new HashMap<String, String>();
		gates = new HashMap<String, Gate>();
	}
	
	private void parseConfigFile(InputSource is)
	throws DataSourceException
	{
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Element cfgDoc = parser.parse(is).getDocumentElement();
			
			Node chld = cfgDoc.getFirstChild();
			while (chld != null) {
				if (chld instanceof Element) {
					Element echld = (Element) chld;
					if (echld.getTagName().equals("datasource")) {
						// System.out.println("Found a datasource");
						String id = echld.getAttribute("id");
						if (id == null) {
							throw new ServletException("Datasource has no ID attribute");
						}
						
						dataSources.put(id, new DataSourceHolder(id, echld));
					} else if (echld.getTagName().equals("resource")) {
						String id = echld.getAttribute("id");
						Object o = initResource(echld, id);
						ctx.setAttribute(id, o);
					} else if (echld.getTagName().equals("alias")) {
						String id = echld.getAttribute("id");
						if (id == null || id.length() == 0) {
							throw new ServletException("Alias has no ID attribute");
						}
						String target = echld.getAttribute("target");
						if (target == null || target.length() == 0) {
							throw new ServletException("Alias " + id + " has no target attribute");
						}
						
						aliases.put(id, target);
					} else if (echld.getTagName().equals("ipgate")) {
						String id = echld.getAttribute("target");
						if (id == null || id.length() == 0) {
							throw new ServletException("ipgate has no `target' attribute");
						}
						Gate g = parseGate(echld);
						
						StringTokenizer toke = new StringTokenizer(id, ", ");
						while (toke.hasMoreTokens()) {
							gates.put(toke.nextToken(), g);
						}
					} else if (echld.getTagName().equals("include")) {
						String href = echld.getAttribute("href");
						if (href == null || href.length() == 0) {
							throw new ServletException("include has no `href' attribute");
						}
						InputSource is2 = new InputSource(ctx.getResourceAsStream(href));
						parseConfigFile(is2);
					}
				}
				chld = chld.getNextSibling();
			}
		} catch (IOException ex) {
			throw new DataSourceException(ex, "Couldn't load DAS config");
		} catch (SAXException ex) {
			throw new DataSourceException(ex, "Couldn't parse DAS config");
		} catch (Throwable t) {
			throw new DataSourceException(t);
		}
	}
	
	public void init(ServletConfig conf)
	throws DataSourceException
	{
		this.ctx = conf.getServletContext();
		InputSource is = new InputSource(ctx.getResourceAsStream(CONFIG_FILE));
		parseConfigFile(is);
	}
	
	public void destroy() {
		for (Iterator dsi = dataSources.values().iterator(); dsi.hasNext(); ) {
		   
		   Object o = dsi.next();
		   
		   if ( o instanceof DazzleDataSource) {
		   
		      DazzleDataSource dds = (DazzleDataSource) o;
		      System.out.println("destroying  DazzleDataSource" + dds.getName());
		      dds.destroy();
		   } else if ( o instanceof DataSourceHolder ) {
		      System.out.println("destroying DataSourceHolder ");
		      DataSourceHolder holder = (DataSourceHolder) o;
		      holder.destroy();
		   }
		}
	}
	
	private Gate parseGate(Element el)
	throws ServletException
	{
		Gate g = new Gate();
		Node chld = el.getFirstChild();
		while (chld != null) {
			if (chld instanceof Element) {
				Element echld = (Element) chld;
				if (echld.getTagName().equals("accept")) {
					String addr = echld.getAttribute("address");
					if (addr == null || addr.length() == 0) {
						throw new ServletException("ipgate.accept has no `address' attribute");
					}
					g.addAddress(addr);
				}
			}
			chld = chld.getNextSibling();
		}
		return g;
	}
	
	private DazzleDataSource initDataSource(Element e, String id) 
	throws DataSourceException
	{
		
		try {
			// check if DataSource has a no arguments constructor			
			
			DazzleDataSource dds = (DazzleDataSource) XMLBeans.INSTANCE.instantiateBean(
					e,
					getClass().getClassLoader(),
					new HashMap()
			);
			dds.init(ctx);
			return dds;
		} catch (DataSourceException ex) {
			throw ex;
		} catch (AppException ex) {			
			throw new DataSourceException(ex, "Couldn't instantiate datasource " + id + " ; failed at element " + e);
		} catch (ClassCastException ex) {
			throw new DataSourceException(ex, "Bean " + id + " is not a valid data source");
		} catch (Throwable t) {
			throw new DataSourceException(t);
		}
	}
	
	private Object initResource(Element e, String id)
	throws DataSourceException
	{
		try {
			Object o = XMLBeans.INSTANCE.instantiateBean(
					e,
					getClass().getClassLoader(),
					new HashMap()
			);
			return o;
		} catch (AppException ex) {
			throw new DataSourceException("Couldn't instantiate datasource " + id + " ; failed at element " + e);
		} catch (Throwable t) {
			throw new DataSourceException(t);
		}
	}
	
	public DazzleDataSource getDataSource(List nameComponents, HttpServletRequest req)
	throws DataSourceException
	{
		if (nameComponents.size() != 1) {
			throw new DataSourceException("Basic installation only covers flat namespace of data-sources");
		}
		String id = (String) nameComponents.get(0);
		Gate g = (Gate) gates.get(id);
		if (g != null && !g.accept(req)) {
			throw new DataSourceException("No such datasource " + id);
		}
		
		String realID = id;
		if (aliases.containsKey(id)) {
			realID = (String) aliases.get(id);
		}
		
		DataSourceHolder ddsh = (DataSourceHolder) dataSources.get(realID);
		if (ddsh == null) {
			throw new DataSourceException("No such datasource " + id);
		} else {
			try {
				return ddsh.getDataSource();
			} catch (DataSourceException ex) {
				ctx.log("Couldn't initialize datasource " + realID, ex);
				dataSources.remove(realID);
				throw ex;
			}
		}
	}
	
	public Set getDataSourceIDs(List nameComponents, HttpServletRequest req)
	throws DataSourceException
	{
		// System.err.println("Address: " + req.getRemoteAddr());
		// System.err.println("Host: " + req.getRemoteHost());
		
		if (nameComponents.size() != 0) {
			throw new DataSourceException("Basic installation only covers flat namespace of data-sources");
		}
		
		Set<String> _names = new HashSet<String>();
		_names.addAll(dataSources.keySet());
		_names.addAll(aliases.keySet());
		
		Set<String> names = new TreeSet<String>();
		for (Iterator<String> i = _names.iterator(); i.hasNext(); ) {
			String n =  i.next();
			boolean accept = true;
			Gate g = (Gate) gates.get(n);
			if (g != null) {
				accept = g.accept(req);
			}
			if (accept) {
				names.add(n);
			}
		}
		
		return names;
	}
	
	private class Gate {
		private Set<String> addresses = new HashSet<String>();
		
		public void addAddress(String addr) {
			addresses.add(addr);
		}
		
		public boolean accept(HttpServletRequest req) {
			return addresses.contains(req.getRemoteAddr());
		}
	}
}
