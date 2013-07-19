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
import java.io.*;
import java.sql.*;
import java.net.*;
import javax.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.utils.*;
import org.biojava.utils.xml.*;
import org.biojava.utils.cache.*;

/**
 * Datasource for pulling annotation out of LDAS tables.
 * 
 * @author Thomas Down
 * @version 1.00
 */

public class LdasDataSource extends AbstractDataSource implements DazzleDataSource {
    private static final int TILE_THRESHOLD = 5000;
    private static final int TILE_SIZE = 200000;
    private String mapMaster;
    private String dbURL;
    private String dbUser;
    private String dbPass;
    private String linkoutPrefix;

    private DataSource connectionPool;
    private CacheMap featureSets = new FixedSizeMap(30);
    private Cache tileCache = new FixedSizeCache(30);

    private Map typesByID = null;
    private Map sourcesByID = null;

    private SequenceDBLite reference;
    
    public Sequence getSequence(String ref)
        throws DataSourceException, NoSuchElementException
    {
        try {
            Sequence seq = reference.getSequence(ref);
            int dummy = seq.length();  // ensure fetching...
            return seq;
        } catch (IllegalIDException ex) {
            return null;
        } catch (Exception ex) {
            throw new NoSuchElementException("Bad reference sequence: " + ref);
        }
    }
    
    public String getLandmarkVersion(String ref) 
        throws DataSourceException, NoSuchElementException
    {
        if (getSequence(ref) != null) {
            return "unknown";
        } else {
            return null;
        }
    }
    
    public String getDataSourceType() {
        return "ldas";
    }
    
    public String getDataSourceVersion() {
        return "1.00";
    }
    
    public void setDbURL(String s) {
        this.dbURL = s;
    }

    public String getDbURL() {
        return this.dbURL;
    }
  
    public void setDbUser(String s) {
        dbUser = s;
    }
  
    public String getDbUser() {
        return this.dbUser;
    }
  
    public void setDbPass(String s) {
        dbPass = s;
    }

    public void setMapMaster(String s) {
        this.mapMaster = s;
    }

    public String getMapMaster() {
        return mapMaster;
    }
    
    public void setLinkoutPrefix(String s) {
        this.linkoutPrefix = s;
    }

    public void init(ServletContext ctx) 
        throws DataSourceException
    {
        super.init(ctx);
        
        try {
            connectionPool = JDBCPooledDataSource.getDataSource(
                "org.gjt.mm.mysql.Driver",
                dbURL,
                dbUser,
                dbPass
            );
            
            SequenceDB refSeqs = new HashSequenceDB();
            
            Connection con = connectionPool.getConnection();
            PreparedStatement get_components = con.prepareStatement(
                "select fref, fstart, fstop from fdata, ftype where ftype.ftypeid = fdata.ftypeid and ftype.fmethod = 'Component'"
            );
            ResultSet rs = get_components.executeQuery();
            while (rs.next()) {
                String fref = rs.getString(1);
                int fstart = rs.getInt(2);
                int fstop = rs.getInt(3);
                refSeqs.addSequence(
                    new SimpleSequence(
                        new DummySymbolList(DNATools.getDNA(), fstop),
                        fref,
                        fref,
                        Annotation.EMPTY_ANNOTATION
                    )
                );
            }
            rs.close();
            get_components.close();
            con.close();
            
            reference = refSeqs;
        } catch (Exception e) {
            throw new DataSourceException(e, "Coundn't initialize LDAS datasource");
        }
    }


    protected Map getTypesByID() {
        if (typesByID == null) {
            initTypes();
        }
        return typesByID;
    }
    
    protected Map getSourcesByID() {
        if (sourcesByID == null) {
            initTypes();
        }
        return sourcesByID;
    }
    
    private void initTypes() {
        Map _types = new HashMap();
        Map _sources = new HashMap();
        try {
            Connection conn = connectionPool.getConnection();
            PreparedStatement get_types = conn.prepareStatement(
                "select ftypeid, fmethod, fsource " +
                "  from ftype"
            );
            ResultSet rs = get_types.executeQuery();
            while (rs.next()) {
                int type = rs.getInt(1);
                String method = rs.getString(2);
                String source = rs.getString(3);
                Integer typeKey = new Integer(type);
                _types.put(typeKey, method + ':' + source);
                _sources.put(typeKey, method);
            }
            rs.close();
            get_types.close();
            conn.close();
        } catch (SQLException ex) {
            log("Couldn't fetch types from LDAS", ex);
        }
        typesByID = _types;
        sourcesByID = _sources;
    }
    
