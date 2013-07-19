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
import java.util.zip.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.utils.*;
import org.biojava.utils.xml.*;

import org.biojava.servlets.dazzle.datasource.*;

/**
 * A general purpose server for the Distributed Annotation System.  Should be a
 * fully compliant implementation of DAS/1.52
 *
 * <p>
 * When the servlet is initialized, it reads an XML configuration file (dazzlecfg.xml by
 * default), which should configure one or more plugins implementing the DazzleDataSource
 * interface.  These are then served as DAS DSNs.
 * </p>
 *
 * @author Thomas Down
 * @author Andreas Prlic
 * @version 1.0.95
 */

public class DazzleServlet extends HttpServlet {
	//
	// General configuration
	//
			
	private static final long serialVersionUID = 1L;
	
	public static final String DAZZLE_VERSION = "DazzleServer/1.0.95 (20090715; BioJava 1.7)";
	
	public static final String DAS_PROTOCOL_VERSION = "1.53E";
	
	public static final String[] DAS_CORE_CAPABILITIES = new String[] {"dsn/1.0"};
	
	public static final String XML_CONTENT_TYPE     = "text/xml";
	public static final String SOURCES_CONTENT_TYPE = "application/x-das-sources+xml";
	
	public static final String DEFAULT_STYLESHEET = "/stylesheet.xml";
	public static final String WELCOME_MESSAGE    = "/das_welcome.html";
	public static final String DAS_SOURCES_LIST   = "/sources.xml";
	public static final String XSL_FILE           = "/das.xsl";
	
	public static final String DAZZLE_INSTALLATION_TYPE = "org.biojava.servlets.dazzle.datasource.BasicDazzleInstallation";
	public static final boolean REGEXP_SUPPORT = false;
	public static final boolean TOLERATE_MISSING_SEGMENTS = false;
	
	//
	// Runtime stuff
	// 
	
	private DazzleHandler[] handlers;
	private DazzleInstallation installation;
	
	//
	// Local configuration flags
	//
	
	private boolean gzipEncoding = true;
	
	/**
	 * Get info about this servlet.
	 */
	
	public String getServletInfo() {
		return DAZZLE_VERSION;
	}
	
	/**
	 * Initialize DAS services, and load the XML configurations.
	 */
	
	public void init(ServletConfig config)
	throws ServletException
	{
		super.init(config);
				
		// Initialize handlers
		
		try {
			Set handlerNames = Services.getImplementationNames(
					DazzleHandler.class,
					getClass().getClassLoader()
			);
			List<Object> handlerList = new ArrayList<Object>();
			for (Iterator i = handlerNames.iterator(); i.hasNext(); ) {
				String handlerName = (String) i.next() ;
				System.out.println("adding handler " + handlerName);
				handlerList.add(getClass().getClassLoader().loadClass(handlerName).newInstance());
			}
			handlers = (DazzleHandler[] ) handlerList.toArray(new DazzleHandler[0]);
		} catch (Exception ex) {
			throw new ServletException("Error initializing handler", ex);
		}
		
		// Initialize installation
		
		String installClassName = config.getInitParameter("dazzle.installation_type");
		if (installClassName == null) {
			installClassName = DAZZLE_INSTALLATION_TYPE;
		}
		
		try {
			Class installClass = getClass().getClassLoader().loadClass(installClassName);
			installation = (DazzleInstallation) installClass.newInstance();
			installation.init(config);
		} catch (DataSourceException ex) {
			log("Error initializing installation", ex);
			throw new ServletException("Error initializing installation");
		} catch (ClassCastException ex) {
			throw new ServletException("Not a Dazzle installation: " + installClassName);
		} catch (ClassNotFoundException ex) {
			throw new ServletException("Couldn't find class: " + installClassName);
		} catch (InstantiationException ex) {
			throw new ServletException("Couldn't instantiate " + installClassName);
		} catch (IllegalAccessException ex) {
			throw new ServletException("Couldn't instantiate " + installClassName);
		}
	}
	
	/**
	 * Clean up datasources
	 */
	
	public void destroy() {
		super.destroy();
		installation.destroy();
	}
	
	/**
	 * Normal DAS command.  As well as handling GETs, this method
	 * also gets form-encoded POSTs.
	 */
	
