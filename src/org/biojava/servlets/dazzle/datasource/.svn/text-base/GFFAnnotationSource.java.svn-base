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
import java.util.regex.*;
import java.io.*;

import javax.servlet.*;
import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

import org.biojava.bio.program.gff.*;

import java.util.regex.Pattern;

/**
 * Annotation datasource backed by a GFF file.
 *
 * @author Thomas Down
 * @version 1.00
 */

public class GFFAnnotationSource extends AbstractDataSource implements DazzleDataSource {
    private static final Pattern REGION_PATTERN;
    private static final Pattern VERSION_PATTERN;
    
    static {
        REGION_PATTERN = Pattern.compile("sequence-region ([^ ]+) ([0-9]+) ([0-9]+)");
        VERSION_PATTERN = Pattern.compile("(.+)\\.([0-9]+)");
    }
    
    private String mapMaster;
    private String fileName;

    private CacheMap featureSets = new FixedSizeMap(500);
    protected Map gffSets;
    protected Set allTypes;
    private boolean dotVersions = false;
    private Map sequenceLengths = new HashMap();
    private Map aliases = new HashMap();
    private Map versions = new HashMap();
    
    private String idProperty = "id";
    private String IDProperty = "ID";
    private String groupIdProperty = "group";

    public String getDataSourceType() {
        return "gff";
    }
    
    public String getDataSourceVersion() {
        return "1.00";
    }
    
    public void setMapMaster(String s) {
        this.mapMaster = s;
    }

    public String getMapMaster() {
        return mapMaster;
    }

    public void setFileName(String s) {
        fileName = s;
    }
    
    public String getFileName(){
    	return fileName;
    }
    
    public void setDotVersions(boolean b) {
        this.dotVersions = b;
    }
    
    public void setIdProperty(String s) {
        this.idProperty = s;
    }
    
    public void setGroupIdProperty(String s) {
        this.groupIdProperty = s;
    }
    
    protected void registerSeq(String name) {
        if (dotVersions) {
            Matcher nameMatcher = VERSION_PATTERN.matcher(name);
            if (nameMatcher.matches()) {
                String seq = nameMatcher.group(1);
                String version = nameMatcher.group(2);
                aliases.put(seq, name);
                versions.put(name, version);
            }
        }
    }
    
    private String mapName(String ref) {
        if (aliases.containsKey(ref)) {
            ref = (String) aliases.get(ref);
        }
        return ref;
    }

    private class Handler implements GFFDocumentHandler {
        public void startDocument(String locator) {
        }
        
        public void commentLine(String comment) {
            if (comment.charAt(0) == '#') {
                try {
                    Matcher regionMatcher = REGION_PATTERN.matcher(comment.substring(1));
                    if (regionMatcher.matches()) {
                        String seqName = regionMatcher.group(1);
                        int length = Integer.parseInt(regionMatcher.group(3));
                        registerSeq(seqName);
                        if (!sequenceLengths.containsKey(seqName)) {
                            sequenceLengths.put(seqName, new Integer(length));
                        }
                    }
                } catch (Exception ex) {
                    log("Error parsing comment", ex);
                }
            }
        }
        
        
        
        public void endDocument() {    
        }

		public void recordLine(GFFRecord record) {
			addRecordLine(record);
		}
    }
    
    public void addRecordLine(GFFRecord record) {
        allTypes.add(record.getFeature());
        String seq = record.getSeqName();
        registerSeq(seq);
        List seqGFF = (List) gffSets.get(seq);
        if (seqGFF == null) {
            seqGFF = new ArrayList();
            gffSets.put(seq, seqGFF);
        }
        
        seqGFF.add(record);
    }
    protected void initContainers(){
        //gffSets = new HashMap();
        //allTypes = new HashSet();
        gffSets = new HashMap<String, List<GFFRecord>>();
		allTypes = new HashSet<String>();
    }
    
    public void init(ServletContext ctx) 
        throws DataSourceException
    {
        super.init(ctx);
        try {
        	initContainers();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResourceAsStream(fileName)));
            GFFEntrySet gffe = new GFFEntrySet();
            final GFFDocumentHandler adder = gffe.getAddHandler();
            
