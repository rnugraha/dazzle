package org.biojava.servlets.dazzle.datasource;

import java.util.*;
import java.io.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;

/** an interface that defines which commands are provided by a Feature service
 * <p>
 * When implementing an annotation source, you need to provide
 * a <code>FeatureHolder</code> containing just those extra
 * features which you want to add to a sequence.  However, BioJava
 * doesn't allow construction of features without an associated
 * sequence.  The easiest approach here is to use the BioJava
 * <code>ViewSequence</code> class, e.g.:
 *
 * <pre>
 * Sequence seq = referenceDB.get(seqID);
 * ViewSequence vseq = new ViewSequence(seq);
 * vseq.createFeature(myFeatureTemplate);
 * // add more features...
 * return vseq.getAddedFeatures();;
 * </pre>
 */
public interface BiojavaFeatureSource extends DazzleDataSource {

	/**
	 * Get the complete set of features for a given sequence landmark.
	 * For large datasources, this might often be a `lazy fetch'
	 * implementation of <code>FeatureHolder</code>
	 *
	 * @param ref the ID of a landmark sequence
	 * @return A <code>FeatureHolder</code> containing all features on that
	 *          landmark.  This might be filtered by Dazzle.
	 * @throws DataSourceException if an error occurs accessing underlying
	 *                             storage, or if the plugin hasn't be
	 *                             correctly configured.
	 * @throws NoSuchElementException if no landmark of the specified name
	 *                                can be found.
	 */

	public FeatureHolder getFeatures(String ref) throws DataSourceException,
			NoSuchElementException;

	/**
	 * Return an ID string for a feature.  This /may/ be <code>null</code>, but
	 * the resulting datasource may then not comply perfectly with DAS 1.0.
	 * @param f 
	 * @return id
	 */

	public String getFeatureID(Feature f);

	/**
	 * Return a display label for a feature.
	 * @param f 
	 * @return label
	 */

	public String getFeatureLabel(Feature f);

	/**
	 * Get the NOTE string to use for the specified feature.  If no notes
	 * are applicable, return an empty list.
	 * @param f 
	 * @return notes
	 */

	public List getFeatureNotes(Feature f);

	/**
	 * Get the phase for a feature
	 * @param f 
	 * @return phase
	 */

	public FramedFeature.ReadingFrame getPhase(Feature f);

	/**
	 * Return a score representation for this feature, or
	 * <code>null</code> if not applicable.
	 * @param f 
	 * @return score
	 *
	 * @since 0.97
	 */

	public String getScore(Feature f);

	/**
	 * Find the features matching a given ID
	 * @param id 
	 * @param matchType 
	 * @return FeatureHolder
	 * @throws DataSourceException 
	 */

	public FeatureHolder getFeaturesByID(String id, MatchType matchType)
			throws DataSourceException;

	/**
	 * Find the features matching a given group ID
	 * @param id 
	 * @param matchType 
	 * @return FeatureHolder
	 * @throws DataSourceException 
	 */

	public FeatureHolder getFeaturesByGroup(String id, MatchType matchType)
			throws DataSourceException;

	//
	// Embedded links
	//

	/**
	 * Return a set of links out from a feature.  This is a map
	 * which can contain zero or more role -> url mappings.
	 *
	 * @param f A feature, which will have been obtained from this datasource
	 * @return a <code>Map</code> of link URLs, or the empty Map.
	 * @since 0.93
	 */

	public Map getLinkouts(Feature f);

	/**
	 * Write out additional `detail' elements in the XFF features dump.
	 * @param xw 
	 * @param f 
	 * @throws IOException 
	 */

	public void writeXFFDetails(XMLWriter xw, Feature f) throws IOException;

	public final static int COUNT_CALCULATE = -1;

	public final static int COUNT_DONTRETURN = -2;

	/**
	 * Optional optimization of the counting of features in a given region.
	 * Can return <code>COUNT_CALCULATE</code>, <code>COUNT_DONTRETURN</code>,
	 * or a positive integer count.
	 * @param reference 
	 * @param type 
	 * @return nr of features
	 * @throws DataSourceException 
	 * @throws NoSuchElementException 
	 *
	 * @since 0.94
	 */

	public int countFeatures(String reference, String type)
			throws DataSourceException, NoSuchElementException;

	/**
	 * Optional optimization of the counting of features in a given region.
	 * Can return <code>COUNT_CALCULATE</code>, <code>COUNT_DONTRETURN</code>,
	 * or a positive integer count.
	 * @param reference 
	 * @param start 
	 * @param end 
	 * @param type 
	 * @return nr featues
	 * @throws DataSourceException 
	 * @throws NoSuchElementException 
	 *
	 * @since 0.94
	 */

	public int countFeatures(String reference, int start, int end, String type)
			throws DataSourceException, NoSuchElementException;

	/**
	 * Return a list of DASGFFGroup objects of which this feature is a member.
	 * This replaces the old getSpoofedGroups method.
	 * @param f 
	 * @return groups
	 *
	 * @since 1.0
	 */

	public List getGroups(Feature f);

	/**
	 * Determine if a non-contiguous feature should be shattered into multiple blocks.
	 * When the DASGFF generator finds a feature who's BioJava location is not
	 * contiguous, it calls this method.  If it returns true, the BioJava feature
	 * is shattered into multiple DAS features, one for each contiguous span
	 * of the location.
	 * @param f 
	 * @return flag 
	 */

	public boolean getShatterFeature(Feature f);

	public static final class MatchType {
		private String name;

		private MatchType(String name) {
			this.name = name;
		}

		public String toString() {
			return "MatchType(" + name + ")";
		}
	}

	public final static MatchType MATCH_EXACT = new MatchType("exact");

	public final static MatchType MATCH_PARTIAL = new MatchType("partial");

}
