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


import javax.servlet.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;

import org.biojava.bio.program.das.*;

/**
 * Annotation datasource backed by a BioSQL database
 *
 * @author Thomas Down, Benjamin Schuster-Boeckler
 * @version 0.1
 */

public class BioSQLDataSource extends AbstractDataSource implements DazzleDataSource {
    static {
        try {
            BioSQLDataSource.class.getClassLoader().loadClass("org.postgresql.Driver").newInstance();
        } catch (Exception ex) {
        }
        try {
            BioSQLDataSource.class.getClassLoader().loadClass("org.gjt.mm.mysql.Driver").newInstance();
        } catch (Exception ex) {
        }
    }

    private SequenceDBLite reference;

    private String mapMaster;
    private String stylesheet;

    private String dbURL;
    private String dbUser;
    private String dbPass;
    private String biodatabase;
    private String driver;
    
    private BioSQLSequenceDB sdb;
    private Set              allTypes;

    public String getDataSourceType() {
        return "biosql";
    }
    
    public String getDataSourceVersion() {
        return "0.1";
    }
    
    public void setMapMaster(String s) {
        this.mapMaster = s;
    }
    
    public String getMapMaster() {
        return mapMaster;
    }

    public void setDbURL(String s) {
        dbURL = s;
    }
    
    public void setDbUser(String s) {
        dbUser = s;
    }
    
    public void setDbPass(String s) {
        dbPass = s;
    }
    
    public void setBiodatabase(String s) {
        biodatabase = s;
    }
    
    private void setJDBCDriver(String s) {
        driver = s;
    }
    
    public void init(ServletContext ctx) 
        throws DataSourceException
    {
        super.init(ctx);   
        try {
            reference = new DASSequenceDB(new URL(mapMaster)).allEntryPointsDB();
            
            sdb = new BioSQLSequenceDB(driver,
            dbURL,
            dbUser,
            dbPass,
            biodatabase,
            false);
            allTypes = new HashSet();
            allTypes.add("GD_mRNA");
            allTypes.add("GD_partial_mRNA");
            allTypes.add("GD_ncRNA");
            allTypes.add("Pseudogene");
        } catch (Exception ex) {
            throw new DataSourceException(ex, "Couldn't connect to BioSQL databse");
        }
    }
    
    public Sequence getSequence(String ref)
        throws DataSourceException, NoSuchElementException
    {
        try {
            try {
                return sdb.getSequence(ref);
            } catch (IllegalIDException ex) {
                return reference.getSequence(ref);
            }
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

    public FeatureHolder getFeatures(String ref)
	throws NoSuchElementException, DataSourceException
    {
        Sequence seq = getSequence(ref);
        if (seq instanceof DASSequence) {
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        } else {
            return seq;
        }
    }

    public String getFeatureID(Feature f) {
        Annotation anno = f.getAnnotation();
        if (anno.containsProperty("id")) {
            Object idProp = anno.getProperty("id");
            if (idProp instanceof List) {
                return ((List) idProp).get(0).toString();
            } else {
                return idProp.toString();
            }
        }
        
        return null;
    }
    

    public Set getAllTypes() {
        return Collections.unmodifiableSet(allTypes);
    }

    public void setStylesheet(String s) {
	this.stylesheet = s;
    }

    public String getStylesheet() {
	return stylesheet;
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
    
    public int countFeatures(String reference,
    int min,
    int max,
    String type)
    throws DataSourceException, NoSuchElementException
    {
        try {
            sdb.getSequence(reference);
            return 3000;
        } catch (Exception ex) {}
        return 0;
    }
}
