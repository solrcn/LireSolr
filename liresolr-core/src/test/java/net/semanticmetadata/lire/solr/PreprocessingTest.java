package net.semanticmetadata.lire.solr;

import com.jhlabs.image.DespeckleFilter;
import junit.framework.TestCase;
import net.semanticmetadata.lire.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This file is part of LIRE, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 30.11.2014
 */
public class PreprocessingTest extends TestCase {

    private int maxSideLength = 512;

    public void testSingleImage() throws IOException {
        String num = "1031";
        File file = new File("testdata/"+num+".tif");
        BufferedImage read = ImageIO.read(file);
        BufferedImage img = ImageUtils.createWorkingCopy(read);
        // despeckle
        DespeckleFilter df = new DespeckleFilter();
        img = df.filter(img, null);
        img = ImageUtils.trimWhiteSpace(img); // trims white space
        if (maxSideLength > 50)
            img = ImageUtils.scaleImage(img, maxSideLength);

        ImageIO.write(img, "png", new File("testdata/"+num+".png"));
    }

    public void testSplit() {
        System.out.println("test:rest".split(":")[1]);
    }
}
