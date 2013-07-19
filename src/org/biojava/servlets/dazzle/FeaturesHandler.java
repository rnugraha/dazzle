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
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

import org.biojava.utils.xml.*;

import org.biojava.servlets.dazzle.datasource.*;

import java.util.regex.Pattern;

/**
 * Handler which implements the DAS features and types commands.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class FeaturesHandler extends AbstractDazzleHandler {
	private static final String DASGFF_VERSION = "1.0";

	public FeaturesHandler() {
		super(
				BiojavaFeatureSource.class,
				new String[] {"features", "types"},
				new String[] {"types/1.0", "features/1.0", "encoding-dasgff/1.0", "encoding-xff/1.0", "feature-by-id/1.0", "group-by-id/1.0", "component/1.0"}
		);
	}

	private final static String[] EMPTY_STRING_ARRAY = new String[0];
	
    public String[] capabilities(DazzleDataSource dds) {
    	String[] caps = super.capabilities(dds);
    	if (dds instanceof TilingFeatureSource) {
    		List l = new ArrayList(Arrays.asList(caps));
    		l.add("maxbins/1.0");
    		caps = (String[]) l.toArray(EMPTY_STRING_ARRAY);
    	}
        return caps;
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

		if ( ! (dds instanceof BiojavaFeatureSource) ) {
			System.err.println("got DataSource that is not a BiojavaFeaturesource!");
		}

		if("types".equals(cmd)) {
			BiojavaFeatureSource bjfs = (BiojavaFeatureSource) dds ;
			typesCommand(dazzle, req, resp, bjfs);
		} else {
			BiojavaFeatureSource bjfs = (BiojavaFeatureSource) dds ;
			featuresCommand(dazzle, req, resp, bjfs);
		}
	}

	private void featuresCommand(
			DazzleServlet dazzle,
			HttpServletRequest req,
			DazzleResponse resp,
			BiojavaFeatureSource dds
	)
	throws IOException, ServletException, DazzleException, DataSourceException
	{
		List segments = DazzleTools.getSegments(dds, req, resp);

		String[] type = req.getParameterValues("type");
		String[] category = req.getParameterValues("category");
		String encoding = req.getParameter("encoding");
		if (encoding == null) {
			encoding = "dasgff";
		}
		boolean categorize = ("yes".equals(req.getParameter("categorize"))); // WHY WHY WHY?
		int maxbins = -1;
		{
			String sMaxbins = req.getParameter("maxbins");
			if (sMaxbins != null) {
				maxbins = Integer.parseInt(sMaxbins);
			}
		}
		FeatureFilter generalFilter = null;
		try {
			generalFilter = featuresOutput_buildGeneralFilter(dds, type, category);
		} catch (PatternSyntaxException ex) {
			throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, ex);
		}

		// Fetch and validate the requests.

		Map segmentResults = new HashMap();
		for (Iterator i = segments.iterator(); i.hasNext(); ) {
			Segment seg = (Segment) i.next();

			try {
				int length = dds.getLandmarkLength(seg.getReference());
				if (seg.isBounded() && (seg.getMin() < 1 || seg.getMax() > length)) {
					segmentResults.put(seg, "Segment " + seg.toString() + " doesn't fit sequence of length " + length);
				} else {
					FeatureHolder features;
					if (dds instanceof TilingFeatureSource && maxbins >= 0) {
						features = ((TilingFeatureSource) dds).getFeatures(seg.getReference(), maxbins);
					} else {
						features = dds.getFeatures(seg.getReference());
					}
					segmentResults.put(seg, features);
				}
				

			} catch (NoSuchElementException ex) {
				if (DazzleServlet.TOLERATE_MISSING_SEGMENTS) {
					dazzle.log("Ugh, requested segment " + seg.getReference() + " was missing, but we're just going to ignore it.  Heigh ho");
				} else {
					segmentResults.put(seg, "Segment " + seg.getReference() + " was not found.");
				}
			} catch (DataSourceException ex) {
				throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
			}
		}

		//
		// Looks okay -- generate the response document
		//

		if (encoding.equalsIgnoreCase("dasgff")) {
			featuresOutput_dasgff(req, resp, dds, segmentResults, generalFilter, categorize);
		} else if (encoding.equalsIgnoreCase("xff")) {
			featuresOutput_xff(req, resp, dds, segmentResults, generalFilter, categorize);
		} else {
			throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "Bad features encoding: " + encoding);
		}
	}

	private FeatureFilter featuresOutput_buildSegmentFilter(FeatureFilter ff,
			Segment seg)
	{
		if (seg.isBounded()) {
			FeatureFilter newff = new FeatureFilter.ShadowOverlapsLocation(new RangeLocation(seg.getMin(),
					seg.getMax()));
			if (ff != FeatureFilter.all) {
				ff = new FeatureFilter.And(ff, newff);
			} else {
				ff = newff;
			}
		}

		return ff;
	}

	private FeatureFilter featuresOutput_buildGeneralFilter(DazzleDataSource dds,
			String[] type,
			String[] category) 
	throws PatternSyntaxException
	{
		FeatureFilter ff = FeatureFilter.all;
		Set allTypes = dds.getAllTypes();

		if (type != null) {
			Set types = new HashSet();
			for (int t = 0; t < type.length; ++t) {

				// if all types are requested, return all of them...
				// this is to support otter...
				if (type[t].equals("any") || type[t].equals("all")){
					ff = FeatureFilter.all;
					return ff;
				}
				if (DazzleServlet.REGEXP_SUPPORT) {

					boolean added = false;
					Pattern typesRE = Pattern.compile(type[t]);
					for (Iterator i = allTypes.iterator(); i.hasNext(); ) {
						String aType = (String) i.next();
						if (typesRE.matcher(aType).matches()) {
							types.add(aType);
							added = true;
						}
					}

					if (!added) {
						types.add(type[t]);
					}
				} else {

					types.add(type[t]);
				}
			}


			for (Iterator i = types.iterator(); i.hasNext(); ) {
				FeatureFilter newff = new FeatureFilter.ByType((String) i.next());
				if (ff == FeatureFilter.all) {
					ff = newff;
				} else {
					ff = new FeatureFilter.Or(ff, newff);
				}
			}
		}
		if (category != null) {
			for (int t = 0; t < category.length; ++t) {
				if ("component".equals(category[t])) {
					FeatureFilter newff = new FeatureFilter.ByClass(ComponentFeature.class);
					if (ff != FeatureFilter.all) {
						ff = new FeatureFilter.Or(ff, newff);
					} else {
						ff = newff;
					}
				} else {
					if (ff == FeatureFilter.all) {
						ff = FeatureFilter.none;
					}
				}
			}
		}

		// 
		// Ensure that we don't descend past components (this required quite recent BioJava)
		//

		boolean descendThroughComponents = false;

		if (!descendThroughComponents) {
			FeatureFilter notComponentDescendants = new FeatureFilter.Not(
					new FeatureFilter.ByAncestor(
							new FeatureFilter.ByClass(ComponentFeature.class)
					)
			);
			if (ff == FeatureFilter.all) {
				ff = notComponentDescendants;
			} else {
				ff = new FeatureFilter.And(ff, notComponentDescendants);
			}
		}

		return ff;
	}

	/**
	 * Actual FEATURES document emitter for DASGFF.
	 */

	private void writeDasgffFeature(BiojavaFeatureSource dds, 
			XMLWriter xw,
			Feature feature,
			List groups,
			Location forcedLocation,
			String forcedID,
			boolean categorize)
	throws IOException, ServletException
	{


		xw.openTag("FEATURE");
		if (forcedID == null) {
			xw.attribute("id", dds.getFeatureID(feature));
		} else {
			xw.attribute("id", forcedID);
		}
		String label = dds.getFeatureLabel(feature);
		if (label != null) {
			xw.attribute("label", label);
		}

		xw.openTag("TYPE");
		xw.attribute("id", feature.getType());
		if (feature instanceof ComponentFeature) {
			if (categorize) {
				xw.attribute("category", "component");
			}
			xw.attribute("reference", "yes");


			boolean subParts = (feature.filter(new FeatureFilter.ByClass(ComponentFeature.class), false).countFeatures() > 0);
			xw.attribute("subparts", subParts ? "yes" : "no");
		} else {
			if (categorize) {
				xw.attribute("category", "default");
			}
		}
		String description = dds.getTypeDescription(feature.getType());
		if (description == null) {
			description = feature.getType();
		}
		xw.print(description);  // todo: map this nicely
		xw.closeTag("TYPE");

		xw.openTag("METHOD");
		xw.attribute("id", feature.getSource());
		xw.print(feature.getSource());
		xw.closeTag("METHOD");

		{
			Location loc = forcedLocation;
			if (loc == null) {
				loc = feature.getLocation();
			}

			xw.openTag("START");
			xw.print("" + loc.getMin());
			xw.closeTag("START");

			xw.openTag("END");
			xw.print("" + loc.getMax());
			xw.closeTag("END");
		}

		xw.openTag("SCORE");
		String score = dds.getScore(feature);
		if (score != null) {
			xw.print(score);
		} else {
			xw.print("-");
		}
		xw.closeTag("SCORE");

		xw.openTag("ORIENTATION");
		// System.err.print("Doing feature of type " + feature.getClass().toString() + ": ");
		StrandedFeature.Strand strand = StrandedFeature.UNKNOWN;
		if (feature instanceof StrandedFeature) {
			strand = ((StrandedFeature) feature).getStrand();
		} else if (feature.getParent() instanceof StrandedFeature) {
			strand = ((StrandedFeature) feature.getParent()).getStrand();
		}
		// System.err.println(strand.toString());

		if (strand == StrandedFeature.POSITIVE) {
			xw.print("+");
		} else if (strand == StrandedFeature.NEGATIVE) {
			xw.print("-");
		} else {
			xw.print("0");
		}
		xw.closeTag("ORIENTATION");

		xw.openTag("PHASE");
		FramedFeature.ReadingFrame phase = dds.getPhase(feature);
		if (phase == null) {
			xw.print("-");
		} else {
			xw.print("" + phase.getFrame());
		}
		xw.closeTag("PHASE");


		List notes = dds.getFeatureNotes(feature);
		for (Iterator ni = notes.iterator(); ni.hasNext(); ) {
			String note = ni.next().toString();
			xw.openTag("NOTE");
			xw.print(note);
			xw.closeTag("NOTE");
		}

		if (feature instanceof ComponentFeature) {
			ComponentFeature cf = (ComponentFeature) feature;
			Location cfl = cf.getComponentLocation();

			xw.openTag("TARGET");
			xw.attribute("id", cf.getComponentSequence().getName());
			xw.attribute("start", "" + cfl.getMin());
			xw.attribute("stop", "" + cfl.getMax());
			xw.closeTag("TARGET");
		}

		// Now generate DAS-0.999 linkouts.

		if (forcedID == null) {
			Map linkouts = dds.getLinkouts(feature);
			if (linkouts != null && linkouts.size() > 0) {
				for (Iterator li = linkouts.entrySet().iterator(); li.hasNext(); ) {
					Map.Entry link = (Map.Entry) li.next();
					String linkRole = (String) link.getKey();
					String linkURI = (String) link.getValue();

					xw.openTag("LINK");
					xw.attribute("href", linkURI);
					xw.print(linkRole);
					xw.closeTag("LINK");
				} 
			}
		} else {
			// We don't really have an identity ourselves, so should just be linked via the group.
		}

		for (Iterator gi = groups.iterator(); gi.hasNext(); ) {
			Object groupingObject = gi.next();
			String gid;
			String type;
			String glabel;
			Map links;
			List groupNotes;
			if (groupingObject instanceof Feature) {
				Feature parentF = (Feature) groupingObject;
				gid = dds.getFeatureID(parentF);
				if (gid == null) {
					gid = "" + parentF.hashCode();
				}
				type = parentF.getType();
				links = dds.getLinkouts(parentF);
				glabel = dds.getFeatureLabel(parentF);
				groupNotes = Collections.EMPTY_LIST;
			} else if (groupingObject instanceof DASGFFGroup) {
				DASGFFGroup group = (DASGFFGroup) groupingObject;
				gid = group.getGID();
				type = group.getType();
				links = group.getLinkMap();
				glabel = group.getLabel();
				groupNotes = group.getNotes();
			} else {
				throw new BioRuntimeException("Bad grouping object " + groupingObject.toString());
			}

			xw.openTag("GROUP");
			xw.attribute("id", /* feature.getType() + "-" + */ gid);
			xw.attribute("type", type);
			if (glabel != null) {
				xw.attribute("label", glabel);
			}
			if (links != null && links.size() > 0) {
				for (Iterator li = links.entrySet().iterator(); li.hasNext(); ) {
					Map.Entry link = (Map.Entry) li.next();
					String linkRole = (String) link.getKey();
					String linkURI = (String) link.getValue();

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

		xw.closeTag("FEATURE");	
	}


	private void featuresOutput_dasgff(
			HttpServletRequest req,
			DazzleResponse resp,
			BiojavaFeatureSource dds,
			Map segmentResults,
			FeatureFilter generalFilter,
			boolean categorize)
	throws IOException, ServletException, DazzleException
	{

		XMLWriter xw = resp.startDasXML("DASGFF", "dasgff.dtd");

		try {
			xw.openTag("DASGFF");
			xw.openTag("GFF");
			xw.attribute("version", DASGFF_VERSION);
			xw.attribute("href", DazzleTools.fullURL(req));

			for (Iterator i = segmentResults.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry me = (Map.Entry) i.next();
				Segment seg = (Segment) me.getKey();
				Object segv = me.getValue();
				if (segv instanceof FeatureHolder) {
					FeatureHolder features = (FeatureHolder) segv;

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
					// TODO: Labels here?

					FeatureFilter ff = featuresOutput_buildSegmentFilter(generalFilter, seg);
					features = features.filter(ff, true);

					List nullList = Collections.nCopies(1, null);

					for (Iterator fi = features.features(); fi.hasNext(); ) {
						Feature feature = (Feature) fi.next();

						if (dds.getShatterFeature(feature)) {
							int idSeed = 1;
							String baseID = dds.getFeatureID(feature);
							List baseGroupList = dds.getGroups(feature);
							for (Iterator bi = feature.getLocation().blockIterator(); bi.hasNext(); ) {
								Location shatterSpan = (Location) bi.next();
								List groupList = new ArrayList();
								groupList.add(feature);
								groupList.addAll(baseGroupList);
								writeDasgffFeature(dds, xw, feature, groupList, shatterSpan, baseID + "-" + (idSeed++), categorize);
							}
						} else {
							List groups = dds.getGroups(feature);
							writeDasgffFeature(dds, xw, feature, groups, null, null, categorize);
						}
					}

					xw.closeTag("SEGMENT");
				} else if (segv instanceof String) {
					xw.openTag("ERRORSEGMENT");
					xw.attribute("id", seg.getReference());
					if (seg.isBounded()) {
						xw.attribute("start", "" + seg.getStart());
						xw.attribute("stop", "" + seg.getStop());
					} 
					xw.closeTag("ERRORSEGMENT");
				} else if (segv == null) {
					xw.openTag("UNKNOWNSEGMENT");
					xw.attribute("id", seg.getReference());
					xw.closeTag("UNKNOWNSEGMENT");
				}
			}

			xw.closeTag("GFF");
			xw.closeTag("DASGFF");
			xw.close();
		} catch (Exception ex) {
			throw new DazzleException(ex, "Error writing DASGFF FEATURES document");
		}
	}

	/**
	 * Wrapper for the standard XFF-writing code
	 */

	private void featuresOutput_xff(HttpServletRequest req,
			DazzleResponse resp,
			BiojavaFeatureSource dds,
			Map segmentResults,
			FeatureFilter generalFilter,
			boolean categorize)
	throws IOException, ServletException, DazzleException
	{

		XMLWriter xw = resp.startDasXML();

		XFFWriter xffw = new XFFWriter(new DataSourceXFFHelper(dds));
		try {
			xw.openTag("DASFEATURES");

			for (Iterator i = segmentResults.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry me = (Map.Entry) i.next();
				Segment seg = (Segment) me.getKey();
				Object segv = me.getValue();
				if (segv instanceof FeatureHolder) {
					FeatureHolder features = (FeatureHolder) segv;

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
					// TODO: Labels here?

					FeatureFilter ff = featuresOutput_buildSegmentFilter(generalFilter, seg);
					features = features.filter(ff, false); // FIXME!

					xffw.writeFeatureSet(features, xw);

					xw.closeTag("SEGMENT");
				} else if (segv instanceof String) {
					xw.openTag("ERRORSEGMENT");
					xw.attribute("id", seg.getReference());
					if (seg.isBounded()) {
						xw.attribute("start", "" + seg.getStart());
						xw.attribute("stop", "" + seg.getStop());
					} 
					xw.closeTag("ERRORSEGMENT");
				} else if (segv == null) {
					xw.openTag("UNKNOWNSEGMENT");
					xw.attribute("id", seg.getReference());
					xw.closeTag("UNKNOWNSEGMENT");
				}
			}

			xw.closeTag("DASFEATURES");
			xw.close();
		} catch (Exception ex) {
			throw new DazzleException(ex, "Error writing XFF FEATURES document");
		}
	}

	private class DataSourceXFFHelper implements XFFHelper {
		private BiojavaFeatureSource dds;

		DataSourceXFFHelper(BiojavaFeatureSource dds) {
			this.dds = dds;
		}

		public String getFeatureID(Feature f) {
			return dds.getFeatureID(f);
		}

		public void writeDetails(XMLWriter xw, Feature f)
		throws IOException
		{
			// General annotation-writing from Dazzle 0.08.  Is this a good idea?

					Annotation a = f.getAnnotation();
					for (Iterator ai = a.keys().iterator(); ai.hasNext(); ) {
						Object key =  ai.next();
						if (! (key instanceof String))
							continue;
						Object value = a.getProperty(key);
						if (! (value instanceof String))
							continue;


						xw.openTag("biojava:prop");
						xw.attribute("key", (String) key);
						xw.print((String) value);
						xw.closeTag("biojava:prop");
					}

					// Link-writing.  Since 0.93, this is no longer the datasource's responsibility

					Map linkouts = dds.getLinkouts(f);
					if (linkouts != null && linkouts.size() > 0) {
						xw.openTag("das:links");
						xw.attribute("xmlns:das", "http://www.biojava.org/dazzle");
						xw.attribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
						for (Iterator li = linkouts.entrySet().iterator(); li.hasNext(); ) {
							Map.Entry link = (Map.Entry) li.next();
							String linkRole = (String) link.getKey();
							String linkURI = (String) link.getValue();

							xw.openTag("das:link");
							xw.attribute("xlink:role", linkRole);
							xw.attribute("xlink:href", linkURI);
							xw.closeTag("das:link");
						} 
						xw.closeTag("das:links");
					}

					// Any other stuff the datasource wants

					dds.writeXFFDetails(xw, f);
		}
	}

	//
	// TYPES command
	//

	private void typesCommand(
			DazzleServlet dazzle,
			HttpServletRequest req,
			DazzleResponse resp,
			BiojavaFeatureSource dds
	)
	throws IOException, ServletException, DazzleException, DataSourceException
	{
		List segments = DazzleTools.getSegments(dds, req, resp);

		String[] type = req.getParameterValues("type");
		String[] category = req.getParameterValues("category");

		// Fetch and validate the requests.

		Map segmentResults = new HashMap();
		for (Iterator i = segments.iterator(); i.hasNext(); ) {
			Segment seg = (Segment) i.next();

			try {
				int length = dds.getLandmarkLength(seg.getReference());
				if (seg.isBounded() && (seg.getMin() < 1 || seg.getMax() > length)) {
					segmentResults.put(seg, "Segment " + seg.toString() + " doesn't fit sequence of length " + length);
				} else {
					Map typeCounts = new HashMap();
					String[] typesToCalculate;

					if (category == null) {
						String[] _types = type;
						List unhandledTypes = new ArrayList();
						if (_types == null) {
							_types = (String[]) dds.getAllTypes().toArray(new String[0]);
						}
						for (int t = 0; t < _types.length; ++t) {
							String _type = _types[t];
							int cntValue;
							if (seg.isBounded()) {
								cntValue = dds.countFeatures(seg.getReference(),
										seg.getMin(),
										seg.getMax(),
										_type);
							} else {
								cntValue = dds.countFeatures(seg.getReference(), _type);
							}

							if (cntValue != BiojavaFeatureSource.COUNT_CALCULATE) {
								if (cntValue >= 0) {
									typeCounts.put(_type, new Integer(cntValue));
								} else {
									typeCounts.put(_type, null);
								}
							} else {
								unhandledTypes.add(_type);
							}
						}

						if (typeCounts.size() == 0) {
							typesToCalculate = type;
						} else {
							typesToCalculate = (String[]) unhandledTypes.toArray(new String[0]);
						}
					} else {
						typesToCalculate = type;
					}

					if (/* typesToCalculate == null || typesToCalculate.length > 0 */ typesToCalculate != null && typesToCalculate.length > 0) {
						FeatureFilter generalFilter = null;
						try {
							generalFilter = featuresOutput_buildGeneralFilter(dds, typesToCalculate, category);
						} catch (PatternSyntaxException ex) {
							throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, ex);
						}

						FeatureHolder features = dds.getFeatures(seg.getReference());
						FeatureFilter ff = featuresOutput_buildSegmentFilter(generalFilter, seg);
						for (Iterator fi = features.filter(ff, true).features(); fi.hasNext(); ) {
							Feature feature = (Feature) fi.next();
							String t = feature.getType();
							Integer cnt = (Integer) typeCounts.get(t);
							if (cnt != null) {
								typeCounts.put(t, new Integer(cnt.intValue() + 1));
							} else {
								typeCounts.put(t, new Integer(1));
							}
						}
					}

					segmentResults.put(seg, typeCounts);
				}
			} catch (NoSuchElementException ex) {
				if (DazzleServlet.TOLERATE_MISSING_SEGMENTS) {
					dazzle.log("Ugh, requested segment " + seg.getReference() + " was missing, but we're just going to ignore it.  Heigh ho");
				} else {
					segmentResults.put(seg, "Couldn't find segment " + seg.getReference());
				}
			} catch (DataSourceException ex) {
				throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
			}
		}

		//
		// Looks okay -- generate the response document
		//

		XMLWriter xw = resp.startDasXML("DASTYPES", "dastypes.dtd");

		try {
			xw.openTag("DASTYPES");
			xw.openTag("GFF");
			xw.attribute("version", DASGFF_VERSION);
			xw.attribute("href", DazzleTools.fullURL(req));

			if (segmentResults.size() > 0) {
				for (Iterator si = segmentResults.entrySet().iterator(); si.hasNext(); ) {
					Map.Entry me = (Map.Entry) si.next();
					Segment seg = (Segment) me.getKey();
					Object segv = me.getValue();
					if (segv instanceof Map) {
						Map types = (Map) segv;
						typesCommand_writeSegment(xw, dds, seg, types);
					} else if (segv instanceof String) {
						xw.openTag("ERRORSEGMENT");
						xw.attribute("id", seg.getReference());
						if (seg.isBounded()) {
							xw.attribute("start", "" + seg.getStart());
							xw.attribute("stop", "" + seg.getStop());
						} 
						xw.closeTag("ERRORSEGMENT");
					} else if (segv == null) {
						xw.openTag("UNKNOWNSEGMENT");
						xw.attribute("id", seg.getReference());
						xw.closeTag("UNKNOWNSEGMENT");
					}	
				}
			} else {
				Map types = new HashMap();
				for (Iterator i = dds.getAllTypes().iterator(); i.hasNext(); ) {
					String t = (String) i.next();
					types.put(t, null);
				}
				typesCommand_writeSegment(xw, dds, null, types);
			}

			xw.closeTag("GFF");
			xw.closeTag("DASTYPES");
			xw.close();
		} catch (Exception ex) {
			throw new DazzleException(ex, "Error writing DASGFF TYPES document");
		}
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
