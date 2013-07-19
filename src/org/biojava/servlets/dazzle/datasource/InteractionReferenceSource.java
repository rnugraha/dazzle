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

import de.mpg.mpiinf.ag3.dasmi.model.Interaction;

/**
 * Interface of an interaction reference source
 * @author Hagen Blankenburg, Max Planck Institute for Informatics
 *
 */
public interface InteractionReferenceSource extends DazzleReferenceSource {
   
	/**
	 * Request interactions from the data source
	 * @param queries string representations of the query interactors
	 * @param details property/value combinations of details, null otherwise
	 * @param operation union or interections, if null intersection will be used
	 * @return the interactions
	 */
	public Interaction[] getInteractions(String[] queries, String[][] details, String operation);
   
} 
