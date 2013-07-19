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

public class FastaDataSource extends AbstractDataSource implements DazzleReferenceSource {
    private String fileName;
    private Map seqs;
    
    public String getDataSourceType() {
        return "fasta";
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
            BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResourceAsStream(fileName)));
            SequenceBuilderFactory sbf = SimpleSequenceBuilder.FACTORY;
            SequenceFormat sf = new FastaFormat();
            SequenceIterator si = new StreamReader(br, sf, DNATools.getDNA().getTokenization("token"), sbf);
            while (si.hasNext()) {
                Sequence seq = si.nextSequence();
                seqs.put(seq.getName(), seq);
            }
        } catch (Exception ex) {
            throw new DataSourceException(ex, "Couldn't load sequence file");
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
        return Collections.emptySet();
    }
    
    public Set getEntryPoints() {
        return seqs.keySet();
    }
}
