/** biojava LGPL header */

package org.biojava.servlets.dazzle.datasource.db ;


import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.sql.DataSource ;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.biojava.servlets.dazzle.datasource.GFFFeature;


/** a class that caches GFFFeatures using IBM's Cloudscape (former
 * Derby) 

 * how to use it: <br>
 * add the following line to you dazzlecfg.xml file:
 * <pre>
 *   <resource id="GFFFeatureCache" jclass="org.biojava.servlets.dazzle.datasource.DerbyFeatureCache"/>     
 *</pre>
 * a datasource can then ask to get this object by :
 * <pre>
 *   GFFFeatureCache cache =  (GFFFeatureCache) ctx.getAttribute("GFFFeatureCache");
 * </pre>
 */

public class MysqlFeatureCache 
implements GFFFeatureCache 
{

	public static String tableName   = "feature_cache" ;
	public static String DATASOURCE_NAME = "jdbc/feature_cache";
	//Connection conn ; 
	DataSource dataSource;


	static String test = "select count(*) " +
	"from " + tableName + " " + 
	"where code = ? and dassource = ?";


	static String sele = "select "+		    
	" method,"+
	" featurename,"+
	" type,"+
	" note,"+
	" link,"+
	" start,"+
	" fend,"+
	" phase,"+
	" orientation,"+
	" score,"+
	" label " +
	"from " + tableName + " " + 
	"where code = ? and "+
	" dassource = ? and"+
	" nofeaturesflag = ? "+
	"order by type,note" ;

	static String ins = "insert into " + tableName + 
	"   (code,"+
	"   dassource,"+
	"   method,"+
	"   featurename,"+
	"   type,"+
	"   note,"+
	"   link,"+
	"   start,"+
	"   fend," +
	"   phase,"+
	"   orientation,"+
	"   score,"+
	"   label,"+
	"   timestamp,"+
	"   nofeaturesflag) " +
	"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; 

	static String nofeats = " insert into " + tableName +
	" (code,"+
	" dassource,"+
	" nofeaturesflag, timestamp) " +
	"values (?,?,?,?)";



	/** init the cache */


	public MysqlFeatureCache() {
		System.out.println("MysqlFeature cache init");
		try
		{
			// open db connection

			Context env = (Context) new InitialContext().lookup("java:comp/env");

			dataSource = (DataSource) env.lookup(DATASOURCE_NAME);


		}
		catch (Exception e) {
			System.out.println("exception thrown:");	  
			e.printStackTrace();	    
		}

		System.out.println("finished init of GFFFeatureCache");
	}

	/** alternative constructor, if dataSource is provided from outside
	 * 
	 * @param dataSourceCache
	 */
	public MysqlFeatureCache(DataSource dataSourceCache) {
		dataSource = dataSourceCache;
	}

	static void printSQLError(SQLException e)
	{
		while (e != null)
		{
			System.out.println(e.toString());
			e = e.getNextException();
		}
	}





	/** see if cache contains an entry for a paritucal accession code
	 * and a das source */    

	public boolean isCached(String seqId, String dassource){
		//System.out.println("cache - isCached? " + seqId + " " + dassource);
		boolean cached = false;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();

			PreparedStatement testsql   = conn.prepareStatement(test);
			// todo test age of entry!
			testsql.setString(1,seqId);
			testsql.setString(2,dassource);
			ResultSet row = testsql.executeQuery();
			while (row.next()) {
				int size = row.getInt(1);
				if ( size > 0 ) {		   		    
					cached = true ;
					break;
				}
			}	    
			row.close();
			//testsql.clearParameters();
			testsql.close();

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if ( conn != null ) {
				try { conn.close();}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return cached ;

	}

	/** store features in cache */
	public void cacheFeatures(String seqId,String dassource, GFFFeature[] features) {
		//System.out.println("cache - cacheFeatures "+ seqId + " " + dassource);
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			if ( features.length == 0 ) {
				//System.out.println(seqId + " feature length = 0");
				// store that there are no features for this seqId
				PreparedStatement flagsql   = conn.prepareStatement(nofeats);
				flagsql.setString(1,seqId);
				flagsql.setString(2,dassource);
				flagsql.setString(3,"t") ; // true + there are no features
				int ts = getTimeStamp();
				flagsql.setInt(4,ts);
				flagsql.executeUpdate();		
				//conn.commit();
				flagsql.clearParameters();
				return;
			}

			for ( int i = 0 ; i< features.length;i++ ) {
				GFFFeature gff = features[i];
				//if (gff == null) {
				//  System.out.println(" got null instead of gff feature!");
				//}
				cacheFeature(conn,seqId,dassource,gff);		
			}
			//conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if ( conn != null ) {
				try { conn.close();}
				catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private int getTimeStamp(){
		java.util.Date now = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmm");
		String date =  formatter.format(now);
		return Integer.parseInt(date);
	}

	private void cacheFeature(Connection conn,String seqId,String dassource, GFFFeature gff)
	throws SQLException
	{
		//System.out.println("caching feature " + gff.getMethod());
		PreparedStatement insertsql = conn.prepareStatement(ins);
		insertsql.setString(1,seqId);
		insertsql.setString(2,dassource);
		insertsql.setString(3,gff.getMethod());
		insertsql.setString(4,gff.getName());
		insertsql.setString(5,gff.getType());
		insertsql.setString(6,gff.getNote());
		insertsql.setString(7,gff.getLink());
		insertsql.setString(8,gff.getStart());
		insertsql.setString(9,gff.getEnd());
		insertsql.setString(10,gff.getPhase());
		insertsql.setString(11,gff.getOrientation());
		insertsql.setString(12,gff.getScore());
		insertsql.setString(13,gff.getLabel());
		// a timestamp
		int ts = getTimeStamp();
		insertsql.setInt(14,ts);
		insertsql.setString(15,"f");
		insertsql.executeUpdate();
		//conn.commit();
		insertsql.clearParameters();


	}

	/** retrieve all Features from cache for a particular sequence 
	 */

	public GFFFeature[] retrieveFeatures(String seqId,String dassource){

		//System.out.println("cache - retreiveFeatures "+ seqId + " " + dassource);

		Connection conn = null;
		List features = new ArrayList();
		try {
			conn = dataSource.getConnection();
			PreparedStatement selectsql = conn.prepareStatement(sele);
			selectsql.setString(1,seqId);
			selectsql.setString(2,dassource);
			selectsql.setString(3,"f"); // nofeaturesflag 
			ResultSet row = selectsql.executeQuery();
			while (row.next()) {
				GFFFeature feat = getGFFFeatureFromRow(row);

				if ( feat != null ) {
					features.add(feat);
					//System.out.println(feat);
				}
			}
			row.close();
			selectsql.clearParameters();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try { 
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return (GFFFeature[])features.toArray(new GFFFeature[features.size()]) ;
	}

	/** convert data from a row to a GFFFeature */
	private GFFFeature getGFFFeatureFromRow(ResultSet row) 
	throws SQLException 
	{

		String method = row.getString(1);
		String name   = row.getString(2);
		String type   = row.getString(3);
		String note   = row.getString(4);
		String link   = row.getString(5);
		String start  = row.getString(6);
		String end    = row.getString(7);
		String phase  = row.getString(8);
		String orient = row.getString(9);
		String score  = row.getString(10);
		String label  = row.getString(11);

		GFFFeature gff = new GFFFeature();
		gff.setName(name.trim());
		gff.setMethod(method.trim());
		gff.setType(type.trim());
		gff.setStart(start.trim());
		gff.setEnd(end.trim());
		gff.setNote(note.trim());
		if ( link != null ) 
			gff.setLink(link.trim()) ;
		if (phase != null )
			gff.setPhase(phase.trim());
		if ( orient != null )
			gff.setOrientation(orient.trim());
		if ( score != null )
			gff.setScore(score.trim());
		if ( label != null )
			gff.setLabel(label.trim());
		return gff ;	

	} 

	/** clear all data in cache */
	public void clearCache() {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			Statement s = conn.createStatement();

			String deletesql = "delete from " +tableName ;
			System.out.println(deletesql);
			s.execute(deletesql);
			//conn.commit();
			s.close();


		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if ( conn != null ) {
				try { 
					conn.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
