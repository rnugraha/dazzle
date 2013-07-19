package org.biojava.servlets.dazzle.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.bio.program.ssbind.AnnotationFactory;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.jama.Matrix;

/** a class that helps in creating Alignment objects
 * 
 * @author Andreas Prlic 
 *
 */
public class AlignmentTools {
	
	/** add a new object to an alignment
	 * 
	 * @param ali the alignment to which the new object should be attached to
	 * @param accessionCode
	 * @param intObjectId
	 * @param objectVersion
	 * @param type
	 * @param dbSource
	 * @param dbVersion
	 * @param dbCoordSys
	 * @param details a list of detail annotation as created with getObjectDetails. Can be null or size 0.
	 * @throws DASException
	 */
	public static void addObject(Alignment ali,
						String accessionCode, 
						String intObjectId, 
						String objectVersion, 
						String type, 
						String dbSource, 
						String dbVersion, 
						String dbCoordSys,
						List details)
	throws DASException{
		
		HashMap info = new HashMap();
		info.put("dbAccessionId",accessionCode);
		info.put("intObjectId",intObjectId);
		info.put("objectVersion",objectVersion);
		info.put("type", type);
		info.put("dbSource", dbSource);
		info.put("dbVersion", dbVersion);
		info.put("dbCoordSys", dbCoordSys);
		
		if ( details != null) {
			if ( details.size() > 0 ){
				info.put("details",details);
			}
		}
		
		Annotation anno = AnnotationFactory.makeAnnotation(info);
		
		ali.addObject(anno);
		
	}
	
	/** returns an annotation representing an object detail -
	 * as can be attached to the addObject calls
	 * @param property
	 * @param dbSource
	 * @param detail
	 * @return
	 */
	public static Annotation getObjectDetails(String property, String dbSource, String detail){
		
		Map m = new HashMap();
		m.put("property", property);
		m.put("dbSource",dbSource);
		m.put("detail",detail);
		Annotation anno = AnnotationFactory.makeAnnotation(m);
		return anno;
		
	}
	
	
	/** add a shift vector to an alignment
	 * 
	 * @param ali
	 * @param intObjectId
	 * @param atom
	 * @throws DASException
	 */
	 public static void addVector(Alignment ali, String intObjectId, Atom atom) 
	 throws DASException {
	        Map anno = new HashMap();
	        anno.put("intObjectId",intObjectId);	        
	        anno.put("vector",atom);
	        Annotation a = AnnotationFactory.makeAnnotation(anno);
	        ali.addVector(a);
	    }

	
	/** add a Matrix to an object
	 * 
	 * @param ali
	 * @param ac
	 * @param matrix
	 * @throws DASException
	 */
	public static void addMatrix(Alignment ali, String intObjectId, Matrix matrix)
	throws DASException{
        Map anno = new HashMap();
        anno.put("intObjectId",intObjectId);

        for (int x=0;x<3;x++){
            for (int y=0;y<3;y++){
                String key = "mat"+(x+1)+(y+1);
                anno.put(key,matrix.get(x,y)+"");
            }
        }
        Annotation a = AnnotationFactory.makeAnnotation(anno);
        ali.addMatrix(a);
    }

	
}
