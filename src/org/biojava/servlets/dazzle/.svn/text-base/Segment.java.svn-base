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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Memento for a segment in a DAS request.
 *
 * @author Thomas Down
 * @since 0.90
 */

public class Segment {
    private String ref;
    private int start;
    private int stop;
    
    public Segment(String ref) {
        this.ref = ref;
        this.start = Integer.MIN_VALUE;
        this.stop = Integer.MAX_VALUE;
    }
    
    public Segment(String ref, int start, int stop) {
        this.ref = ref;
        this.start = start;
        this.stop = stop;
    }
    
    public boolean isBounded() {
        return (start != Integer.MIN_VALUE);
    }
    
    public boolean isInverted() {
        return (start > stop);
    }
    
    public String getReference() {
        return ref;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getStop() {
        return stop;
    }
    
    public int getMin() {
        return Math.min(start, stop);
    }
    
    public int getMax() {
        return Math.max(start, stop);
    }
    
    public static Segment fromString(String seg)
    		throws IllegalArgumentException
    {
        try {
	        StringTokenizer toke = new StringTokenizer(seg, ":,");
	        String newRef = toke.nextToken();
	        if (toke.hasMoreTokens()) {
	            String starts = toke.nextToken();
	            String stops = toke.nextToken();
	            return new Segment(newRef, Integer.parseInt(starts), Integer.parseInt(stops));
	        } else {
	            return new Segment(newRef);
	        }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid coordinate: " + ex.getMessage());
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException("Not a valid segment: " + seg);
        }
    }
    
    public String toString() {
        if (isBounded()) {
            return ref + ':' + start + ',' + stop;
        } else {
            return ref;
        }
    }
}
