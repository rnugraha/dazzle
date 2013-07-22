package org.biojava.servlets.dazzle.datasource;

import org.biojava.servlets.dazzle.Segment;

/**
 * Created with IntelliJ IDEA.
 * User: rnugraha
 * Date: 18-07-13
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class MyPlugin extends AbstractGFFFeatureSource {

    @Override
    public GFFFeature[] getFeatures(Segment seg, String[] types) throws DataSourceException {
        System.out.println("got a features request for " + seg);
        return new GFFFeature[0];
    }


    public GFFFeature[] getFeatures(String reference) throws  DataSourceException {
        System.out.println("got a features request for " + reference);
        return new GFFFeature[0];
    }

}
