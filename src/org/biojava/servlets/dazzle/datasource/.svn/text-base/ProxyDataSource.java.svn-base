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

import java.util.*;
import java.net.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

import org.biojava.bio.program.das.*;
import org.biojava.bio.program.gff.*;

import org.biojava.utils.xml.*;

/**
 * Annotation datasource backed by a remote DAS source.
 *
 * @author Thomas Down
 * @version 0.91
 */

public class ProxyDataSource extends AbstractDataSource implements DazzleDataSource {
    private String mapMaster;
    private String remoteAnnotationSource;
    private URL mapMasterURL;
    private URL remoteAnnotationURL;

    private DASSequenceDB dsdb;
    private CacheMap      recentSeqs = new FixedSizeMap(100);

    private Set allTypes;

    public String getDataSourceType() {
        return "proxy";
    }
    
    public String getDataSourceVersion() {
        return "0.91";
    }
    
    public void setMapMaster(String s) {
	this.mapMaster = s;
    }

    public String getMapMaster() {
	return mapMaster;
    }

    public void setRemoteAnnotationSource(String s) {
	remoteAnnotationSource = s;
    }

    public void init(ServletContext ctx) 
        throws DataSourceException
    {
        super.init(ctx);
        try {
            mapMasterURL = new URL(mapMaster);
            dsdb = new DASSequenceDB(mapMasterURL);
            remoteAnnotationURL = new URL(remoteAnnotationSource);
        } catch (Exception ex) {
            throw new DataSourceException(ex, "Couldn't connect to map master");
        }
    }
    
    public Sequence getSequence(String ref)
	throws DataSourceException, NoSuchElementException
    {
	try {
	    DASSequence ds = (DASSequence) recentSeqs.get(ref);
	    if (ds != null) {
		return ds;
	    }    

	    ds = (DASSequence) dsdb.allEntryPointsDB().getSequence(ref);

	    // System.err.println("Processing " + ds.getName());
	    boolean containsAnno = false;
	    Set oldAnnotation = new HashSet(ds.dataSourceURLs());
	    for (Iterator i = oldAnnotation.iterator(); i.hasNext(); ) {
		URL au = (URL) i.next();
		// System.err.println(au.toString());
		if (! (au.equals(remoteAnnotationURL))) {
		    ds.removeAnnotationSource(au);
		    // System.err.println("Removing");
		} else {
		    containsAnno = true;
		    // System.err.println("Keeping");
		}
	    }

	    if (!containsAnno) {
		ds.addAnnotationSource(remoteAnnotationURL);
	    }

	    recentSeqs.put(ref, ds);
	    return ds;
	} catch (IllegalIDException ex) {
	    throw new NoSuchElementException("Bad reference sequence: " + ref);
        } catch (BioException ex) {
	    throw new DataSourceException(ex, "Error getting reference sequence");
	} catch (ChangeVetoException ex) {
	    throw new DataSourceException(ex, "Unexpected change veto");
	}
    }


    public String getLandmarkVersion(String ref) 
	    throws DataSourceException, NoSuchElementException
    {
        Sequence seq = getSequence(ref);
        try {
            return seq.getAnnotation().getProperty(DASSequence.PROPERTY_SEQUENCEVERSION).toString();
        } catch (NoSuchElementException ex) {
            return "unknown";
        }
    }

    public FeatureHolder getFeatures(String ref)
	    throws NoSuchElementException, DataSourceException
    {
        return getSequence(ref).filter(new FeatureFilter.ByAnnotation(DASSequence.PROPERTY_ANNOTATIONSERVER, remoteAnnotationURL), true);
    }

    public String getFeatureID(Feature f) {
        Annotation anno = f.getAnnotation();
        if (anno.containsProperty(DASSequence.PROPERTY_FEATUREID)) {
            Object idProp = anno.getProperty(DASSequence.PROPERTY_FEATUREID);
            if (idProp instanceof List) {
                return ((List) idProp).get(0).toString();
            } else {
                return idProp.toString();
            }
        }
        
        return null;
    }
    
    public String getFeatureLabel(Feature f) {
        return null;
    }

    public Set getAllTypes() {
        if (allTypes == null) {
            try {
                allTypes = DAS.getTypes(remoteAnnotationURL);
            } catch (BioException ex) {
                throw new BioRuntimeException(ex, "Communications error with DAS server");
            }
        } 
        
        return allTypes;
    }

    public String getTypeDescription(String type) {
        return null;
    }

    public Map getLinkouts(Feature f) {
        Map anno = f.getAnnotation().asMap();
        Map links = new HashMap();
        for (Iterator i = anno.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry me = (Map.Entry) i.next();
            String key = me.getKey().toString();
            if (key.startsWith("href:")) {
                Object o = me.getValue();
                if (o instanceof List) {
                    links.put(key.substring(5), ((List) o).get(0));
                } else {
                    links.put(key.substring(5), o);
                }
            }
        }
        
        return links;
    }
}
