package crawlercommons.mimetypes;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class MimeTypeDetectorTest {

    @Test
    public void testXMLDetection() throws Exception {
        MimeTypeDetector detector = new MimeTypeDetector();

        byte[] content = getSitemap("atom.xml");
        String mimeType = detector.detect(content);
        assertTrue(detector.isXml(mimeType));
        assertFalse(detector.isText(mimeType));
        assertFalse(detector.isGzip(mimeType));
    }

    @Test
    public void testTextDetection() throws IOException {
        MimeTypeDetector detector = new MimeTypeDetector();
        byte[] content = getSitemap("sitemap.txt");
        String mimeType = detector.detect(content);
        assertFalse(detector.isXml(mimeType));
        assertTrue(detector.isText(mimeType));
        assertFalse(detector.isGzip(mimeType));

        content = getSitemap("sitemap-with-bom.txt");
        mimeType = detector.detect(content);
        assertFalse(detector.isXml(mimeType));
        assertTrue(detector.isText(mimeType));
        assertFalse(detector.isGzip(mimeType));
    }

    @Test
    public void testGzipDetection() throws IOException {
        MimeTypeDetector detector = new MimeTypeDetector();

        byte[] content = getSitemap("xmlSitemap.gz");
        String mimeType = detector.detect(content);
        assertFalse(detector.isXml(mimeType));
        assertFalse(detector.isText(mimeType));
        assertTrue(detector.isGzip(mimeType));

        content = getSitemap("sitemap.txt.gz");
        mimeType = detector.detect(content);
        assertFalse(detector.isXml(mimeType));
        assertFalse(detector.isText(mimeType));
        assertTrue(detector.isGzip(mimeType));
    }

    private byte[] getSitemap(String filename) throws IOException {
        return IOUtils.toByteArray(MimeTypeDetectorTest.class.getResourceAsStream("/sitemaps/" + filename));
    }

}
