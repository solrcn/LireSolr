package net.semanticmetadata.lire.solr;

import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.indexing.tools.Extractor;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: mlux
 * Date: 08.07.13
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
public class AddImagesFromDataFile {
    boolean verbose = true;
    private static HashMap<Class, String> classToPrefix = new HashMap<Class, String>(5);

    static {
        classToPrefix.put(ColorLayout.class, "cl");
        classToPrefix.put(EdgeHistogram.class, "eh");
        classToPrefix.put(PHOG.class, "ph");
        classToPrefix.put(OpponentHistogram.class, "oh");
        classToPrefix.put(JCD.class, "jc");
    }
    
    public static void main(String[] args) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
//        BitSampling.setNumFunctionBundles(80);
//        BitSampling.generateHashFunctions("BitSampling.obj");
        BitSampling.readHashFunctions();
        AddImagesFromDataFile a = new AddImagesFromDataFile();
        a.createXml(new File("I:/WIPO/CA/"), new File("I:/WIPO/CA/wipo.data"));
    }


    public void createXml(File outDirectory, File inputFile) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(inputFile)));
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;
        int count = 0;
        byte[] temp = new byte[100 * 1024];
        // read file hashFunctionsFileName length:
        FileWriter out = new FileWriter(outDirectory.getPath() + "/data_001.xml", false);
        int fileCount = 1;
        out.write("<add>\n");
        while (in.read(tempInt, 0, 4) > 0) {
            tmp = SerializationUtils.toInt(tempInt);
            // read file hashFunctionsFileName:
            in.read(temp, 0, tmp);
            String filename = new String(temp, 0, tmp);
            // normalize Filename to full path.
            File file = new File(filename);
            out.write("\t<doc>\n");
            // id and file name ...
            out.write("\t\t<field name=\"id\">");
            out.write(file.getCanonicalPath().replace("I:\\WIPO\\", "").replace('\\', '/'));
            out.write("</field>\n");
            out.write("\t\t<field name=\"title\">");
            out.write(file.getName());
            out.write("</field>\n");
//            System.out.print(filename);
            while ((tmpFeature = in.read()) < 255) {
//                System.out.print(", " + tmpFeature);
                LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                // byte[] length ...
                in.read(tempInt, 0, 4);
                tmp = SerializationUtils.toInt(tempInt);
                // read feature byte[]
                in.read(temp, 0, tmp);
                f.setByteArrayRepresentation(temp, 0, tmp);
                addToDocument(f, out);
//                d.add(new StoredField(Extractor.featureFieldNames[tmpFeature], f.getByteArrayRepresentation()));
            }
            out.write("\t</doc>\n");
            count++;
            if (count % 100000 == 0) {
//                break;
                out.write("</add>\n");
                out.close();
                fileCount++;
                out = new FileWriter(outDirectory.getPath() + "/data_0"+((fileCount<10)?"0"+fileCount:fileCount)+".xml", false);
                out.write("<add>\n");
            }
            if (verbose) {
                if (count %  1000 == 0) System.out.print('.');
                if (count % 10000 == 0) System.out.println(" " + count);
            }
        }
        if (verbose) System.out.println(" " + count);
        out.write("</add>\n");
        out.close();
        in.close();
    }

    private void addToDocument(LireFeature feature, Writer out) throws IOException {
        String histogramField = classToPrefix.get(feature.getClass()) + "_hi";
        String hashesField = classToPrefix.get(feature.getClass()) + "_ha";

        out.write("\t\t<field name=\"" + histogramField + "\">");
        out.write(Base64.encodeBase64String(feature.getByteArrayRepresentation()));
        out.write("</field>\n");
        out.write("\t\t<field name=\"" + hashesField + "\">");
        out.write(SerializationUtils.arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram())));
        out.write("</field>\n");
        
    }
}