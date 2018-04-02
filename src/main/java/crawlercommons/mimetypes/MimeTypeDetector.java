package crawlercommons.mimetypes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MimeTypeDetector {

    private static String[] XML_MIMETYPES = new String[] {
                    "application/xml",
                    "application/x-xml",
                    "text/xml",
                    "application/atom+xml",
                    "application/rss+xml",
                    "text/rss"
                    };

    private static String[] TEXT_MIMETYPES = new String[] {
                    "text/plain"
                    };

    private static String[] GZIP_MIMETYPES = new String[] {
                    "application/gzip",
                    "application/gzip-compressed",
                    "application/gzipped",
                    "application/x-gzip",
                    "application/x-gzip-compressed",
                    "application/x-gunzip",
                    "gzip/document"
                    };

    private static String[][] MIMETYPES = {
                    XML_MIMETYPES,
                    TEXT_MIMETYPES,
                    GZIP_MIMETYPES
                    };

    private static byte[] UTF8_BOM = {
                    (byte) 0xEF,
                    (byte) 0xBB,
                    (byte) 0xBF
                    };

    private static class MimeTypeEntry {
        private String mimeType;
        private byte[] pattern;

        public MimeTypeEntry(String mimeType, String pattern) {
            this(mimeType, pattern, false);
        }

        public MimeTypeEntry(String mimeType, String pattern, boolean addBOM) {
            this.mimeType = mimeType;

            byte[] patternBytes = pattern.getBytes(StandardCharsets.UTF_8);
            if (addBOM) {
                this.pattern = new byte[UTF8_BOM.length + patternBytes.length];
                System.arraycopy(UTF8_BOM, 0, this.pattern, 0, UTF8_BOM.length);
                System.arraycopy(patternBytes, 0, this.pattern, UTF8_BOM.length, patternBytes.length);
            } else {
                this.pattern = patternBytes;
            }
        }

        public MimeTypeEntry(String mimeType, int... pattern) {
            this.mimeType = mimeType;
            this.pattern = makeBytePattern(pattern);
        }

        private byte[] makeBytePattern(int[] pattern) {
            byte[] result = new byte[pattern.length];
            for (int i = 0; i < pattern.length; i++) {
                result[i] = (byte) (pattern[i] & 0xFF);
            }

            return result;
        }

        public String getMimeType() {
            return mimeType;
        }

        public byte[] getPattern() {
            return pattern;
        }
    }

    private List<MimeTypeEntry> mimeTypes;
    private int maxPatternLength;

    public MimeTypeDetector() {
        mimeTypes = new ArrayList<>();

        // Add all text patterns without and with a BOM.
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<?xml"));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<?xml", true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<?XML"));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<?XML", true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<!--"));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<!--", true));

        mimeTypes.add(new MimeTypeEntry(TEXT_MIMETYPES[0], "http://"));
        mimeTypes.add(new MimeTypeEntry(TEXT_MIMETYPES[0], "http://", true));
        mimeTypes.add(new MimeTypeEntry(TEXT_MIMETYPES[0], "https://"));
        mimeTypes.add(new MimeTypeEntry(TEXT_MIMETYPES[0], "https://", true));

        mimeTypes.add(new MimeTypeEntry(GZIP_MIMETYPES[0], "\037\213"));
        mimeTypes.add(new MimeTypeEntry(GZIP_MIMETYPES[0], 0x1F, 0x8B));

        maxPatternLength = 0;
        for (MimeTypeEntry entry : mimeTypes) {
            maxPatternLength = Math.max(maxPatternLength, entry.getPattern().length);
        }
    }

    public String detect(byte[] content) {
        for (MimeTypeEntry entry : mimeTypes) {
            if (patternMatches(entry.getPattern(), content, 0, content.length)) {
                return entry.getMimeType();
            }
        }

        // No mime-type detected.
        return null;
    }

    public String detect(byte[] content, int offset, int length) {
        for (MimeTypeEntry entry : mimeTypes) {
            if (patternMatches(entry.getPattern(), content, offset, length)) {
                return entry.getMimeType();
            }
        }

        // No mime-type detected.
        return null;
    }

    private boolean patternMatches(byte[] pattern, byte[] content, int offset, int length) {
        if (pattern.length > length) {
            return false;
        }

        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] != content[offset + i]) {
                return false;
            }
        }

        return true;
    }

    public String detect(InputStream is) throws IOException {
        if (!is.markSupported()) {
            throw new IllegalArgumentException("Can't detect mime type for input stream that doesn't support mark/reset");
        }

        is.mark(maxPatternLength);
        byte[] content = new byte[maxPatternLength];

        try {
            int contentLength = is.read(content);
            return detect(content, 0, contentLength);
        } finally {
            is.reset();
        }
    }

    public boolean isXml(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        for (String xmlMimeType : XML_MIMETYPES) {
            if (mimeType.equals(xmlMimeType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isText(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        for (String textMimeType : TEXT_MIMETYPES) {
            if (mimeType.equals(textMimeType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isGzip(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        for (String gzipMimeType : GZIP_MIMETYPES) {
            if (mimeType.equals(gzipMimeType)) {
                return true;
            }
        }

        return false;
    }

    public String normalize(String contentType, byte[] content) {
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        for (String[] mimeTypes : MIMETYPES) {
            for (String mimeType : mimeTypes) {
                if (normalizedContentType.equals(mimeType)) {
                    return mimeTypes[0];
                }
            }
        }

        String result = detect(content);
        if (result != null) {
            return result;
        }

        return null;
    }

}
