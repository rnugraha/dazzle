/*
 * Created on Feb 15, 2006
 */
package org.biojava.servlets.dazzle.datasource;

/**
 * Datasources that know how to provide extra metadata about some or all of their
 * types.
 * 
 * @author thomas
 */

public interface TypeMetadataSource {
    public String getTypeMethod(String type);
    public String getTypeEvidenceCode(String type);
    public String getTypeOntology(String type);
    public String getTypeDescriptionString(String type);
    public String getCategory(String type);
}
