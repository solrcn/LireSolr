package net.semanticmetadata.lire.solr;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * This file is part of LIRE, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 10.12.2014
 */
public class BenchmarkTest extends TestCase {
    String[] ids = new String[]{
            "images03/25/im246522.jpg",
            "images08/71/im707733.jpg",
            "images05/44/im433743.jpg",
            "images07/68/im678446.jpg",
            "images09/82/im818003.jpg",
            "images05/43/im427948.jpg",
            "images08/77/im762847.jpg",
            "images02/14/im134542.jpg",
            "images10/92/im910329.jpg",
            "images09/88/im873077.jpg",
            "images02/13/im120870.jpg",
            "images10/93/im923458.jpg",
            "images09/81/im804427.jpg",
            "images05/45/im449382.jpg",
            "images01/3/im23613.jpg",
            "images09/81/im805273.jpg",
            "images07/70/im696839.jpg",
            "images05/50/im490269.jpg",
            "images03/28/im275972.jpg",
            "images09/84/im838193.jpg",
            "images03/29/im285693.jpg",
            "images05/49/im481031.jpg",
            "images05/47/im467367.jpg",
            "images06/52/im513739.jpg",
            "images06/55/im541631.jpg",
            "images06/60/im594633.jpg",
            "images06/59/im580919.jpg",
            "images03/23/im225043.jpg",
            "images02/13/im121897.jpg",
            "images06/53/im525695.jpg",
            "images07/66/im658104.jpg",
            "images09/88/im877935.jpg",
            "images04/40/im391889.jpg",
            "images05/50/im495078.jpg",
            "images06/58/im578905.jpg",
            "images04/40/im393973.jpg",
            "images04/35/im348361.jpg",
            "images10/93/im929155.jpg",
            "images10/97/im967589.jpg",
            "images09/87/im863699.jpg",
            "images03/22/im215898.jpg",
            "images04/40/im391329.jpg",
            "images08/78/im773700.jpg",
            "images05/47/im469097.jpg",
            "images05/45/im448549.jpg",
            "images03/28/im279688.jpg",
            "images05/47/im465115.jpg",
            "images07/69/im686255.jpg",
            "images10/95/im942542.jpg",
            "images06/60/im590108.jpg",
            "images07/70/im691860.jpg",
            "images06/60/im590541.jpg",
            "images04/36/im350746.jpg",
            "images08/74/im739213.jpg",
            "images04/38/im371019.jpg",
            "images04/31/im306018.jpg",
            "images09/89/im889775.jpg",
            "images07/61/im607263.jpg",
            "images09/83/im828038.jpg",
            "images07/64/im637063.jpg",
            "images06/52/im513474.jpg",
            "images08/75/im744497.jpg",
            "images03/26/im258996.jpg",
            "images07/67/im664385.jpg",
            "images07/68/im677101.jpg",
            "images04/31/im307298.jpg",
            "images05/48/im472740.jpg",
            "images06/51/im503369.jpg",
            "images04/31/im306404.jpg",
            "images09/84/im839901.jpg",
            "images03/25/im243232.jpg",
            "images07/62/im611088.jpg",
            "images08/79/im789924.jpg",
            "images08/75/im745260.jpg",
            "images10/93/im926618.jpg",
            "images04/36/im359704.jpg",
            "images08/78/im779489.jpg",
            "images07/63/im620996.jpg",
            "images05/50/im493070.jpg",
            "images08/78/im778144.jpg",
            "images06/58/im571657.jpg",
            "images06/60/im599974.jpg",
            "images08/79/im783327.jpg",
            "images08/72/im716648.jpg",
            "images06/52/im511559.jpg",
            "images04/36/im357315.jpg",
            "images09/88/im872535.jpg",
            "images08/79/im786952.jpg",
            "images07/65/im642722.jpg",
            "images10/99/im984829.jpg",
            "images10/93/im928992.jpg",
            "images10/93/im921464.jpg",
            "images04/32/im319250.jpg",
            "images07/66/im653395.jpg",
            "images08/75/im748541.jpg",
            "images07/66/im655450.jpg",
            "images06/53/im529256.jpg",
            "images09/88/im870473.jpg",
            "images10/95/im949403.jpg",
            "images03/21/im207634.jpg",
            "images06/52/im514626.jpg",
            "images10/96/im958765.jpg",
            "images10/99/im989909.jpg",
            "images04/36/im351042.jpg",
            "images08/76/im756472.jpg",
            "images07/64/im636398.jpg",
            "images09/87/im865089.jpg",
            "images10/100/im992573.jpg",
            "images10/98/im973000.jpg",
            "images03/29/im283965.jpg",
            "images08/76/im757444.jpg",
            "images09/87/im866770.jpg",
            "images06/52/im515174.jpg",
            "images03/27/im269215.jpg",
            "images03/27/im261859.jpg",
            "images07/61/im607435.jpg"
    };
    String[] fields = {"cl_ha", "ce_ha", "eh_ha"};
    String[] acc = {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"};
    // lire/lireq?rows=30&id=images02/20/im193713.jpg&field=ce_ha&accuracy=0.15
    String baseUrl = "http://localhost:8983/solr/";

    public void testGetRandom() throws Exception {
        String data = getData(baseUrl + "lire/lireq?rows=100");
        System.out.println(data);
    }

    public void testConnection() throws Exception {
        System.out.println("Field\tAcc\tQTime\tSearch\tRank");
        for (int j = 0; j < acc.length; j++) {
            for (int i = 0; i < fields.length; i++) {
                double qtime = 0d, searchTime = 0d, rankTime = 0d;
                for (int k = 0; k < ids.length; k++) {
                    String data = getData(baseUrl + "lire/lireq?rows=30&id=" + ids[k] + "&field=" + fields[i] + "&accuracy=" + acc[j]);
                    qtime += getValue("QTime", data);
                    searchTime += getValue("RawDocsSearchTime", data);
                    rankTime += getValue("ReRankSearchTime", data);

                }
                System.out.printf("%s\t%s\t%4.2f\t%4.2f\t%4.2f\n", fields[i], acc[j], qtime / (double) ids.length, searchTime / (double) ids.length, rankTime / (double) ids.length);
            }
        }
//        String data = getData(baseUrl + "lire/lireq?rows=30");
//        System.out.println(data);
    }

    public int getValue(String name, String data) {
        String value = data.substring(data.indexOf(name) + name.length() + 2);
        value = value.substring(0, value.indexOf(','));
        if (value.startsWith("\"")) value = value.substring(1);
        if (value.endsWith("\"")) value = value.substring(0, value.length() - 1);
        return Integer.parseInt(value);
    }

    public String getData(String url) throws Exception {
        StringBuilder ab = new StringBuilder();
        URL oracle = new URL(url);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            ab.append(inputLine);
        in.close();
        return ab.toString();
    }
}
