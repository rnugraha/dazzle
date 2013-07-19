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


/**
 * Object for representing arbitrary groupings of DASGFF features.
 *
 * @author Thomas Down
 * @version 1.00
 * @since 0.97
 */

public class DASGFFGroup {
    private final String gid;
    private final String type;
    private final String label;
    private final Map linkMap;
    private final List<String> notes;
    
    public DASGFFGroup(String gid, String type) {
        this(gid, type, null, Collections.EMPTY_MAP, Collections.EMPTY_LIST);
    }
    
    public DASGFFGroup(String gid, String type, String label) {
        this(gid, type, label, Collections.EMPTY_MAP, Collections.EMPTY_LIST);
    }
    
    public DASGFFGroup(String gid, String type, Map linkMap) {
        this(gid, type, null, linkMap, Collections.EMPTY_LIST);
    }
    
    public DASGFFGroup(String gid, String type, String label, Map linkMap) {
        this(gid, type, label, linkMap, Collections.EMPTY_LIST);
    }
    
    public DASGFFGroup(String gid, String type, String label, Map linkMap, List<String> notes) {
        super();
        this.gid = gid;
        this.type = type;
        this.linkMap = linkMap;
        this.label = label;
        this.notes = notes;
    }
    
    public List<String> getNotes() {
        return notes;
    }
    
    public String getGID() {
        return gid;
    }
    
    public String getType() {
        return type;
    }
    
    public Map getLinkMap() {
        return linkMap;
    }
    
    public String getLabel() {
        return label;
    }
}
