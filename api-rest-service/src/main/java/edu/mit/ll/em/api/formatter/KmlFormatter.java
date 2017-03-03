package edu.mit.ll.em.api.formatter;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KmlFormatter {
    /** A standard KML root element. */
    public static final String KML_ROOT_START_TAG =
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\" " +
            "xmlns:gx=\"http://www.google.com/kml/ext/2.2\" " +
            "xmlns:kml=\"http://www.opengis.net/kml/2.2\" " +
            "xmlns:atom=\"http://www.w3.org/2005/Atom\">";

    /** A pattern that matches KML documents without a root <kml> element. Escapes for Question mark seems off by two. Need  */
    public static final Pattern MALFORMED_KML_PATTERN = Pattern.compile("^\\s*<\\?xml[^>]+>\\s*<Document>", Pattern.MULTILINE);

    public void format(InputStream input, OutputStream output) throws IOException {
        formatMissingKmlTag(input, output);
    }

    public void formatMissingKmlTag(InputStream input, OutputStream output) throws IOException{
        byte[] buffer = new byte[4096];
        int n;

        // Convert the first (maximum of) 4096 bytes to a string.
        if (-1 == (n = input.read(buffer)))
            return;
        String prologue = new String(buffer, 0, n, "UTF-8");

        // Attempt to repair the document prologue, if a root <kml> tag is missing.
        Matcher matcher = MALFORMED_KML_PATTERN.matcher(prologue);
        boolean missingKMLTag = matcher.find();
        if (missingKMLTag) {
            int insertionPoint = matcher.end() - 10; // Insertion point, before <Document> tag.

            IOUtils.write(prologue.substring(0, insertionPoint), output);
            IOUtils.write(KML_ROOT_START_TAG, output);
            IOUtils.write(prologue.substring(insertionPoint), output);
        }

        // Otherwise, simply write out the byte buffer and signal that no epilogue is needed.
        else {
            output.write(buffer, 0, n);
            prologue = null;
        }

        // Write out the rest of the stream.
        IOUtils.copy(input, output);

        // If an epilogue is needed, write it now.
        if (prologue != null) {
            IOUtils.write("</kml>", output);
        }

    }
}
