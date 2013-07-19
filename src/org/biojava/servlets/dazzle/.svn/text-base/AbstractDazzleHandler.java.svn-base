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

import org.biojava.servlets.dazzle.datasource.*;

/**
 * Handler which implements one or more DAS commands.
 */

public abstract class AbstractDazzleHandler implements DazzleHandler {
    private final Class requirements;
    private final String[] _commands;
    private final String[] _caps;
    
    protected AbstractDazzleHandler(Class requirements, String[] commands, String[] caps) {
        super();
        this.requirements = requirements;
        this._commands = commands;
        this._caps = caps;
    }
    
    public boolean accept(DazzleDataSource dds) {
        return requirements.isInstance(dds);
    }
    
    public String[] capabilities(DazzleDataSource dds) {
        return _caps;
    }
    
    public String[] commands(DazzleDataSource dds) {
        return _commands;
    }
}
