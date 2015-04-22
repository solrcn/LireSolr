package net.semanticmetadata.lire.solr;

import com.google.common.collect.Lists;
import com.jhlabs.image.DespeckleFilter;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.solr.indexing.ImageDataProcessor;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

/**
 * Created by longkeyy on 15-4-22.
 */
public class ImageToDocument {

    static List<LireFeature> features = Lists.newArrayList();

    private static void addFeatures(List features) {
        // original features
        features.add(new PHOG());
        features.add(new ColorLayout());
        features.add(new EdgeHistogram());
        features.add(new JCD());

        // new features
        features.add(new CEDD());
        features.add(new ScalableColor());
    }

    public static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder(array.length * 8);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(Integer.toHexString(array[i]));
        }
        return sb.toString();
    }
    private static int maxSideLength = 512;

    public static void main(String[] args) throws Exception {
        BitSampling.readHashFunctions();
        addFeatures(features);

        String fileUrl = "http://www.solr.cc/weixin/weixin.jpg";
        BufferedImage read = ImageIO.read(new URL(fileUrl.replaceAll(" ", "%20")).openStream());
        BufferedImage img = ImageUtils.createWorkingCopy(read);
        DespeckleFilter df = new DespeckleFilter();
        img = df.filter(img, null);
        img = ImageUtils.trimWhiteSpace(img);

        if (maxSideLength > 50)
            img = ImageUtils.scaleImage(img, maxSideLength); // scales image to 512 max sidelength.

        else if (img.getWidth() < 32 || img.getHeight() < 32) { // image is too small to be worked with, for now I just do an upscale.
            double scaleFactor = 128d;
            if (img.getWidth() > img.getHeight()) {
                scaleFactor = (128d / (double) img.getWidth());
            } else {
                scaleFactor = (128d / (double) img.getHeight());
            }
            img = ImageUtils.scaleImage(img, ((int) (scaleFactor * img.getWidth())), (int) (scaleFactor * img.getHeight()));
        }

        StringBuilder sb = new StringBuilder();
        for (LireFeature feature : features) {
            String featureCode = FeatureRegistry.getCodeForClass(feature.getClass());
            if (featureCode != null) {
                feature.extract(img);
                String histogramField = FeatureRegistry.codeToFeatureField(featureCode);
                String hashesField = FeatureRegistry.codeToHashField(featureCode);

                //document.addField(histogramField, Base64.encodeBase64String(feature.getByteArrayRepresentation()));
                //document.addField(hashesField,arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram())));
                sb.append("<field name=\"" + histogramField + "\">");
                sb.append(Base64.encodeBase64String(feature.getByteArrayRepresentation()));
                sb.append("</field>\n");
                sb.append("<field name=\"" + hashesField + "\">");
                sb.append(arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram())));
                sb.append("</field>");
            }
        }
        System.out.println(sb.toString());


    }
}
