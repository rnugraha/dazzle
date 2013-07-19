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
 * Reference datasource backed by a remote DAS source.
 *
 * @author Thomas Down
 * @version 0.91
 */

public class ProxyReferenceSource extends AbstractDataSource implements DazzleReferenceSource {
    private String remoteReferenceSource;
    private URL remoteReferenceURL;

    private DASSequenceDB dsdb;
    private CacheMap recentSeqs = new FixedSizeMap(100);

    private Set allTypes;

    public String getDataSourceType() {
        return "proxy";
    }
    
    public String getDataSourceVersion() {
        return "0.91";
    }
    
    public String getMapMaster() {
        return null;
    }

    public void setRemoteReferenceSource(String s) {
        remoteReferenceSource = s;
    }

    public void init(ServletContext ctx) 
        throws DataSourceException
    {
        super.init(ctx);
        try {
            remoteReferenceURL = new URL(remoteReferenceSource);
            dsdb = new DASSequenceDB(remoteReferenceURL);
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
            recentSeqs.put(ref, ds);
            return ds;
        } catch (IllegalIDException ex) {
            throw new NoSuchElementException("Bad reference sequence: " + ref);
        } catch (BioException ex) {
            throw new DataSourceException(ex, "Error getting reference sequence");
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
    
    public Set getEntryPoints() {
        return dsdb.ids();
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
    

    public Set getAllTypes() {
        if (allTypes == null) {
            try {
                allTypes = DAS.getTypes(remoteReferenceURL);
            } catch (BioException ex) {
                throw new BioRuntimeException(ex, "Communications error with DAS server");
            }
        } 

        return allTypes;
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
