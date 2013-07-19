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

package org.biojava.servlets.dazzle;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.xml.*;
import org.biojava.servlets.dazzle.datasource.*;

/**
 * Handler which implements the DAS sequence and dna commands.
 */

public class SequenceHandler extends AbstractDazzleHandler {
    public SequenceHandler() {
        super(
            DazzleReferenceSource.class,
            new String[] {"sequence", "dna"},
            new String[] {"sequence/1.0", "dna/1.0"}
        );
    }
    
    public void run(
        DazzleServlet dazzle,
        DazzleDataSource dds,
        String cmd,
        HttpServletRequest req,
        DazzleResponse resp
    )
        throws IOException, DataSourceException, ServletException, DazzleException
    {
        if("dna".equals(cmd)) {
            dnaCommand(req, resp, dds);
        } else {
            sequenceCommand(req, resp, dds);
        }
    }
    
    private void dnaCommand(
        HttpServletRequest req,
        DazzleResponse resp,
        DazzleDataSource dds
    )
	    throws IOException, DataSourceException, ServletException, DazzleException
    {

        DazzleReferenceSource drs = (DazzleReferenceSource) dds;
        
        List segments = DazzleTools.getSegments(dds, req, resp);
        if (segments.size() == 0) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "No segments specified for dna command");
        }
        
        // Fetch and validate the requests.
        
        Map segmentResults = new HashMap();
        for (Iterator i = segments.iterator(); i.hasNext(); ) {
            Segment seg = (Segment) i.next();
            
            try {
                Sequence seq = drs.getSequence(seg.getReference());
                if (seq.getAlphabet() != DNATools.getDNA()) {
                    throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, "Sequence " + seg.toString() + " is not in the DNA alphabet");
                }
                if (seg.isBounded()) {
                    if (seg.getMin() < 1 || seg.getMax() > seq.length()) {
                        throw new DazzleException(DASStatus.STATUS_BAD_COORDS, "Segment " + seg.toString() + " doesn't fit sequence of length " + seq.length());
                    }
                }
                segmentResults.put(seg, seq);
            } catch (NoSuchElementException ex) {
                throw new DazzleException(DASStatus.STATUS_BAD_REFERENCE, ex);
            } catch (DataSourceException ex) {
                throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
            }
        }
        
        //
        // Looks okay -- generate the response document
        //
        
        
        XMLWriter xw = resp.startDasXML("DASDNA", "dasdna.dtd");
        
        try {
            xw.openTag("DASDNA");
            for (Iterator i = segmentResults.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry me = (Map.Entry) i.next();
                Segment seg = (Segment) me.getKey();
                Sequence seq = (Sequence) me.getValue();
                
                xw.openTag("SEQUENCE");
                xw.attribute("id", seg.getReference());
                xw.attribute("version", drs.getLandmarkVersion(seg.getReference()));
                if (seg.isBounded()) {
                    xw.attribute("start", "" + seg.getStart());
                    xw.attribute("stop", "" + seg.getStop());
                } else {
                    xw.attribute("start", "" + 1);
                    xw.attribute("stop", "" + seq.length());
                }
                
                SymbolList syms = seq;
                if (seg.isBounded()) {
                    syms = syms.subList(seg.getMin(), seg.getMax());
                }
                if (seg.isInverted()) {
                    syms = DNATools.reverseComplement(syms);
                }
                
                xw.openTag("DNA");
                xw.attribute("length", "" + syms.length());
                
                for (int pos = 1; pos <= syms.length(); pos += 60) {
                    int maxPos = Math.min(syms.length(), pos + 59);
                    xw.println(syms.subStr(pos, maxPos));
                }
                
                xw.closeTag("DNA");
                xw.closeTag("SEQUENCE");
            }
            xw.closeTag("DASDNA");
            xw.close();
        } catch (Exception ex) {
            throw new DazzleException(ex, "Error writing DNA document");
        }
    }

    //
    // Sequence command
    //

    private void sequenceCommand(HttpServletRequest req,
			                     DazzleResponse resp,
                                 DazzleDataSource dds)
	    throws IOException, ServletException, DataSourceException, DazzleException
    {
        DazzleReferenceSource drs = (DazzleReferenceSource) dds;
        
        List segments = DazzleTools.getSegments(dds, req, resp);
        if (segments.size() == 0) {
            throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS, "No segments specified for sequence command");
        }
        
        // Fetch and validate the requests.
        
        Map segmentResults = new HashMap();
        for (Iterator i = segments.iterator(); i.hasNext(); ) {
            Segment seg = (Segment) i.next();
            
            try {
                Sequence seq = drs.getSequence(seg.getReference());
                if (seg.isBounded()) {
                    if (seg.getMin() < 1 || seg.getMax() > seq.length()) {
                        throw new DazzleException(DASStatus.STATUS_BAD_COORDS, "Segment " + seg.toString() + " doesn't fit sequence of length " + seq.length());
                    }
                }
                segmentResults.put(seg, seq);
            } catch (NoSuchElementException ex) {
                throw new DazzleException(DASStatus.STATUS_BAD_REFERENCE, ex);
            } catch (DataSourceException ex) {
                throw new DazzleException(DASStatus.STATUS_SERVER_ERROR, ex);
            }
        }
        
        //
        // Looks okay -- generate the response document
        //
        
        XMLWriter xw = resp.startDasXML("DASSEQUENCE", "dassequence.dtd");
        
        try {
            xw.openTag("DASSEQUENCE");
            for (Iterator i = segmentResults.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry me = (Map.Entry) i.next();
                Segment seg = (Segment) me.getKey();
                Sequence seq = (Sequence) me.getValue();
                
                xw.openTag("SEQUENCE");
                xw.attribute("id", seg.getReference());
                xw.attribute("version", drs.getLandmarkVersion(seg.getReference()));
                if (seg.isBounded()) {
                    xw.attribute("start", "" + seg.getStart());
                    xw.attribute("stop", "" + seg.getStop());
                } else {
                    xw.attribute("start", "" + 1);
                    xw.attribute("stop", "" + seq.length());
                }
                String molType = seq.getAlphabet().getName();
                if (seq.getAlphabet() == DNATools.getDNA()) {
                    molType = "DNA";
                } else if (seq.getAlphabet() == RNATools.getRNA()) {
                    molType = "ssRNA";
                } else if (seq.getAlphabet() == ProteinTools.getAlphabet() || seq.getAlphabet() == ProteinTools.getTAlphabet()) {
                    molType = "Protein";
                }
                xw.attribute("moltype", molType);
                
                SymbolList syms = seq;
                if (seg.isBounded()) {
                    syms = syms.subList(seg.getMin(), seg.getMax());
                }
                if (seg.isInverted()) {
                    syms = DNATools.reverseComplement(syms);
                }
                
                for (int pos = 1; pos <= syms.length(); pos += 60) {
                    int maxPos = Math.min(syms.length(), pos + 59);
                    xw.println(syms.subStr(pos, maxPos));
                }
                xw.closeTag("SEQUENCE");
            }
            xw.closeTag("DASSEQUENCE");
            xw.close();
        } catch (Exception ex) {
            throw new DazzleException(ex, "Error writing DNA document");
        }
    }
}