	public void doGet(
			HttpServletRequest req,
			HttpServletResponse resp
	)
	throws ServletException, IOException
	{
						
		// We `filter' the query string, to accept strings separated
		// using the ';' character
		req = new DASQueryStringTranslator(req);
		
		// determine content-encoding for reply
		// we only implement gzip so if that's not found, we
		// fallback to plaintext.
		String encodingStr = req.getHeader("Accept-Encoding");
		
		// if gzip encoding is required, replace the HttpServletResponse with a gzip version
		// As IO is now buffered DO REMEMBER TO FLUSH BUFFERS PROPERLY.
		if (encodingStr != null && gzipEncoding)  {
			if (encodingStr.indexOf("gzip") != -1) {
				resp = new HttpServletResponseWrapper(resp) {
					GZIPOutputStream gzipOut = null;
					public PrintWriter getWriter() throws IOException {
						gzipOut = new GZIPOutputStream(getResponse().getOutputStream());
						return new PrintWriter(gzipOut) {
							// we return a subclassed PrintWriter which finishes GZIPOutputStream when close is called
							public void close() {
								super.close();
								try {
									gzipOut.finish();
								}
								catch (IOException ie) {
									System.err.println("Unexpected IOException when closing GZIPOutputStream");
								}
							}
						};
					}
				};
				resp.setHeader("Content-Encoding", "gzip");
			}
		}
		
		DazzleResponse dazzleResp = new DazzleResponse(resp, this);

		// Allow cross-site access
		
		// log("Origin= " + req.getHeader("Origin"));
		dazzleResp.setHeader("Access-Control-Allow-Origin", "*");
		// dazzleResp.setHeader("Access-Control-Allow-Credentials", "true");
		
		// Let ourselves be known...
		
		dazzleResp.setHeader("X-DAS-Version", DAS_PROTOCOL_VERSION);
		dazzleResp.setHeader("X-DAS-Server", DAZZLE_VERSION);
		
		// used to send caps here, now have to wait until we've sorted out the dds
		
		String cmdPath = req.getPathInfo();
		if (cmdPath == null) {
			cmdPath = "";
		}
		
		StringTokenizer toke = new StringTokenizer(cmdPath, "/");
		if (! toke.hasMoreTokens()) {
			welcomePage(req, resp);
			return;
		}
		
		List<String> nameComponents = new ArrayList<String>();
		String command = toke.nextToken();
		String prevComponent = "";
		while (toke.hasMoreTokens()) {
			nameComponents.add(command);
			prevComponent = command;
			command = toke.nextToken();
		}		
		
		//System.out.println("prevComponent " + prevComponent);
		try {
			//System.out.println("*** INFO *** got command " + command);
			
			if (command.equals("dsn")) {
				dsnPage(nameComponents, req, dazzleResp);
			} else if (command.equals("das.xsl")) {
				sendXSLFile( dazzleResp);
			} else if ( command.equals("sources")){
				sendSources(req,resp);
			} else if (command.endsWith(".dtd")) {
				sendDTD(req, dazzleResp, command);
			} else if (prevComponent.equalsIgnoreCase("css")){
				// a stylesheet was requested
				sendStylesheet(req,dazzleResp,command);
			} else {
				DazzleDataSource dds = null;
				try {
					dds = installation.getDataSource(nameComponents, req);
				} catch (DataSourceException ex) {
					sendError(req, dazzleResp, DASStatus.STATUS_BAD_DATASOURCE);
					return;
				}
				
				List<String> caps = new ArrayList<String>(Arrays.asList(DAS_CORE_CAPABILITIES));
				DazzleHandler commandHandler = null;
				for (Iterator hi = Arrays.asList(handlers).iterator(); hi.hasNext(); ) {
					DazzleHandler handler = (DazzleHandler) hi.next();
					
					if (handler.accept(dds)) {
						//System.out.println("accepting " + dds.getName());
						caps.addAll(Arrays.asList(handler.capabilities(dds)));
						//System.out.println(handler.commands(dds));
						if (Arrays.asList(handler.commands(dds)).contains(command)) {
							if (commandHandler == null) {
								commandHandler = handler;
							}
						}
					}
				}
				
				dazzleResp.setHeader("X-DAS-Capabilities", formatCaps(caps));
				
				if (commandHandler != null) {
					commandHandler.run(this, dds, command, req, dazzleResp);
				} else {
					sendError(req, dazzleResp, DASStatus.STATUS_BAD_COMMAND, "Command " + command + " is not available for datasource " +dds.getName());
					
				}
			}
		} catch (DataSourceException ex) {
			sendError(req, dazzleResp, DASStatus.STATUS_SERVER_ERROR, ex);
		} catch (DazzleException ex) {		
			sendError(req, dazzleResp, ex.getDasStatus(), ex);
		}
	}
	
