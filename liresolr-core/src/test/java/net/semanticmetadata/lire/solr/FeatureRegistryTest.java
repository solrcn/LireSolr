package net.semanticmetadata.lire.solr;

import junit.framework.TestCase;

/**
 * This file is part of LIRE SOLR, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 28.11.2014
 */
public class FeatureRegistryTest extends TestCase {
    public void testToString() {
        System.out.println(new FeatureRegistry().toString());
    }

    public void testGetClass() {
        System.out.println("cl_ha = " + FeatureRegistry.getClassForHashField("cl_ha").getName());
        System.out.println("sc_ha = " + FeatureRegistry.getClassForHashField("sc_ha").getName());
        System.out.println("ph_ha = " + FeatureRegistry.getClassForHashField("ph_ha").getName());
        System.out.println("ce_ha = " + FeatureRegistry.getClassForHashField("ce_ha").getName());

        System.out.println("cl_hi = " + FeatureRegistry.getClassForFeatureField("cl_hi").getName());
        System.out.println("sc_hi = " + FeatureRegistry.getClassForFeatureField("sc_hi").getName());
        System.out.println("ph_hi = " + FeatureRegistry.getClassForFeatureField("ph_hi").getName());
        System.out.println("ce_hi = " + FeatureRegistry.getClassForFeatureField("ce_hi").getName());
    }


    public void testGetFieldname() {
        System.out.println("cl_ha => " + FeatureRegistry.getFeatureFieldName("cl_ha"));
        System.out.println("sc_ha => " + FeatureRegistry.getFeatureFieldName("sc_ha"));
        System.out.println("ph_ha => " + FeatureRegistry.getFeatureFieldName("ph_ha"));
        System.out.println("ce_ha => " + FeatureRegistry.getFeatureFieldName("ce_ha"));
    }
}