    public FeatureHolder getFeatures(String ref)
	    throws DataSourceException, NoSuchElementException
    {
        FeatureHolder features = (FeatureHolder) featureSets.get(ref);
        if (features != null) {
            return features;
        }
        
        Sequence rawSeq = getSequence(ref);
        if (rawSeq == null) {
            return null;
        }
        
        int count = 0;
        
        try {
            Connection con = connectionPool.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select count(*) from fdata where fref = '" + ref + "'");
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            st.close();
            con.close();
        } catch (SQLException ex) {
            throw new DataSourceException(ex, "Couldn't check features in database");
        }
        
        if (count == 0) {
            features = FeatureHolder.EMPTY_FEATURE_HOLDER;
        } else if (count < TILE_THRESHOLD) {
            ViewSequence vs = new ViewSequence(rawSeq);
            try {
                fetchFeatures(vs, ref, -1, -1);
            } catch (Exception ex) {
                throw new DataSourceException(ex, "Exception fetching features");
            }
            features = vs.getAddedFeatures();
        } else {
            features = new TiledFeatures(rawSeq, TILE_SIZE); 
        }
        
        featureSets.put(ref, features);
        return features;
    }

    public String getFeatureID(Feature f) {
        if (f instanceof ComponentFeature) {
            return ((ComponentFeature) f).getComponentSequence().getName();
        } else {
            Annotation ann = f.getAnnotation();
            StringBuffer sb = new StringBuffer();
            if (ann.keys().contains("group_class")) {
                sb.append(ann.getProperty("group_class"));
                sb.append(':');
            }
            if (ann.keys().contains("group_name")) {
                sb.append(ann.getProperty("group_name"));
                sb.append('/');
            }
            if (ann.keys().contains("id")) {
                sb.append(ann.getProperty("id"));
            }
            return sb.toString();
        }
    }
    
    public String getFeatureLabel(Feature f) {
        Annotation ann = f.getAnnotation();
        if (ann.containsProperty("group_name")) {
            return (String) ann.getProperty("group_name");
        } else {
            return null;
        }
    }

    public Set getAllTypes() 
    {
        return new HashSet(getTypesByID().values());
    }

    public String getTypeDescription(String type) {
        return null;
    }
    
    public Map getLinkouts(Feature f) {
        Map links = new SmallMap();
        if (linkoutPrefix != null && f.getAnnotation().containsProperty("group_name")) {
            String group_name = (String) f.getAnnotation().getProperty("group_name");
            links.put(group_name, linkoutPrefix + group_name);
        }
        return links;
    }
    