	private static String formatCaps(List capList) {
		StringBuffer sb = new StringBuffer();
		for (Iterator ci = capList.iterator(); ci.hasNext(); ) {
			sb.append(ci.next());
			if (ci.hasNext()) {
				sb.append("; ");
			}
		}
		return sb.toString();
	}
	
	/**
	 * The only POSTs we support are form-encoded requests, which
	 * get delegated to doGet.  Anything else is an error.
	 */
	
	public void doPost(HttpServletRequest req,
			HttpServletResponse resp)
	throws ServletException, IOException
	{
		String contentType = req.getContentType();
		StringTokenizer ctToke = new StringTokenizer(contentType, "/ ");
		String ctmedia = null, ctsubtype = null;
		if (ctToke.hasMoreTokens()) {
			ctmedia = ctToke.nextToken().toLowerCase();
		}
		if (ctToke.hasMoreTokens()) {
			ctsubtype = ctToke.nextToken().toLowerCase();
		}
		
		if ("application".equals(ctmedia) && "x-www-form-urlencoded".equals(ctsubtype)) {
			doGet(req, resp);
			return;
		}
		
		resp.setHeader("X-DAS-Version", DAS_PROTOCOL_VERSION);
		resp.setHeader("X-DAS-Server", DAZZLE_VERSION);
		
		sendError(
				req, 
				resp,
				DASStatus.STATUS_BAD_COMMAND_ARGUMENTS,
				"Bad POSTed content type: " + contentType
		);
	}
	
	//
	// Useful error-handling code.
	//
	
	private void sendError(HttpServletRequest req,
			HttpServletResponse resp,
			int statusCode) 
	throws ServletException, IOException
	{
		log("DAS Error: status=" + statusCode);
		
		resp.setIntHeader("X-DAS-Status", statusCode);
		resp.setContentType("text/plain");
		PrintWriter pw = resp.getWriter();
		String msg = DASStatus.getErrorDescription(statusCode);
		pw.println(msg + " (" + statusCode + ")");
		pw.println();
		pw.println("URL: " + DazzleTools.fullURL(req));
	}
	
	private void sendError(
			HttpServletRequest req,
			HttpServletResponse resp,
			int statusCode,
			String message
	)
	throws ServletException, IOException
	{
		log("DAS Error: " + message);
		
		resp.setIntHeader("X-DAS-Status", statusCode);
		resp.setContentType("text/plain");
		PrintWriter pw = resp.getWriter();
		String msg = DASStatus.getErrorDescription(statusCode);
		pw.println(msg + " (" + statusCode + ")");
		pw.println();
		pw.println("URL: " + DazzleTools.fullURL(req));
		pw.println();
		pw.println(message);
	}
	
	private void sendError(
			HttpServletRequest req,
			HttpServletResponse resp,
			int statusCode,
			Throwable exception
	)
	throws ServletException, IOException
	{
		
		
		resp.setIntHeader("X-DAS-Status", statusCode);
		resp.setContentType("text/plain");
		
		PrintWriter pw = resp.getWriter();
		String msg = DASStatus.getErrorDescription(statusCode);
		pw.println(msg + " (" + statusCode + ")");
		pw.println();
		pw.println("URL: " + DazzleTools.fullURL(req));
		pw.println();
		
		if (statusCode == DASStatus.STATUS_SERVER_ERROR ) {
		      exception.printStackTrace(pw);
		      log("DAS Error", exception);
		}
		pw.close();
		pw.flush();
				
		
	}
	
	
	
	// 
	// Welcome page
	//
	
