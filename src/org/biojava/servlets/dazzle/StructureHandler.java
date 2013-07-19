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

import org.biojava.utils.xml.*;
import org.biojava.servlets.dazzle.datasource.*;
import org.biojava.bio.structure.io.StructureIO;
import org.biojava.bio.structure.io.FileConvert;
import org.biojava.bio.structure.*;


/**
 * Handler which implements the DAS STRUCTURE command.
 *
 *
 * structures can be stored as
 *  - PDB files
 *  - XML files
 *  - in a local database 
 *  - etc.
 * we need an interface to this io
 * configure in dazzlecfg.xml WHICH one to use...
 *
 * @author Andreas Prlic
 */

public class StructureHandler extends AbstractDazzleHandler {

	public StructureHandler() {
		super(
				DazzleReferenceSource.class,
				new String[] {"structure"},
				new String[] {"structure/1.0"}
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

		StructureSource stru_dds = (StructureSource)dds;

		// get Structure data from StructureInterface.
		// which pdb reader to use can be defined in dazzlecfg.xml

		// pdbIo is initiated in StructureSource, which knows where to get data from
		// here we just care to get the structure ...
		//System.out.println("get pdbio"+getTimeStamp());
		StructureIO pdbio = stru_dds.getPdbIo() ;

		try {
			
			String queries[] = req.getParameterValues("query") ;
			String modelnr   = req.getParameter("model");
			String chains[]  = req.getParameterValues("chain");
			String ranges[]  = req.getParameterValues("range");

			boolean responseStarted = false;
			XMLWriter xw = null;
			
			if (queries == null){
				throw new DazzleException(DASStatus.STATUS_BAD_COMMAND_ARGUMENTS,"did not provide the mandatory argument >query<");
			}
			
			for (int i = 0; i<queries.length;i++) {
				String query=queries[i];

				Structure pdb = null ;
				boolean problem = false;

				try {
					pdb = pdbio.getStructureById(query);

				} catch ( Exception e) {
					// could not get structure
					throw new DazzleException(DASStatus.STATUS_SERVER_ERROR,e.getMessage());
				}

				if ( pdb == null ) {
					throw new DazzleException(DASStatus.STATUS_BAD_REFERENCE,"no PDB file found for query " + query);
				}

				if ( problem ){
					throw new DazzleException(DASStatus.STATUS_SERVER_ERROR);

				}
							
				// if requested, return just one model...
				if ( modelnr != null) 
					pdb = getStructureForModel(pdb,modelnr);                   


				if (( chains != null) && (chains.length >=1 ) )                
					pdb = getStructureForChains(pdb, chains);


				if ( (ranges != null) && ( ranges.length >= 1))                 
					pdb =  getStructureForRanges(pdb,ranges);

				if (pdb == null) {   
					throw new DazzleException(DASStatus.STATUS_SERVER_ERROR);
					
				}


				// check for existing PDB code, if none exists, set to query...
				String idCode = pdb.getPDBCode();
				if ( idCode == null)
					pdb.setPDBCode(query);

				
				if ( ! responseStarted ){					
//					 do not show the stylesheet currently
					xw = resp.startDasXML(false) ;

					String strudtd = "http://www.efamily.org.uk/xml/das/2004/06/17/dasstructure.xsd" ;

					xw.openTag("dasstructure");

					xw.attribute("xmlns",strudtd);
					xw.attribute("xmlns:data","http://www.efamily.org.uk/xml/data/2004/06/17/dataTypes.xsd");
					xw.attribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
					xw.attribute("xsi:schemaLocation",strudtd + " " + strudtd);

					responseStarted = true;
				}
				
				

				FileConvert conv = new FileConvert(pdb);
				try {
					conv.toDASStructure(xw);
				} catch (Exception e) {

					e.printStackTrace();
					problem = true;

				}                
			}


			xw.closeTag("dasstructure");
			xw.close();

			pdbio = null;

		}
		catch (IOException e) {
			e.printStackTrace();
			throw new DazzleException("Error creating Structure document");
			

		}               
	}    

	private Structure getStructureForModel(Structure pdb, String modelnr){

		Structure npdb = new StructureImpl();

		try {
			int m = Integer.parseInt(modelnr) ;
			//System.out.println("requested model nr " + m);
			if ( m == 0 ) { m=1;}
			List model = pdb.getModel(m-1);
			Map h = pdb.getHeader();

			npdb.setHeader(h);
			npdb.addModel(model);
			npdb.setNmr(pdb.isNmr());
			npdb.setPDBCode(pdb.getPDBCode());
			npdb.setName(pdb.getName());
			npdb.setConnections(pdb.getConnections());


		} catch (Exception e) {
			// most likely user requested non existing model,
			// return the whole structure
			e.printStackTrace();

			return pdb;
		}

		return npdb;

	}

	private Structure getStructureForChains(Structure pdb, String[] chains){

		Structure npdb = new StructureImpl();
		npdb.setHeader(pdb.getHeader());
		npdb.setPDBCode(pdb.getPDBCode());
		npdb.setName(pdb.getName());

		if ( chains == null)
			return pdb;

		for (int c=0; c < chains.length;c++){
			try {
				Chain ch= pdb.getChainByPDB(chains[c]);
				npdb.addChain(ch);
			} catch (StructureException e){
				// the user requested a non -existing chain. ignore
			}
		}

		return npdb;
	}


	private Structure getStructureForRanges(Structure complete, String[] ranges){

		Structure npdb = new StructureImpl();
		npdb.setHeader(complete.getHeader());
		npdb.setPDBCode(complete.getPDBCode());
		npdb.setName(complete.getName());

		List rangesLst = new ArrayList(); 
		for ( int r=0;r<ranges.length;r++){
			// split the ranges up into bits and get the correpsonding coords
			String range = ranges[r];
			//System.out.println("range" + range);
			String[] spl = range.split(":");
			if (spl.length != 2)
				continue;
			RangeRequest rstart = new RangeRequest(spl[0]);
			RangeRequest rend   = new RangeRequest(spl[1]);
			Map m = new HashMap();
			m.put("start",rstart);
			m.put("end",rend);
			rangesLst.add(m);

		}


		boolean start = false;
		boolean first = true;
		boolean foundSomething = false;
		String oldchain = "";
		GroupIterator iter = new GroupIterator(complete);
		Chain newChain = new ChainImpl();

		// go over all groups and see if they are in one of the ranges ...
		// if yes add them to the new structure ...
		while(iter.hasNext()){
			Group g = (Group)iter.next();
			Chain curr = iter.getCurrentChain();
			String chainId = curr.getName();
			//System.out.println(start + " " + chainId + " " +  g);

			if ( ! start) {
				Iterator riter = rangesLst.iterator();
				while (riter.hasNext()){
					Map m = (Map)riter.next();
					RangeRequest rstart = (RangeRequest) m.get("start");

					String pdbStart = rstart.getPosition()+rstart.getInsertionCode();

					String startId  = rstart.getChainId();                    
					//System.out.println(start + " " + startId + " " + chainId + " " + pdbStart + " " + g.getPDBCode());
					if ( chainId.equals(startId) && g.getPDBCode().equals(pdbStart)){
						start = true;
						foundSomething = true;
						break;
					}
				}
			}

			if ( start) {
				if (! (chainId.equals(oldchain)) ){
					if (! first)
						npdb.addChain(newChain);
					// we found a new chain.
					newChain = new ChainImpl();
					newChain.setName(chainId);
				}
				first =false;
				newChain.addGroup(g);
			}

			if ( start) {
				Iterator riter = rangesLst.iterator();
				while (riter.hasNext()){
					Map m = (Map) riter.next();
					//RangeRequest rstart = (RangeRequest) m.get("start");
					RangeRequest rend   = (RangeRequest) m.get("end");

					String pdbEnd   = rend.getPosition()+rend.getInsertionCode();           
					String endId    = rend.getChainId();

					//System.out.println(start + " >" + endId + "< >" + chainId + "< " + pdbEnd + " " + g.getPDBCode() + " " + rend.getInsertionCode());
					if ( chainId.equals(endId) && g.getPDBCode().equals(pdbEnd)){ 
						start = false;
						newChain.setName(endId);
						break;
					}
				}
			}

			oldchain = chainId ;

		}
		
		// the current chain has not been added, yet ...
		if ( foundSomething)
			npdb.addChain(newChain);

		return npdb;
	}



}

class RangeRequest  {
	int position;
	String insertionCode;
	String chainId;


