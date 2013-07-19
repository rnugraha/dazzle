/*
 *                  BioJava development code
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
 * Created on Aug 15, 2007
 * 
 */

package org.biojava.servlets.dazzle.datasource;

/** serves as a frontend to serving features from multiple backends.
 * E.g. the DAS / GFF uploads to ensembl, the DAS writeback server.
 * 
 *   The actual decision which backend to talk to is based on the dasSourceName
 *   and is done by the implenting dataSource.
 * 
 */ 
public interface HydraGFFFeatureSource 
    extends DazzleDataSource
{ 


    /** serves as a frontend to serving features from multiple backends.
     * E.g. the DAS / GFF uploads to ensembl, the DAS writeback server.
     * 
     *   The actual decision which backend to talk to is based on the dasSourceName
     *   and is done by the implenting dataSource.
     * 
     * get all features for a given reference
     * @param dasSourceName the name of the DAS source that has been called.
     * @param ref an identifier describing the reference of the
     * features, eg. a sequence identifier or a protein structure
     * @return an array of GFFFeature objects
     * @throws DataSourceException 
    */
    
    public GFFFeature[] getFeatures(String dasSourceName, String ref) 
	throws DataSourceException ;

	
}


