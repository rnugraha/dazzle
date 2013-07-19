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
 * Created on 29.10.2004
 */

package org.biojava.servlets.dazzle.datasource;



/** a very simple feature container 
 @author Andreas Prlic
 */
public class GFFFeature 
implements java.lang.Cloneable {
	String name   ;
	String method ;
	String type   ;
	String note   ;
	String link   ;
	String source ;
	String start  ;
	String end    ;
	String phase  ;
	String orientation ;
	String score       ;
	String label       ;
	String typeCategory; // support for Ontology
	String typeId; // support for Ontology
	DASGFFGroup group;

	public GFFFeature() {
		source = "Unknown";
		method = "Unknown";
		type   = "Unknown";
		note   = "";
		link   = "";
		start  = "" ;
		end    = "" ;
		phase       = null ;
		orientation = null ;
		score       = null ;
		label       = null ;
		group 		= null;
		typeCategory = null;
	}

	public Object clone() {
		GFFFeature n = new GFFFeature();
		n.setName(this.name);
		n.setSource(this.source);
		n.setMethod(this.method);
		n.setType(this.type);
		n.setNote(this.note);
		n.setLink(this.link);
		n.setStart(this.start);
		n.setEnd(this.end);
		n.setPhase(this.phase);
		n.setOrientation(this.orientation);
		n.setScore(this.score);
		n.setLabel(this.label);
		n.setGroup(this.group);
		n.setTypeCategory(this.typeCategory);
		return n;
	}

	public String toString() {
		String str = "GFFFeature: method: " + method +" type: " + type + " note: "+note ;
		str += " link: "+ link + " start:" + start + " end:" + end;

		return str ;
	}

	public void setSource(String s) { source = s;}
	public String getSource() { return source; };

	public void setName(String nam) { name = nam; }
	public String getName() { return name; }

	public void setMethod(String methd) { method = methd ; }
	public String getMethod() { return method ; }

	public void setType(String typ) { type = typ ; }
	public String getType() { return type ; }

	public void setNote(String nte) { note = nte; }
	public String getNote() { return note ; }

	public void setLink(String lnk) { link = lnk;}
	public String getLink() { return link;}

	public void setStart(String s) { start = s;}
	public String getStart() { return start;}

	public void setEnd(String e) { end = e ;}
	public String getEnd() { return end;}


	public void setPhase(String p) { phase = p;}
	public String getPhase() { return phase ;}

	public void setOrientation(String o) { orientation = o;}
	public String getOrientation() { return orientation ; }

	public void setScore(String s) { score = s;}
	public String getScore() { return score ;}

	public void setLabel(String l) { label = l;}
	public String getLabel() { return label ;}

	public DASGFFGroup getGroup() {return group;	}
	public void setGroup(DASGFFGroup group) {this.group = group;	}

    public String getTypeCategory() { return typeCategory; }
    public void setTypeCategory(String typeCategory) {  this.typeCategory = typeCategory; }

    public String getTypeId(){ return typeId; }
    public void setTypeId(String typeId) { this.typeId = typeId; }


}