            GFFDocumentHandler handler = new Handler();
            GFFParser gffp = new GFFParser();
            gffp.parse(br, handler, fileName);
        } catch (Exception ex) {
            throw new DataSourceException(ex, "Couldn't load GFF");
        }
    }
    
    public Sequence getSequence(String ref)
	    throws DataSourceException, NoSuchElementException
    {
        ref = mapName(ref);
        int length = Integer.MAX_VALUE;
        
        if (sequenceLengths.containsKey(ref)) {
            length = ((Integer) sequenceLengths.get(ref)).intValue();
        }
        return new SimpleSequence(new DummySymbolList(DNATools.getDNA(), length), ref, ref, Annotation.EMPTY_ANNOTATION);
    }


    public String getLandmarkVersion(String ref) 
	    throws DataSourceException, NoSuchElementException
    {
        ref = mapName(ref);
        if (versions.containsKey(ref)) {
            return (String) versions.get(ref);
        } else {
            return getVersion();
        }
    }

    protected void annotate(Sequence seq)
        throws BioException, ChangeVetoException
    {
        List gff = (List) gffSets.get(seq.getName());
        if (gff == null) {
            return;
        }
        
        for (Iterator i = gff.iterator(); i.hasNext(); ) {
            GFFRecord record = (GFFRecord) i.next();
            
            StrandedFeature.Template templ = new StrandedFeature.Template();
            templ.strand = record.getStrand();
            templ.location = new RangeLocation(record.getStart(), record.getEnd());
            if ( (record.getStart() == 0) || ( record.getEnd() == 0))
            {
            	throw new BioException("Can not create ranges starting at 0, first" +
            			" position starts counting at 1 ( at:"+
            			seq.getName() + " " + templ.location + ")");
            }

            templ.type = record.getFeature();
            templ.annotation = new SmallAnnotation();
            
            Map group = record.getGroupAttributes();
            for (Iterator gi = group.entrySet().iterator(); gi.hasNext(); ) {
                Map.Entry me = (Map.Entry) gi.next();
                templ.annotation.setProperty(me.getKey(), me.getValue());
            }
            
            if (record.getScore() != GFFRecord.NO_SCORE) {
                templ.annotation.setProperty(GFFEntrySet.PROPERTY_GFF_SCORE, new Double(record.getScore()));
            }
            
            seq.createFeature(templ);
        }
    }

    public FeatureHolder getFeatures(String ref)
	    throws NoSuchElementException, DataSourceException
    {
        ref = mapName(ref);
     
        System.out.println("getFeatures for " + ref);
        //System.out.println(gffSets.containsKey(ref));
        if (!gffSets.containsKey(ref)) {
            return null;
        }
        FeatureHolder features = (FeatureHolder) featureSets.get(ref);
        if (features == null) {
            try {
                Sequence seq = getSequence(ref);
                ViewSequence vseq = new ViewSequence(seq);
                annotate(vseq);
                
                features = vseq.getAddedFeatures();
                featureSets.put(ref, features);
            } catch (BioException ex) {
                throw new DataSourceException(ex, "Error annotating sequence " + ref);
            } catch (ChangeVetoException ex) {
                throw new DataSourceException("ViewSequence isn't accepting features :(");
            }	    
        } 
        return features;
    }

    public String getFeatureID(Feature f) {
        Annotation anno = f.getAnnotation();
        if (anno.containsProperty(idProperty)) {
            return stringifyProp(anno.getProperty(idProperty));
        }
        if ( anno.containsProperty(IDProperty)) {
        	return stringifyProp(anno.getProperty(IDProperty));
        }
        
        return null;
    }
    
    public Set getAllTypes() {
        return Collections.unmodifiableSet(allTypes);
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

    public String getScore(Feature f) {
        Annotation ann = f.getAnnotation();
        if (ann.containsProperty(GFFEntrySet.PROPERTY_GFF_SCORE)) {
            return ann.getProperty(GFFEntrySet.PROPERTY_GFF_SCORE).toString();
        } else {
            return null;
        }
    }
    
    public List getGroups(Feature f) {
        Annotation anno = f.getAnnotation();
        if (anno.containsProperty(groupIdProperty)) {
            String gid = stringifyProp(anno.getProperty(groupIdProperty));
            return Collections.singletonList(
                    new DASGFFGroup(gid, f.getType())
            );
        }
                    
        return Collections.EMPTY_LIST;
    }
    
    private String stringifyProp(Object o) {
        if (o instanceof List) {
            List l = (List) o;
            if (l.size() == 1) {
                return l.get(0).toString();
            }
        }
        return o.toString();
    }
}