	private void welcomePage(
			HttpServletRequest req,
			HttpServletResponse resp)
	throws ServletException, IOException
	{
		List<String> dataSourceIDs = null;
		Map<String,DazzleDataSource> dataSources = new HashMap<String, DazzleDataSource>();
		try {
			dataSourceIDs = new ArrayList<String>(installation.getDataSourceIDs(Collections.EMPTY_LIST, req));
			for (Iterator<String> i = dataSourceIDs.iterator(); i.hasNext(); ) {
				String id = i.next();
				try {
					dataSources.put(id, installation.getDataSource(Collections.nCopies(1, id), req));
				} catch (DataSourceException ex) {
					i.remove();
					log("*** WARNING *** Lost a data source " + id);
				}
			}
		} catch (DataSourceException ex) {
			sendError(req, resp, DASStatus.STATUS_SERVER_ERROR, ex);
			return;
		}
		
		resp.setIntHeader("X-DAS-Status", DASStatus.STATUS_OKAY);
		resp.setContentType("text/html");
		PrintWriter pw = resp.getWriter();
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<link rel=\"STYLESHEET\" href=\"css/dazzle.css\" type=\"text/css\" />");
		pw.println("<title>DAS Server information</title>");
		pw.println("</head>");
		pw.println("<body>");
	
		pw.println("<h1>" + DAZZLE_VERSION + "</h1>");
	
		
		
		// Information about this installation
		
