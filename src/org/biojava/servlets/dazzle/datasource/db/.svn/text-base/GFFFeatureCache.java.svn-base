
package org.biojava.servlets.dazzle.datasource.db ;

import org.biojava.servlets.dazzle.datasource.GFFFeature;

/** an interface that defines how a cache for features looks like */

public interface GFFFeatureCache {

    /** see if cache contains an entry for a paritucal accession code
     * and a das source 
     * @param seqId 
     * @param dassource 
     * @return flag if is cached*/    
    public boolean isCached(String seqId, String dassource);

     /** store features in cache 
     * @param seqId 
     * @param dassource 
     * @param features */
    public void cacheFeatures(String seqId, String dassource, GFFFeature[] features);


    /** retrieve all Features from cache for a particular sequence
     * @param seqId 
     * @param dassource 
     * @return  features
     */    
    public GFFFeature[] retrieveFeatures(String seqId,String dassource);

    /** empty the conent of the cache */
    public void clearCache();

}
