package org.biojava.servlets.dazzle.datasource;

import java.util.NoSuchElementException;

import org.biojava.bio.seq.FeatureHolder;

/**
 * Interface for sources which understand maxbins.
 * 
 * @author Thomas Down
 *
 */
public interface TilingFeatureSource extends BiojavaFeatureSource {
	
	/**
	 * Return a FeatureHolder which behaves exactly like that returned by the getFeatures(String) method,
	 * but which optionally takes the maxbins parameter into account when answering its feature-retrieval
	 * method.
	 * 
	 * @param seq
	 * @param maxbins
	 * @return
	 * @throws DataSourceException
	 * @throws NoSuchElementException
	 */
	public FeatureHolder getFeatures(String seq, int maxbins) throws DataSourceException, NoSuchElementException;
}
