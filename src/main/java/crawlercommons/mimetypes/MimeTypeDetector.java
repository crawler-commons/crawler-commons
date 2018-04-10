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

    private static final int LEADING_WHITESPACE_MAX_SKIP = 16;

    private final static boolean[] spaceCharacters = new boolean[256];
    static {
        spaceCharacters[0x09] = true; // \t - character tabulation (ht)
        spaceCharacters[0x0a] = true; // \n - line feed (lf)
        spaceCharacters[0x0b] = true; // line tabulation (vt)
        spaceCharacters[0x0c] = true; // form feed (ff)
        spaceCharacters[0x0d] = true; // \r - carriage return (cr)
        spaceCharacters[0x20] = true; // space
    }

    private static class MimeTypeEntry {
        private String mimeType;
        private byte[] pattern;
        private boolean allowBOM;
        private boolean allowLeadingSpace;

        public MimeTypeEntry(String mimeType, String pattern) {
            this(mimeType, pattern, false, false);
        }

        public MimeTypeEntry(String mimeType, String pattern, boolean allowBOM, boolean allowLeadingSpace) {
            this.mimeType = mimeType;
            this.allowBOM = allowBOM;
            this.allowLeadingSpace = allowLeadingSpace;
            this.pattern = pattern.getBytes(StandardCharsets.UTF_8);
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
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<?xml", true, true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<?XML", true, true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<!--", true, true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<urlset", true, true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<sitemapindex", true, true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<rss", true, true));
        mimeTypes.add(new MimeTypeEntry(XML_MIMETYPES[0], "<feed", true, true));

        mimeTypes.add(new MimeTypeEntry(TEXT_MIMETYPES[0], "http://", true, true));
        mimeTypes.add(new MimeTypeEntry(TEXT_MIMETYPES[0], "https://", true, true));

        mimeTypes.add(new MimeTypeEntry(GZIP_MIMETYPES[0], "\037\213"));
        mimeTypes.add(new MimeTypeEntry(GZIP_MIMETYPES[0], 0x1F, 0x8B));

        maxPatternLength = 0;
        for (MimeTypeEntry entry : mimeTypes) {
            int length = entry.getPattern().length;
            if (entry.allowBOM)
                length += UTF8_BOM.length;
            if (entry.allowLeadingSpace)
                length += LEADING_WHITESPACE_MAX_SKIP;
            maxPatternLength = Math.max(maxPatternLength, length);
        }
    }

    public String detect(byte[] content) {
        return detect(content, 0, content.length);
    }

    public String detect(byte[] content, int offset, int length) {
        int offsetBOM = -1;
        int offsetSpace = -1;
        for (MimeTypeEntry entry : mimeTypes) {
            if (patternMatches(entry.getPattern(), content, offset, length)) {
                return entry.getMimeType();
            }
            if (entry.allowBOM) {
                if (offsetBOM == -1) {
                    offsetBOM = offset;
                    while (patternMatches(UTF8_BOM, content, offsetBOM, length) && offsetBOM < content.length) {
                        offsetBOM += UTF8_BOM.length;
                    }
                }
                if (patternMatches(entry.getPattern(), content, offsetBOM, length)) {
                    return entry.getMimeType();
                }
            }
            if (entry.allowLeadingSpace) {
                if (offsetSpace == -1) {
                    offsetSpace = (offsetBOM > 0 ? offsetBOM : 0);
                    while (offsetSpace < content.length && spaceCharacters[content[offsetSpace] & 0xFF]) {
                        offsetSpace++;
                    }
                }
                if (patternMatches(entry.getPattern(), content, offsetSpace, length)) {
                    return entry.getMimeType();
                }
            }
        }

        // No mime-type detected.
        return null;
    }

    private boolean patternMatches(byte[] pattern, byte[] content, int offset, int length) {
        if (pattern.length > length) {
            return false;
        }

        for (int i = 0; i < pattern.length && (offset + i) < content.length; i++) {
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