    public List getGroups(Feature f) {
        String group_class = null;
        String group_name = null;
        Annotation ann = f.getAnnotation();
        if (ann.containsProperty("group_class")) {
            group_class = (String) ann.getProperty("group_class");
        }
        if (ann.containsProperty("group_name")) {
            group_name = (String) ann.getProperty("group_name");
        }
        if (group_name != null) {
            return Collections.singletonList(
                new DASGFFGroup(group_class + ':' + group_name, group_class)
            );
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    public String getScore(Feature f) {
        Annotation ann = f.getAnnotation();
        if (ann.containsProperty("score")) {
            return ann.getProperty("score").toString();
        } else {
            return null;
        }
    }

    public void fetchFeatures(FeatureHolder seq, String ref, int min, int max)
	    throws BioException, ChangeVetoException
    {
        // System.err.println("Fetching features from " + min + " to " + max);
        
        try {
            String featureQuery =
                "select " +
                "  fdata.fid, fdata.fref, fdata.fstart, fdata.fstop, fdata.ftypeid, fdata.fscore, fdata.fstrand, fdata.fphase, fgroup.gclass, fgroup.gname" +
                "  from fdata, fgroup " +
                " where fref = '" + ref + "' and fgroup.gid = fdata.gid";

            if (min >= 0) {
                // Fetch by position
                featureQuery = featureQuery + " and fstart <= '" + max +"' and fstop >= '" + min + "'";
            }
	    
            Connection con = connectionPool.getConnection();
	        Statement state = con.createStatement();
            ResultSet rs = state.executeQuery(featureQuery);
	    
            StrandedFeature.Template templ = new StrandedFeature.Template();
	    
            while(rs.next()) {
                int id = rs.getInt(1);
                String seqName = rs.getString(2);
                int fmin = rs.getInt(3);
                int fmax = rs.getInt(4);
                Integer typeKey = new Integer(rs.getInt(5));
                String score = rs.getString(6);
                String strand = rs.getString(7);
                String phase = rs.getString(8);
                String gclass = rs.getString(9);
                String gname = rs.getString(10);
            
                templ.location = new RangeLocation(fmin, fmax);
            
                StrandedFeature.Strand strandObj = null;
                if (strand == null) {
                    strandObj = StrandedFeature.UNKNOWN;
                } else if ("+".equals(strand)) {
                    strandObj = StrandedFeature.POSITIVE;
                } else if ("-".equals(strand)) {
                    strandObj = StrandedFeature.NEGATIVE;
                }
                
                templ.strand = strandObj;
                templ.type = (String) getTypesByID().get(typeKey);
                templ.source = (String) getSourcesByID().get(typeKey);
                Annotation ann = new SmallAnnotation();
                ann.setProperty("id", new Integer(id));
                if (score != null && score.length() > 0) {
                    ann.setProperty("score", score);
                }
                if (phase != null && phase.length() > 0) {
                    ann.setProperty("phase", new Integer(phase));
                }
                ann.setProperty("group_class", gclass);
                ann.setProperty("group_name", gname);
                templ.annotation = ann;
                
                if (templ.source.equalsIgnoreCase("Component")) {
                    // This is a magic feature which we don't want to serve up...
                } else {
                    seq.createFeature(templ);
                }
            }
            rs.close();
            state.close();
            con.close();
        } catch (SQLException sqle) {
            throw new BioException(
                sqle,
                "Database access failed when reading generic features for "
                + ref
            );
        } 
    }

    private class TiledFeatures implements FeatureHolder {
        private CacheReference[] featureSets;
        private Location[] tileSpans;
        private SimpleFeatureHolder spanningFeatures = new SimpleFeatureHolder();
        private MergeFeatureHolder allFeatures;
        private Sequence refSeq;

    
        public FeatureFilter getSchema() {
            return new FeatureFilter.And(FeatureFilter.top_level, FeatureFilter.leaf);
        }
    
	    TiledFeatures(Sequence refSeq, int tileSize) {
            this.refSeq = refSeq;
            int numTiles = (int) Math.ceil((1.0 * refSeq.length()) / tileSize);
            featureSets = new CacheReference[numTiles];
            tileSpans = new Location[numTiles];
            
            allFeatures = new MergeFeatureHolder();
            try {
                for (int i = 0; i < numTiles; ++i) {
                    tileSpans[i] = new RangeLocation(i * tileSize + 1, 
                    Math.min(refSeq.length(), (i + 1) * tileSize));
                    allFeatures.addFeatureHolder(new Tile(i));
                }
                allFeatures.addFeatureHolder(spanningFeatures);
            } catch (ChangeVetoException ex) {
                throw new BioError(ex, "Assertion failure: couldn't modify MargeFeatureHolder");
            }
        }

        public Iterator features() {
            return allFeatures.features();
        }

        public FeatureHolder filter(FeatureFilter ff) {
            // System.err.println("Filtering: " + ff);
            return allFeatures.filter(ff);
        }
    
	    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
            // System.err.println("Filtering: " + ff);
            return allFeatures.filter(ff, recurse);
        }

        public int countFeatures() {
            return allFeatures.countFeatures();
        }

        public boolean containsFeature(Feature f) {
            return allFeatures.containsFeature(f);
        }

        public Feature createFeature(Feature.Template templ)
	        throws ChangeVetoException
        {
            throw new ChangeVetoException("NO");
        }

        public void removeFeature(Feature f) 
	        throws ChangeVetoException
        {
            throw new ChangeVetoException("NO");
        }

        public void addChangeListener(ChangeListener cl) {}
        public void addChangeListener(ChangeListener cl, ChangeType ct) {}
        public void removeChangeListener(ChangeListener cl) {}
        public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
        public boolean isUnchanging(ChangeType ct) { return true; }
	
	    private class Tile implements FeatureHolder {
            private int tileNum;

            public FeatureFilter getSchema() {
                return new FeatureFilter.And(
                    new FeatureFilter.ContainedByLocation(tileSpans[tileNum]),
                    new FeatureFilter.And(FeatureFilter.top_level, FeatureFilter.leaf)
                );
            }
        
	        Tile(int tileNum) {
                this.tileNum = tileNum;
            }

            private synchronized SimpleFeatureHolder getFeatures() {
                if (featureSets[tileNum] != null) {
                    SimpleFeatureHolder fh = (SimpleFeatureHolder) featureSets[tileNum].get();
                    if (fh != null) {
                        return fh;
                    }
                }

                // System.err.println("Fetching tile: " + tileNum);

                SimpleFeatureHolder fh = new SimpleFeatureHolder();
                featureSets[tileNum] = tileCache.makeReference(fh);
                try {
                    fetchFeatures(this, refSeq.getName(), tileSpans[tileNum].getMin(), tileSpans[tileNum].getMax());
                } catch (Exception ex) {
                    throw new BioRuntimeException(ex);
                }
                return fh;
            }

            public Iterator features() {
                return getFeatures().features();
            }
        
            public FeatureHolder filter(FeatureFilter ff) {
                return getFeatures().filter(ff);
            }

            public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
                return getFeatures().filter(ff, recurse);
            }

            public int countFeatures() {
                return getFeatures().countFeatures();
            }

            public boolean containsFeature(Feature f) {
                return getFeatures().containsFeature(f);
            }

            public Feature createFeature(Feature.Template templ)
	            throws ChangeVetoException, BioException
            {
                Feature f = FeatureImpl.DEFAULT.realizeFeature(refSeq, refSeq, templ);
                if (LocationTools.contains(tileSpans[tileNum], f.getLocation())) {
                    getFeatures().addFeature(f);
                } else {
                    if (!spanningFeatures.containsFeature(f)) {
                        spanningFeatures.addFeature(f);
                    }
                }
                return f;
            }
	    
	        public void removeFeature(Feature f) 
                throws ChangeVetoException
            {
                throw new ChangeVetoException("NO");
            }

            public void addChangeListener(ChangeListener cl) {}
            public void addChangeListener(ChangeListener cl, ChangeType ct) {}
            public void removeChangeListener(ChangeListener cl) {}
            public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
            public boolean isUnchanging(ChangeType ct) { return true; }
        }
    }    
}

