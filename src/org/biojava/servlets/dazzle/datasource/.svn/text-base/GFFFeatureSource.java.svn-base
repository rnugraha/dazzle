package org.biojava.servlets.dazzle.datasource;

import org.biojava.servlets.dazzle.Segment;


public interface GFFFeatureSource
    extends DazzleDataSource
{ 


    /** get all features for a given regference
     * @param seg the requested Segment. E.g. an identifier describing the reference of the
     * features, a sequence identifier or a protein structure
     * @param types array of user requested types to display only. null if user did not provide any types, which means response should contain all types.
     * @return an array of GFFFeature objects
     * @throws DataSourceException 
    */
    
    public GFFFeature[] getFeatures(Segment seg, String[] types) 
	throws DataSourceException ;

    
    /**
     * Return an ID string for a feature.  This /may/ be <code>null</code>, but
     * the resulting datasource may then not comply perfectly with DAS 1.0.
     * @param f the feature id
     * @return id for this features
     */

    public String getFeatureID(GFFFeature f);
	
}


