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
import javax.servlet.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.structure.io.*;

/**
 * Simple datasource backed to provide alignment data.
 * can be configured in dazzlecfg.xml .
 *
 * @author Andreas Prlic
 * @author Thomas Down
 * @version 1.00
 */

public class StructureSource 
extends AbstractDataSource 
implements DazzleReferenceSource {

	private Set allTypes;

	public String filepath ;
	public String pdbReader ; 
	public String fileExt;

	// for pfetch command 
	public String host ;
	public int    port ;

	public String dbDriver;
	public String dbUrl;
	public String dbUsername;
	public String dbPassword;

	public void init(ServletContext ctx) 
	throws DataSourceException
	{
		super.init(ctx);

	}

	public void   setHost(String m)         { host =  m ;    }
	public String getHost()                 { return host ;  } 

	public void   setPort(int p)            { port =  p ;    }
	public int    getPort()                 { return port ;  } 

	public void   setDbDriver(String dbd)   { dbDriver = dbd;   }
	public String getDbDriver()             { return dbDriver ; }

	public void   setDbUrl(String dbu)      { dbUrl = dbu ; }
	public String getDbUrl()                { return dbUrl;}

	public void   setDbUsername(String dbu) { dbUsername = dbu; }
	public String getDbUsername()           { return dbUsername ;}

	public void   setDbPassword(String dbp) { dbPassword = dbp ;}
	public String getDbPassword()           { return dbPassword ;}

	public StructureIO getPdbIo()
	throws DataSourceException
	{
		System.out.println("StructureSource getPdbIo");
		System.out.println("pdbReader: "+pdbReader);

		StructureIO pdbIO = null;

		try {
			Class  c = Class.forName(pdbReader);
			Object structureIo = c.newInstance();

			boolean isStructureIOFile = false;

			// go through the interfaces of the reader and see if it implements isStructureIOFile
			Class[] interfacses = c.getInterfaces();
			for (Class class1 : interfacses) {
				if (class1.getName().equals("org.biojava.bio.structure.io.StructureIOFile")) {
					isStructureIOFile = true;
					break;
				}				
			}
			System.out.println( c.getName() + " is a structureIOFile: " + isStructureIOFile);

			if (pdbReader.equals("org.biojava.bio.structure.io.PDBSRSReader")) {

				PDBSRSReader pdbsrs = (PDBSRSReader)structureIo ;

				System.setProperty("PFETCH_host" , host    );
				System.setProperty("PFETCH_port" , ""+port );

				pdbIO = pdbsrs;


			}  else if ( pdbReader.equals("org.biojava.bio.structure.PDBXMLReader")){
				throw new DataSourceException("not implemented,yet "+ pdbReader);

				//pdbIO = (StructureIOXML)d ;		
				// init pdbIOXML

			} else if ( pdbReader.equals("org.biojava.bio.structure.io.PDBMSDReader")){

				// init pdbIOMSD

				PDBMSDReader s = (PDBMSDReader)structureIo ;
				s.setDBConnection(dbDriver,dbUrl,dbUsername,dbPassword);
				pdbIO = s;		

			} else if (isStructureIOFile) {
				//"org.biojava.bio.structure.io.PDBFileReader"
				System.out.println("this is a  structureIOFile");
				StructureIOFile pdbFile = (StructureIOFile)structureIo ;

				// init pdbIOFile

				pdbFile.setPath(filepath);


				// check if some specific file extensions have been requested
				String[] spl = new String[0];
				if ( fileExt != null ) {
					spl = fileExt.split(" ") ;
					for (int i=0; i<spl.length; i++){
						System.out.println("adding extensions " + spl[i] );
						pdbFile.addExtension(spl[i]);
					}
				}

				pdbIO = pdbFile;

			} else {
				// all other readers should do the config themselves...
				// e.g. uk.ac.sanger.dazzle.datasource.HibernatePDBUPAlignmentSource
				pdbIO = (StructureIO)structureIo;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new DataSourceException(ex,"Couldn't create pdbIo PDB file reader");
		}


		return pdbIO ;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String s) {
		fileExt = s ;
	}

	public String getPdbReader()         { return pdbReader;   }

	public void   setPdbReader(String s) { pdbReader = s ;     }

	public String getFilePath() {

		return filepath ;   
	}

	public void setFilePath(String s) {

		filepath = s;

	}

	public String getDataSourceType() {return "pdbfile"; }

	public String getDataSourceVersion() { return "1.00";  }


	/* not really needed ... */

	public String getMapMaster() {       return null;   }


	public String getLandmarkVersion(String ref)
	throws DataSourceException, NoSuchElementException
	{  return getVersion();   }

	public Sequence getSequence(String ref)
	throws NoSuchElementException, DataSourceException
	{       return null;  }

	public Set getAllTypes() {
		if ( allTypes == null)
			allTypes = new HashSet();
		return Collections.unmodifiableSet(allTypes);
	}

	public Set getEntryPoints() {      return new HashMap().keySet();  }

	public Set getEntryPoints(String ref) {   return new HashMap().keySet();  }
}
