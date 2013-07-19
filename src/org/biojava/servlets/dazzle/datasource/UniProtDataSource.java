/*
 *                  BioJava development code
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
 * Created on Apr 23, 2007
 * 
 */

package org.biojava.servlets.dazzle.datasource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.ServletContext;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.Location;


/** a class that servs data from a UniProt flat file
 * 
 * @author Andreas Prlic
 *
 */
public class UniProtDataSource extends AbstractDataSource implements DazzleReferenceSource {

	private Map seqs;
	private Set allTypes;
	String fileName;

	
	public void init(ServletContext ctx) 
	throws DataSourceException
	{
		super.init(ctx);
		try {
			seqs = new HashMap();
			allTypes = new HashSet();
			BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResourceAsStream(fileName)));

//			read the SwissProt File
			SequenceIterator sequences = SeqIOTools.readSwissprot(br);


			//iterate through the sequences
			while(sequences.hasNext()){

				Sequence seq = sequences.nextSequence();
			
				seqs.put(seq.getName(), seq);
			}
		} catch (Exception ex) {
			throw new DataSourceException(ex, "Couldn't load sequence file");
		}
	}

	/** try to parse a score out of the feature notes
	 * 
	 */
	public String getScore(Feature f) {
		String score = "-";

		Annotation a = f.getAnnotation();
		try {
			
			String note = (String) a.getProperty("swissprot.featureattribute");
			
			int scorePos =note.indexOf("Score: "); 
			if (  scorePos > 0 ) {

				String sc = note.substring(scorePos+7,note.length());
				//System.out.println("parsed " + sc);
				try {
					double scp  = Double.parseDouble(sc);
					score = "" + scp;
				} catch (Exception e){
					e.printStackTrace();
				}
				try {
					int scp = Integer.parseInt(sc);
					score = "" + scp;
				} catch (Exception e){ 
					e.printStackTrace();
				}
				
			}
			
			//score = ""+ (Double)a.getProperty(SCORE);
			System.out.println("found score " + score);
		} catch (NoSuchElementException e){
			// igonre in this case...
		}
		return score;

	}


	public String getDataSourceType() {

		return "UniProtFile";
	}

	public String getDataSourceVersion() {

		return "1.00";
	}

	public void setFileName(String s) {
		fileName = s;
	}

	public Sequence getSequence(String ref) throws DataSourceException, NoSuchElementException {
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



	public String getMapMaster() {
		// TODO Auto-generated method stub
		return null;
	}



	public String getLandmarkVersion(String ref) throws DataSourceException, NoSuchElementException {
		// TODO Auto-generated method stub
		return null;
	}

}
