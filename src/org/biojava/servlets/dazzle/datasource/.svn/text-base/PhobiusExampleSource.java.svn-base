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
 * Created on May 21, 2007
 * 
 */

package org.biojava.servlets.dazzle.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import org.biojava.servlets.dazzle.Segment;

public class PhobiusExampleSource  extends AbstractGFFFeatureSource {

	String fileName;
	String dataSourceName;
	Map data;
	String exampleId;

	
	
	public String getExampleId() {
		return exampleId;
	}



	public void setExampleId(String exampleId) {
		this.exampleId = exampleId;
	}



	public void setFileName(String s) {
		fileName = s;
	}



	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}


	private void parsePhobiusData(ServletContext ctx) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResourceAsStream(fileName)));
		List oData = new ArrayList();
		List sData = new ArrayList();
		List mData = new ArrayList();
		List iData = new ArrayList();
		List hData = new ArrayList();
		
		for(String line = br.readLine(); line != null; line = br.readLine()) {
			if ( line.charAt(0) == '#')
				continue;
			StringTokenizer token = new StringTokenizer(line,"\t");
			if (token.countTokens() < 7) {
				throw new IOException("file does not match expected format at line " + line + " it has got" +  token.countTokens() + " tokens!");
			}
			// pos aa	i	o	M	S
			String pos = token.nextToken();
			String aa  = token.nextToken();
			String i   = token.nextToken();
			String o   = token.nextToken();
			String m   = token.nextToken();
			String s   = token.nextToken();
			String h   = token.nextToken();
			
			
			GFFFeature of = new GFFFeature();
			of.setStart(pos);
			of.setEnd(pos);
			of.setMethod("Phobius");
			of.setName("Phobius O - score");
			of.setLink("http://phobius.cgb.ki.se");
			of.setScore(o);
			of.setType(of.getName());		

			GFFFeature sf = (GFFFeature) of.clone();
			sf.setName("Phobius S - score");
			sf.setScore(s);
			sf.setType(sf.getName());

			GFFFeature mf = (GFFFeature) of.clone();
			mf.setName("Phobius M - score");
			mf.setScore(m);
			mf.setType(mf.getName());
			
			GFFFeature iif = (GFFFeature) of.clone();
			iif.setName("Phobius i - score");
			iif.setScore(i);
			iif.setType(iif.getName());
			
			GFFFeature hydro = (GFFFeature) of.clone();
			hydro.setMethod("KyleDoolittle");
			hydro.setName("hydrophobicity");
			hydro.setLink("");
			hydro.setScore(h);
			hydro.setType(hydro.getName());
			
			oData.add(of);
			sData.add(sf);
			mData.add(mf);
			iData.add(iif);
			hData.add(hydro);
		}

		oData.addAll(sData);
		oData.addAll(mData);
		oData.addAll(iData);
		oData.addAll(hData);
		data.put(exampleId,oData);

	}


	public void init(ServletContext ctx) 
	throws DataSourceException
	{
		data = new HashMap();
		super.init(ctx);
		try {
			//parse the phobius result file
			parsePhobiusData(ctx);

		} catch (Exception ex) {
			throw new DataSourceException(ex, "Couldn't load data file");
		}
	}




	public GFFFeature[] getFeatures(Segment seg,String[] types) throws DataSourceException {

		// here:  ignore filtering by type...
		
		String ref = seg.toString();
		List feats = new ArrayList();

		if ( data.containsKey(ref)) {
			feats = (List) data.get(ref);
		}

		return (GFFFeature[]) feats.toArray(new GFFFeature[feats.size()]);

	}



}
