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
import java.util.regex.PatternSyntaxException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.utils.xml.*;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.servlets.dazzle.datasource.*;


/**
 * Handler which implements the DAS features and types commands.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class GFFFeaturesHandler extends AbstractDazzleHandler {
	private static final String DASGFF_VERSION = "1.0";

	public GFFFeaturesHandler() {
		super(
				GFFFeatureSource.class,
				new String[] {"features","types"},
				new String[] {"features/1.0", "encoding-dasgff/1.0", "encoding-xff/1.0", "feature-by-id/1.0", "group-by-id/1.0", "component/1.0", "types/1.0"}
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

		//System.out.println("gfffeatureshandler got command " + cmd);
		if("types".equals(cmd)) {
			allTypesCommand(dazzle, req, resp, newdds);
		} else {
			featuresCommand(dazzle, req, resp, newdds);
		}
	}



	//
	// TYPES command
	//

	private void allTypesCommand(
			DazzleServlet dazzle,
			HttpServletRequest req,
			DazzleResponse resp,
			GFFFeatureSource dds
	)
	throws IOException, ServletException, DazzleException, DataSourceException
	{
		dds.getAllTypes();
		XMLWriter xw = resp.startDasXML("DASTYPES", "dastypes.dtd");

		try {
			xw.openTag("DASTYPES");
			xw.openTag("GFF");
			xw.attribute("version", DASGFF_VERSION);
			xw.attribute("href", DazzleTools.fullURL(req));

			
				Map types = new HashMap();
				for (Iterator i = dds.getAllTypes().iterator(); i.hasNext(); ) {
					String t = (String) i.next();
					types.put(t, null);
				}
				typesCommand_writeSegment(xw, dds, null, types);
			

			xw.closeTag("GFF");
			xw.closeTag("DASTYPES");
			xw.close();
		} catch (Exception ex) {
			throw new DazzleException(ex, "Error writing DASGFF TYPES document");
		}
	}

	private void featuresCommand(
			DazzleServlet dazzle,
			HttpServletRequest req,
			DazzleResponse resp,
			GFFFeatureSource dds
	)
	throws IOException, ServletException, DazzleException, DataSourceException
	{
		List<Segment> segments = DazzleTools.getSegments(dds, req, resp);

		if ( segments.size() == 0)
			throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS,"you did not provide a segment argument!");
		
		
		String[] types = req.getParameterValues("type");
		// if types != null filter by type!
		
		//System.out.println(segments);

		//String[] type = req.getParameterValues("type");
		//String[] category = req.getParameterValues("category");
		String encoding = req.getParameter("encoding");
		if (encoding == null) {
			encoding = "dasgff";
		}
		//boolean categorize = ("yes".equals(req.getParameter("categorize"))); // WHY WHY WHY?


		// Fetch and validate the requests.

		Map<Segment,GFFFeature[]> segmentResults = new HashMap<Segment, GFFFeature[]>();
		for (Iterator<Segment> i = segments.iterator(); i.hasNext(); ) {
			Segment seg = (Segment) i.next();
			GFFFeature[] features = null ;
			try {
				features = dds.getFeatures(seg,types);

				//System.out.println("Handler got " + features.length + " features");
				
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
					xw.attribute("id",dds.getFeatureID(feature));
					String label = feature.getLabel();
					if (label != null) {
						xw.attribute("label", label);
					}
					xw.openTag("TYPE");
					if ( feature.getTypeId() != null)
					   xw.attribute("id", feature.getTypeId());					   
					else
					   xw.attribute("id", feature.getType());
					
					if ( feature.getTypeCategory() != null) {
                       xw.attribute("category", feature.getTypeCategory());
                    }
					String description = dds.getTypeDescription(feature.getType());
					if (description == null) {
						description = feature.getType();
					}
					xw.print(description);  // now ontology compliant
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


	private void printGroup(GFFFeature feature, XMLWriter xw) throws IOException{

		DASGFFGroup group = feature.getGroup();
		String gid = group.getGID();
		String gtype = group.getType();
		Map links = group.getLinkMap();
		String glabel = group.getLabel();
		List<String> groupNotes = group.getNotes();
		
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
                for (Iterator<String> ni = groupNotes.iterator(); ni.hasNext(); ) {
                    xw.openTag("NOTE");
                    xw.print((String) ni.next());
                    xw.closeTag("NOTE");
                }
            }
            xw.closeTag("GROUP");

	}

	
	private void typesCommand_writeSegment(XMLWriter xw,
			DazzleDataSource dds,
			Segment seg,
			Map types)
	throws IOException, DataSourceException
	{
		xw.openTag("SEGMENT");
		if (seg != null) {
			xw.attribute("id", seg.getReference());
			xw.attribute("version", dds.getLandmarkVersion(seg.getReference()));
			if (seg.isBounded()) {
				xw.attribute("start", "" + seg.getStart());
				xw.attribute("stop", "" + seg.getStop());
			} else {
				xw.attribute("start", "1");
				xw.attribute("stop", "" + dds.getLandmarkLength(seg.getReference()));
			}
		} else {
			xw.attribute("version", dds.getVersion());
		}

		for (Iterator i = types.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry me = (Map.Entry) i.next();
			String type = (String) me.getKey();
			Integer count = (Integer) me.getValue();
			xw.openTag("TYPE");
			xw.attribute("id", type);
			if (dds instanceof TypeMetadataSource) {
				TypeMetadataSource tmds = (TypeMetadataSource) dds;
				String tMethod = tmds.getTypeMethod(type);
				if (tMethod != null) {
					xw.attribute("method", tMethod);
				}
				String tCategory = tmds.getCategory(type);
				if (tCategory != null) {
					xw.attribute("category", tCategory);
				}
				String tDescription = tmds.getTypeDescriptionString(type);
				if (tDescription != null) {
					xw.attribute("description", tDescription);
				}
				String tEvidence = tmds.getTypeEvidenceCode(type);
				if (tEvidence != null) {
					xw.attribute("evidence", tEvidence);
				}
				String tOntology = tmds.getTypeOntology(type);
				if (tOntology != null) {
					xw.attribute("ontology", tOntology);
				}
			}
			if (count != null) {
				xw.print(count.toString());
			}
			xw.closeTag("TYPE");
		}
		xw.closeTag("SEGMENT");
	}
	


}
