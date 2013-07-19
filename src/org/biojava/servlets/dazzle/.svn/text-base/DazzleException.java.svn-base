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

/**
 * Exception thrown to indicate a failure in a Dazzle command handler.  Where
 * possible, these exceptions should be constructed with a valid DAS error code.
 * The <code>DASStatus</code> class provides helpful constants.  If a handler
 * throws one of these exceptions before the HTTP header is complete, the controller
 * servlet will produce a sensible DAS error page.  For this reason, handlers
 * should attempt to perform all operations likely to trigger an exception
 * before they start emitting any XML.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class DazzleException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int dasStatusCode;
    
    public int getDasStatus() {
        return dasStatusCode;
    }
    
    public DazzleException(int sc) {
        super();
        this.dasStatusCode = sc;
    }
    
    public DazzleException(int sc, Exception ex) {
        super(ex);
        this.dasStatusCode = sc;
    }
    
    public DazzleException(int sc, String msg) {
        super(msg);
        this.dasStatusCode = sc;
    }
    
    public DazzleException(int sc, Exception ex, String msg) {
        super(msg, ex);
        this.dasStatusCode = sc;
    }
    
    public DazzleException() {
        this(DASStatus.STATUS_SERVER_ERROR);
    }
    
    public DazzleException(Exception ex) {
        this(DASStatus.STATUS_SERVER_ERROR, ex);
    }
    
    public DazzleException(String message) {
        this(DASStatus.STATUS_SERVER_ERROR, message);
    }
    
    public DazzleException(Exception ex, String message) {
        this(DASStatus.STATUS_SERVER_ERROR, ex, message);
    }
}
