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

import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.utils.xml.*;

/**
 * Simple example datasource backed by an EMBL file.
 *
 * @author Thomas Down
 * @version 1.00
 */

public class EmblDataSource extends AbstractDataSource implements DazzleReferenceSource {
    private String fileName;
    private Map seqs;
    private Set allTypes;
    
    public String getDataSourceType() {
        return "emblfile";
    }
    
    public String getDataSourceVersion() {
        return "1.00";
    }

    public void setFileName(String s) {
        fileName = s;
    }

    public String getMapMaster() {
        return null;
    }

    public void init(ServletContext ctx) 
        throws DataSourceException
    {
        super.init(ctx);
        try {
            seqs = new HashMap();
            allTypes = new HashSet();
            BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResourceAsStream(fileName)));
            SequenceBuilderFactory sbf = new EmblProcessor.Factory(new NameCatcherFactory(SimpleSequenceBuilder.FACTORY));
            SequenceFormat sf = new EmblLikeFormat();
            SequenceIterator si = new StreamReader(br, sf, DNATools.getDNA().getTokenization("token"), sbf);
            while (si.hasNext()) {
                Sequence seq = si.nextSequence();
                seqs.put(seq.getName(), seq);
            }
        } catch (Exception ex) {
            throw new DataSourceException(ex, "Couldn't load sequence file");
        }
    }

    private class NameCatcherFactory implements SequenceBuilderFactory {
        private SequenceBuilderFactory chain;
        
        NameCatcherFactory(SequenceBuilderFactory chain) {
            this.chain = chain;
        }
        
        public SequenceBuilder makeSequenceBuilder() {
            return new NameCatcher(chain.makeSequenceBuilder());
        }
    }
    
    private class NameCatcher extends SequenceBuilderFilter {
        NameCatcher(SequenceBuilder delegate) {
            super(delegate);
        }
        
        public void setName(String name) 
            throws ParseException
        {
            // We ignore this, since biojava is currently using IDs rather than
            // accessions.  Hopefully the accession will be caught later.
        }
        
        public void addSequenceProperty(Object key, Object value)
            throws ParseException
        {
            if ("AC".equals(key)) {
                String acString = value.toString();
                int semi = acString.indexOf(';');
                if (semi > 0) {
                    acString = acString.substring(0, semi);
                }
                getDelegate().setName(acString);
            }
            getDelegate().addSequenceProperty(key, value);
        }
        
        public void startFeature(Feature.Template templ) 
            throws ParseException 
        {
            allTypes.add(templ.type);
            getDelegate().startFeature(templ);
        }
    }

    public String getLandmarkVersion(String ref)
        throws DataSourceException, NoSuchElementException
    {
        Annotation anno = getSequence(ref).getAnnotation();
        if (anno.containsProperty("SV")) {
            String sv = anno.getProperty("SV").toString();
            int dotPos = sv.indexOf('.');
            if (dotPos > 0) {
                return sv.substring(dotPos + 1);
            } else {
                return sv;
            }
        }
        
        return getVersion();
    }

    public Sequence getSequence(String ref)
        throws NoSuchElementException, DataSourceException
    {
        Sequence seq = (Sequence) seqs.get(ref);
        if (seq == null) {
            throw new NoSuchElementException("No sequence " + ref);
        }
        return seq;
    }

    public Set getAllTypes() {
        return Collections.unmodifiableSet(allTypes);
    }
    
    public Set getEntryPoints() {
        return seqs.keySet();
    }
}