	public RangeRequest(String rangeStr){
		super();
		chainId = " ";
		insertionCode = "";
		position = -1;

		int icPos = rangeStr.indexOf("_");
		int chainPos = rangeStr.indexOf(".");
		//System.out.println(chainPos);
		try {
			position = Integer.parseInt(rangeStr);
		} catch (Exception e) {}

		if ( chainPos > -1) {
			if (chainPos+2 > rangeStr.length()) {
				// user left chainId empty ...
				chainId = " ";
			}
			else
				chainId  = rangeStr.substring(chainPos+1,chainPos+2);

			try {
				//System.out.println(rangeStr.substring(0,chainPos));
				position = Integer.parseInt(rangeStr.substring(0,chainPos));
			} catch (Exception e) { // there can be still an insertion code...
			}
		}

		if (icPos > -1) {
			insertionCode = rangeStr.substring(icPos+1,icPos+2);
			position = Integer.parseInt(rangeStr.substring(0,icPos));
		}

		//System.out.println(toString());

	}

	public String toString() {
		String txt = position + " >"+insertionCode+"< >" + chainId +"<";
		return txt;
	}
	public int getPosition(){
		return position;
	}
	public String getInsertionCode(){
		return insertionCode;
	}
	public String getChainId(){
		return chainId;
	}

}



