package org.solrcn.mapreduce.lire;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jhlabs.image.DespeckleFilter;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.solr.FeatureRegistry;
import net.semanticmetadata.lire.solr.indexing.ImageDataProcessor;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.solr.common.SolrInputDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by longkeyy on 15-4-22.
 */
public class ImageToDocument {

    private int maxSideLength = 512;
    private List<LireFeature> features = Lists.newArrayList();
    private Class imageDataProcessor;
    public static String uniqueKey = "id";

    static {

        try {
            BitSampling.readHashFunctions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  ImageToDocument(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        for (int i = 0; i < args.length; i++) {
            if("-r".equals(args[i])){
                Class<?> imageDataProcessorClass = Class.forName(args[++i]);
                if (imageDataProcessorClass.newInstance() instanceof ImageDataProcessor){
                    imageDataProcessor = imageDataProcessorClass;
                }
            }
        }
        ImageDataProcessor idp = null;
        try {
            if (imageDataProcessor != null) {
                idp = (ImageDataProcessor) imageDataProcessor.newInstance();
            }
        } catch (Exception e) {
            System.err.println("Could not instantiate ImageDataProcessor!");
            e.printStackTrace();
        }
    }

    public ImageToDocument() {
        features.add(new PHOG());
        features.add(new ColorLayout());
        features.add(new EdgeHistogram());
        features.add(new JCD());

        // new features
        features.add(new CEDD());
        features.add(new ScalableColor());
    }

    private void addFeatures(List features) {
        features.addAll(features);
    }

    private void setFeatures(List features) {
        this.features = features;
    }

    private void addFeature(LireFeature feature) {
        features.add(feature);
    }

    private List<LireFeature> getFeatures() {
        return features;
    }

    public String arrayToString(final int[] array) {
        StringBuilder sb = new StringBuilder(array.length * 8);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(Integer.toHexString(array[i]));
        }
        return sb.toString();
    }

    public BufferedImage getImgFromUrl(final String fileUrl) throws IOException {
        BufferedImage img = ImageIO.read(new URL(fileUrl.replaceAll(" ", "%20")).openStream());
//        BufferedImage img = ImageUtils.createWorkingCopy(read);
        DespeckleFilter df = new DespeckleFilter();
        img = df.filter(img, null);
        img = ImageUtils.trimWhiteSpace(img); // trims white space
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
        return img;
    }

    public String getXML(final String id, final String fileUrl) throws IOException {
        BufferedImage img = getImgFromUrl(fileUrl);
        StringBuilder sb = new StringBuilder();
        sb.append("<doc>");
        sb.append("<field name=\""+uniqueKey+"\">");
        sb.append(id);//tmp.getFileName()
        sb.append("</field>");
        sb.append("<field name=\"url\">");
        sb.append(fileUrl);
        sb.append("</field>");
        for (LireFeature feature : features) {
            String featureCode = FeatureRegistry.getCodeForClass(feature.getClass());
            if (featureCode != null) {
                feature.extract(img);
                String histogramField = FeatureRegistry.codeToFeatureField(featureCode);
                String hashesField = FeatureRegistry.codeToHashField(featureCode);
//                document.addField(histogramField, Base64.encodeBase64String(feature.getByteArrayRepresentation()));
                //document.addField(hashesField,arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram())));
                sb.append("<field name=\"" + histogramField + "\">");
                sb.append(Base64.encodeBase64String(feature.getByteArrayRepresentation()));
                sb.append("</field>");
                sb.append("<field name=\"" + hashesField + "\">");
                sb.append(arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram())));
                sb.append("</field>");
            }
        }
        sb.append("</doc>\n");
        return sb.toString();
    }

    public SolrInputDocument getSolrInpuDocument(final String id, final String fileUrl) throws IOException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField(uniqueKey, id);
        BufferedImage img = getImgFromUrl(fileUrl);
        for (LireFeature feature : features) {
            feature.extract(img);
            String featureCode = FeatureRegistry.getCodeForClass(feature.getClass());
            if (featureCode != null) {
                String histogramField = FeatureRegistry.codeToFeatureField(featureCode);
                String hashesField = FeatureRegistry.codeToHashField(featureCode);
                doc.addField(histogramField, Base64.encodeBase64String(feature.getByteArrayRepresentation()));
                doc.addField(hashesField, arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram())));
            }
        }
        return doc;
    }

    public Map<String, int[]> getImg(final String fileUrl) throws IOException {
        BufferedImage img = getImgFromUrl(fileUrl);

        Map<String, int[]> fm = Maps.newHashMap();
        for (LireFeature feature : features) {
            feature.extract(img);
            fm.put(feature.getClass().getSimpleName(),BitSampling.generateHashes(feature.getDoubleHistogram()));
            int[] ints = BitSampling.generateHashes(feature.getDoubleHistogram());
        }
        return fm;
    }

    public String getFseMessage(final String id, final String fileUrl) throws IOException {
        BufferedImage img = getImgFromUrl(fileUrl);
        StringBuilder sb = new StringBuilder();
        sb.append(uniqueKey).append("\2").append(id).append("\1");
        Map<String, int[]> fm = Maps.newHashMap();
        for (LireFeature feature : features) {
            feature.extract(img);
            String featureCode = FeatureRegistry.getCodeForClass(feature.getClass());
            if (featureCode != null) {
                String histogramField = FeatureRegistry.codeToFeatureField(featureCode);
                String hashesField = FeatureRegistry.codeToHashField(featureCode);
                sb.append(histogramField).append("\2").append(Base64.encodeBase64String(feature.getByteArrayRepresentation())).append("\1");
                sb.append(hashesField).append("\2").append(arrayToString(BitSampling.generateHashes(feature.getDoubleHistogram()))).append("\1");
            }
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

}