		InputStream is = getServletContext().getResourceAsStream(WELCOME_MESSAGE);
		if (is != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				pw.println(line);
			}
		}
		
		
		// DSN list
		
		String path = req.getContextPath() + req.getServletPath();
		//System.out.println("dazzle use path:" + path);
		pw.println("<ul><li>Do a DAS - <a href=\""+path+"/dsn\">DSN</a> request</ul></li>");
		
		pw.println("<h2>Available data sources</h2>");
		pw.println("<table border=\"1\">");
		pw.println("<tr><th>ID</th> <th>Description</th> <th>Version</th> <th>Reference?</th><th>Plugin</th></tr>");
		for (Iterator<String> i = dataSourceIDs.iterator(); i.hasNext(); ) {
			String id =  i.next();
			DazzleDataSource dds = (DazzleDataSource) dataSources.get(id);
			
			pw.println("<tr>");
			pw.println("  <td>" + id + "</td>");
			pw.println("  <td>" + dds.getDescription() + "</td>");
			pw.println("  <td>" + dds.getVersion() + "</td>");
			pw.println("  <td>" + ((dds instanceof DazzleReferenceSource) ? "Yes" : "No") + "</td>");
			pw.println("  <td>" + dds.getDataSourceType() + "/" + dds.getDataSourceVersion() + "</td>");
			pw.println("</tr>");
		}
		pw.println("</table>");
		
		
		pw.println("</body>");
		pw.println("</html>");
		pw.close();
	}    
	
	//
	// DSN command
	//
	
	private void dsnPage(List<String> nameComponents,
			HttpServletRequest req,
			DazzleResponse resp)
	throws ServletException, IOException, DazzleException
	{
		List<String> dataSourceIDs = null;
		Map<String,DazzleDataSource> dataSources = new HashMap<String, DazzleDataSource>();
		try {
			dataSourceIDs = new ArrayList<String>(installation.getDataSourceIDs(nameComponents, req));
			for (Iterator<String> i = dataSourceIDs.iterator(); i.hasNext(); ) {
				String id =  i.next();
				List<String> nc = new ArrayList<String>(nameComponents);
				nc.add(id);
				try {
					dataSources.put(id, installation.getDataSource(nc, req));
				} catch (DataSourceException ex) {
					i.remove();
					log("*** WARNING *** Lost a data source " + id);
				}
			}
		} catch (DataSourceException ex) {
			sendError(req, resp, DASStatus.STATUS_SERVER_ERROR, ex);
			return;
		}
		
		String refBase = req.getRequestURL().toString();
		//deprecated: String refBase = HttpUtils.getRequestURL(req).toString();
		if (refBase.endsWith("dsn")) {
			refBase = refBase.substring(0, refBase.length() - 3);
		} else if (refBase.endsWith("dsn/")) {
			refBase = refBase.substring(0, refBase.length() - 4);
		} else {
			sendError(req, resp, DASStatus.STATUS_BAD_COMMAND);
			return;
		}
		
		XMLWriter xw = resp.startDasXML("DASDSN", "dasdsn.dtd");
		
		xw.openTag("DASDSN");
		for (Iterator<String> i = dataSourceIDs.iterator(); i.hasNext(); ) {
			String id =  i.next();
			DazzleDataSource dataSource = (DazzleDataSource) dataSources.get(id);
			
			xw.openTag("DSN");
			xw.openTag("SOURCE");
			xw.attribute("id", id);
			String version = dataSource.getVersion();
			if (version != null) {
				xw.attribute("version", version);
			}
			xw.print(dataSource.getName());
			xw.closeTag("SOURCE");
			xw.openTag("MAPMASTER");
			if (dataSource instanceof DazzleReferenceSource) {
				String ref = refBase + id + "/";
				xw.print(ref);
			} else {
				xw.print(dataSource.getMapMaster());
			}
			xw.closeTag("MAPMASTER");
			String description = dataSource.getDescription();
			if (description != null) {
				xw.openTag("DESCRIPTION");
				xw.print(description);
				xw.closeTag("DESCRIPTION");
			}
			xw.closeTag("DSN");
		}
		xw.closeTag("DASDSN");
		xw.close();
	}
	
	/** DAS2- style sources description of the DAS sources, as is also supported by the DAS registry.
	 * e.g. see http://www.dasregistry.org/das1/sources/
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void sendSources(HttpServletRequest req,
			HttpServletResponse resp)
	throws ServletException, IOException
	{
		
		//TODO: automatically build up the sources description form the data-sources
		//add optional configuration fields to the dazzleconfig.xml file that allows
		//to specify the information that can not be inferred automatically
		
		System.out.println("getting sources listing from file: " +DAS_SOURCES_LIST );
		
		InputStream is = getServletContext().getResourceAsStream(DAS_SOURCES_LIST);
		
		
		resp.setContentType(XML_CONTENT_TYPE);
		
		PrintWriter pw = resp.getWriter();
		 
		if (is != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				pw.println(line);
			}
		}
		pw.flush();
		
		pw.close();
		
	}
	
	//
	// Old style Dazzle CAPABILITIES command
	//
	/*
	private void capabilitiesPage(HttpServletRequest req,
			DazzleResponse resp)
	throws ServletException, IOException
	{
		XMLWriter xw = resp.startDasXML("DASCAP", "dascap.dtd");
		
		xw.printRaw("<?xml version='1.0' standalone='no' ?>");
		
		xw.openTag("capabilities");
		sendCapability(xw, "featureTable", "dasgff");
		sendCapability(xw, "featureTable", "xff");
		xw.closeTag("capabilities");
		xw.close();
	}
	
	private void sendCapability(XMLWriter xw,
			String type, 
			String value)
	throws IOException
	{
		xw.openTag("capability");
		xw.attribute("type", type);
		if (value != null) {
			xw.attribute("value", value);
		}
		xw.closeTag("capability");
	}*/
	
	//
	// DTD pseudocommand
	//
	
	private void sendDTD(HttpServletRequest req,
			HttpServletResponse resp,
			String dtdName)
	throws ServletException, IOException
	{   
		String dtdPath = "org/biodas/das1/" + dtdName;
		InputStream dtd = getClass().getClassLoader().getResourceAsStream(dtdPath);
		
		if (dtd == null) {
			throw new ServletException("No such DTD: " + dtdName);
		}
		
		resp.setContentType(XML_CONTENT_TYPE);
		resp.setHeader("Content-Encoding", "plain");
		OutputStream os = resp.getOutputStream();
		
		// reading with buffer array to  allow also sending of images
		byte[] buffer = new byte[256];
		int bufMax = 0;
		while (bufMax >= 0) {
			bufMax = dtd.read(buffer);
			if (bufMax > 0)
				os.write(buffer, 0, bufMax);
		}
		os.flush();	
	}
	
	private void sendStylesheet(HttpServletRequest req,
			HttpServletResponse resp,
			String command) 
	throws IOException,ServletException{
					
		String cssPath = "css/" + command;
		InputStream is = getClass().getClassLoader().getResourceAsStream(cssPath);
		
		if (is == null) 
			throw new ServletException("No such css: " + command);
		
		PrintWriter pw = resp.getWriter();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			//System.out.println(line);
			pw.println(line);
		}

    	pw.close();
    	pw.flush();
		
	}
	
	 private void sendXSLFile(HttpServletResponse resp) 
	    throws IOException{
	    	
		 	
	    	resp.setContentType("text/xml");
	    	PrintWriter pw = resp.getWriter();
	    	InputStream is = getServletContext().getResourceAsStream(XSL_FILE);
	    	if (is != null) {
	    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    		String line;
	    		while ((line = br.readLine()) != null) {
	    			//System.out.println(line);
	    			pw.println(line);
	    		}
	    	}
	    	pw.close();
	    	pw.flush();
	    }
	    
	
}
