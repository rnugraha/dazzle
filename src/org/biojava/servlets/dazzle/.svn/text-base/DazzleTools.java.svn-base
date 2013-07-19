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

import javax.servlet.http.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.servlets.dazzle.datasource.*;

/**
 * Utilities for processing DAS requests.
 *
 * @author Thomas Down
 * @since 1.1
 */
 
public class DazzleTools {
    private DazzleTools() {
    }
    
    /**
     * Generalized routine for extracting segments from a query string.
     * @param dds DazzleDataSource
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     *
     * @return A List<Segment> giving all the valid segments specified by
     *          the query (may be empty), or <code>null</code> is an error
     *          occurred during processing (in which case a DAS error document
     *          will already have been sent to the client).
     * @throws IOException 
     * @throws DazzleException 
     */

    public static List<Segment> getSegments(
        DazzleDataSource dds,
        HttpServletRequest req,
        HttpServletResponse resp
    )
	    throws IOException, DazzleException
    {
        List<Segment> segments = new ArrayList<Segment>();
        
        try {
            BiojavaFeatureSource.MatchType matchType = BiojavaFeatureSource.MATCH_EXACT;
            {
                String matchString = req.getParameter("match");
                if (matchString != null) {
                    if (matchString.equalsIgnoreCase("exact")) {
                        matchType = BiojavaFeatureSource.MATCH_EXACT;
                    } else if (matchString.equalsIgnoreCase("partial")) {
                        matchType = BiojavaFeatureSource.MATCH_PARTIAL;
                    } else {
                        throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS);
                    }
                }
            }
            
            //
            // Old style
            //
            
            String ref = req.getParameter("ref");
            if (ref != null) {
                String starts = req.getParameter("start");
                String stops = req.getParameter("stop");
                
                if (starts == null) {
                    segments.add(new Segment(ref));
                } else {
                    segments.add(new Segment(ref, Integer.parseInt(starts), Integer.parseInt(stops)));
                }
            }
            
            //
            // New style...
            //
            
            String[] newSegs = req.getParameterValues("segment");
            if (newSegs != null) {
                for (int i = 0; i < newSegs.length; ++i) {
                    String newSeg = newSegs[i];
                    StringTokenizer toke = new StringTokenizer(newSeg, ":,");
                    String newRef = toke.nextToken();
                    if (toke.hasMoreTokens()) {
                        String starts = toke.nextToken();
                        String stops = toke.nextToken();
                        segments.add(new Segment(newRef, Integer.parseInt(starts), Integer.parseInt(stops)));
                    } else {
                        segments.add(new Segment(newRef));
                    }
                }
            }
            
	   
	    
	    if (dds instanceof BiojavaFeatureSource) {
		BiojavaFeatureSource bjdds = (BiojavaFeatureSource) dds ;
		
		String[] featureSegs = req.getParameterValues("feature_id");
		if (featureSegs != null) {
		    for (int i = 0; i < featureSegs.length; ++i) {
			String featureID = featureSegs[i];
			FeatureHolder featureInstances = bjdds.getFeaturesByID(featureID, matchType);
			for (Iterator<Feature> fi = featureInstances.features(); fi.hasNext(); ) {
			    Feature f = (Feature) fi.next();
			    segments.add(new Segment(f.getSequence().getName(), f.getLocation().getMin(), f.getLocation().getMax()));
			}
		    }
		}
		
		String[] groupSegs = req.getParameterValues("group_id");
		if (groupSegs != null) {
		    Map<String, List<Location>> locsBySeqName = new HashMap<String, List<Location>>();
		    for (int i = 0; i < groupSegs.length; ++i) {
			String groupID = groupSegs[i];
			FeatureHolder groupInstances = bjdds.getFeaturesByGroup(groupID, matchType);
			for (Iterator<Feature> fi = groupInstances.features(); fi.hasNext(); ) {
			    Feature f = (Feature) fi.next();
			    String seqName = f.getSequence().getName();
			    List<Location> locs = (List<Location>) locsBySeqName.get(seqName);
			    if (locs == null) {
				locs = new ArrayList<Location>();
				locsBySeqName.put(seqName, locs);
			    }
			    locs.add(f.getLocation());
			}
		    }
		    for (Iterator seqi = locsBySeqName.entrySet().iterator(); seqi.hasNext(); ) {
			Map.Entry seqme = (Map.Entry) seqi.next();
			String seqName = (String) seqme.getKey();
			Location locs = LocationTools.union((List) seqme.getValue());
			segments.add(new Segment(seqName, locs.getMin(), locs.getMax()));
		    }
		}
            }
        } catch (NumberFormatException ex) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, ex);
        } catch (NoSuchElementException ex) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, ex);
        } catch (DataSourceException ex) {
            throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
        }
        
        return segments;
    }
    
    public static String fullURL(HttpServletRequest req) 
    {
        StringBuffer u = req.getRequestURL(); 
        String q = req.getQueryString();
        if (q != null) {
            u.append('?');
            u.append(req.getQueryString());
        }
        return u.toString();
    }
}
