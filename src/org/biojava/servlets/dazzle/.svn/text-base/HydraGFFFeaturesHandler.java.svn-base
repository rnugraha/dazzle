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

import org.biojava.utils.xml.*;
import org.biojava.servlets.dazzle.datasource.*;


/**
 * A Handler that speaks to multiple database backends at the same time.
 * Uses the GFFFeature object for data representation.
 *
 * @author Andreas Prlic
 * @since 1.1
 */

public class HydraGFFFeaturesHandler 
extends AbstractDazzleHandler
{
	private static final String DASGFF_VERSION = "1.0";



	public HydraGFFFeaturesHandler() {
		super(
				HydraGFFFeatureSource.class,
				new String[] {"features"},
				new String[] {"features/1.0", "encoding-dasgff/1.0", "encoding-xff/1.0", "feature-by-id/1.0", "group-by-id/1.0", "component/1.0"}
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
		GFFFeatureSource newdds = (GFFFeatureSource) dds ;

		System.out.println("hydragfffeatureshandler got command " + cmd);
		if("types".equals(cmd)) {
			System.out.println("not implemented... types" );
		} else {
			featuresCommand(dazzle, req, resp, newdds);
		}
	}

	/** parse the DAS source name from the Request and pass it as an argument to the DasSource backend
	 * 
	 * @param dazzle
	 * @param req
	 * @param resp
	 * @param dds
	 * @throws IOException
	 * @throws ServletException
	 * @throws DazzleException
	 * @throws DataSourceException
	 */
	private void featuresCommand(
			DazzleServlet dazzle,
			HttpServletRequest req,
			DazzleResponse resp,
			GFFFeatureSource dds
	)
	throws IOException, ServletException, DazzleException, DataSourceException
	{
		List segments = DazzleTools.getSegments(dds, req, resp);

		System.out.println(segments);

		//String[] type = req.getParameterValues("type");
		//String[] category = req.getParameterValues("category");
		String encoding = req.getParameter("encoding");
		if (encoding == null) {
			encoding = "dasgff";
		}
		//boolean categorize = ("yes".equals(req.getParameter("categorize"))); // WHY WHY WHY?
		
		
		// if types != null filter by type!	
		String[] types = req.getParameterValues("type");
		
		

		// Fetch and validate the requests.

		Map segmentResults = new HashMap();
		for (Iterator i = segments.iterator(); i.hasNext(); ) {
			Segment seg = (Segment) i.next();
			GFFFeature[] features = null ;
			try {
				features = dds.getFeatures(seg,types);

				//int length = dds.getLandmarkLength(seg.getReference());
				segmentResults.put(seg, features);

			} 

			catch (DataSourceException ex) {
				// by AP;
				ex.printStackTrace();
				throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
			}
		}

		//
		// Looks okay -- generate the response document
		//
		writeDASResponse(req, resp, segmentResults, dds);

	}
	
	
	//
	//TODO: remove dedundancy in the code below to GFFFeaturesHandler!!!
	// everything is copied from there below:
	
	protected void writeDASResponse(
			HttpServletRequest req,
			DazzleResponse resp, 
			Map segmentResults,
			GFFFeatureSource dds) throws IOException, DazzleException{
		XMLWriter xw = resp.startDasXML("DASGFF", "dasgff.dtd");
		try {
			xw.openTag("DASGFF");
			xw.openTag("GFF");
			xw.attribute("version", DASGFF_VERSION);
			xw.attribute("href", DazzleTools.fullURL(req));

			for (Iterator i = segmentResults.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry me = (Map.Entry) i.next();
				Segment seg = (Segment) me.getKey();

				xw.openTag("SEGMENT");
				xw.attribute("id", seg.getReference());
				xw.attribute("version", dds.getLandmarkVersion(seg.getReference()));
				if (seg.isBounded()) {
					xw.attribute("start", "" + seg.getStart());
					xw.attribute("stop", "" + seg.getStop());
				} else {
					xw.attribute("start", "1");
					xw.attribute("stop", "" + dds.getLandmarkLength(seg.getReference()));
				}

				GFFFeature[] features = (GFFFeature[])  me.getValue();
				for ( int f = 0 ; f < features.length; f++ ) {
					GFFFeature feature = features[f];
					//System.out.println(feature);
					xw.openTag("FEATURE");
					xw.attribute("id",getFeatureID(feature));
					String label = feature.getLabel();
					if (label != null) {
						xw.attribute("label", label);
					}
					xw.openTag("TYPE");
					xw.attribute("id", feature.getType());
					String description = dds.getTypeDescription(feature.getType());
					if (description == null) {
						description = feature.getType();
					}
					xw.print(description);  // todo: map this nicely
					xw.closeTag("TYPE");
					xw.openTag("METHOD");
					xw.attribute("id", feature.getMethod());
					xw.print(feature.getMethod());
					xw.closeTag("METHOD");
					xw.openTag("START");
					xw.print(feature.getStart());
					xw.closeTag("START");


					xw.openTag("END");
					xw.print(feature.getEnd());
					xw.closeTag("END");

					xw.openTag("SCORE");
					String score = feature.getScore();
					if (score != null) {
						xw.print(score);
					} else {
						xw.print("-");
					}
					xw.closeTag("SCORE");


					String note = feature.getNote();
					if ( note != null ) {
						xw.openTag("NOTE");
						// System.err.print("Doing feature of type " + feature.getClass().toString() + ": ");
						xw.print(note);
						xw.closeTag("NOTE");
					}

					String orient = feature.getOrientation();
					if ( orient != null ) {
						xw.openTag("ORIENTATION");
						// System.err.print("Doing feature of type " + feature.getClass().toString() + ": ");
						xw.print(orient);
						xw.closeTag("ORIENTATION");
					}


					String phase = feature.getPhase();
					if (phase != null) {

						xw.openTag("PHASE");
						xw.print(phase);			 
						xw.closeTag("PHASE");
					}

					// LINK
					String link = feature.getLink();
					if ( link != null ) {
						xw.openTag("LINK");
						xw.attribute("href", link);
						xw.print(link);
						xw.closeTag("LINK");
					}


					// GROUP
					if ( feature.getGroup() != null ){
						printGroup(feature,xw);
					}

					xw.closeTag("FEATURE");
				}
				xw.closeTag("SEGMENT");
			}


			xw.closeTag("GFF");
			xw.closeTag("DASGFF");
			xw.close();


		} catch (Exception ex) {

			ex.printStackTrace();
			throw new DazzleException(ex, "Error writing DASGFF FEATURES document");
		}
	}

	/**
	 * Default implementation which returns an autogenerated ID.  This
	 * takes the form __dazzle__&lt;type&gt;_&lt;refseq&gt;_&lt;start&gt;_&lt;stop&gt.
	 * This method should be overriden whereever possible, but it provides a useful
	 * fallback for features which don't have a natural ID.
	 * @param f the feature
	 * @return returns the name of the faeture
	 */

	public String getFeatureID(GFFFeature f) {
		//return f.getName() ;
		

        StringBuffer sb = new StringBuffer();
        sb.append("__dazzle__");
        sb.append(pack(f.getType()));
        sb.append('_');
        sb.append(pack(f.getName()));
        sb.append('_');
        sb.append(f.getStart());
        sb.append('_');
        sb.append(f.getEnd());
        return sb.toString();
		 
	}

	private String pack(String s) {
		if (s.indexOf('_') < 0) {
			return s;
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s.length(); ++i) {
				char c = s.charAt(i);
				if (c != '_') {
					sb.append(c);
				} else {
					sb.append("__");
				}
			}
			return sb.toString();
		}
	}
	
	private void printGroup(GFFFeature feature, XMLWriter xw) throws IOException{

		DASGFFGroup group = feature.getGroup();
		String gid = group.getGID();
		String gtype = group.getType();
		Map links = group.getLinkMap();
		String glabel = group.getLabel();
		List groupNotes = group.getNotes();
		
		xw.openTag("GROUP");
            xw.attribute("id", /* feature.getType() + "-" + */ gid);
            xw.attribute("type", gtype);
            if (glabel != null) {
                xw.attribute("label", glabel);
            }
            if (links != null && links.size() > 0) {
                for (Iterator li = links.entrySet().iterator(); li.hasNext(); ) {
                    Map.Entry glink = (Map.Entry) li.next();
                    String linkRole = (String) glink.getKey();
                    String linkURI = (String) glink.getValue();
                    
                    xw.openTag("LINK");
                    xw.attribute("href", linkURI);
                    xw.print(linkRole);
                    xw.closeTag("LINK");
                }
                for (Iterator ni = groupNotes.iterator(); ni.hasNext(); ) {
                    xw.openTag("NOTE");
                    xw.print((String) ni.next());
                    xw.closeTag("NOTE");
                }
            }
            xw.closeTag("GROUP");

	}

}
