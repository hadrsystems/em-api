/**
 * Copyright (c) 2008-2016, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.mit.ll.em.api.formatter;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KmlFormatter {
    private static final String XSI_ATTRIBUTE = " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ";

    /** A standard KML root element. */
    protected static final String KML_ROOT_START_TAG =
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\" " +
            "xmlns:gx=\"http://www.google.com/kml/ext/2.2\" " +
            "xmlns:kml=\"http://www.opengis.net/kml/2.2\" " +
            XSI_ATTRIBUTE +
            "xmlns:atom=\"http://www.w3.org/2005/Atom\">";

    /** A pattern that matches KML documents without a root <kml> element. Escapes for Question mark seems off by two. Need  */
    protected static final Pattern MALFORMED_KML_PATTERN = Pattern.compile("^\\s*<\\?xml[^>]+>\\s*<Document>", Pattern.MULTILINE);
    private static final Pattern XSI_DEFINED_PATTERN = Pattern.compile("xmlns:xsi", Pattern.MULTILINE);
    private static final Pattern USING_XSI_PATTERN = Pattern.compile("\\s*xsi:", Pattern.MULTILINE);
    private static final Pattern KML_TAG_PATTERN = Pattern.compile("<kml|KML>", Pattern.MULTILINE);

    public void format(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int n;

        // Convert the first (maximum of) 4096 bytes to a string.
        if (-1 == (n = input.read(buffer)))
            return;
        String prologue = new String(buffer, 0, n, "UTF-8");

        // Attempt to repair the document prologue, if a root <kml> tag is missing.
        Matcher missingKMLMatcher = MALFORMED_KML_PATTERN.matcher(prologue);
        boolean missingKMLTag = missingKMLMatcher.find();

        int insertionPoint = 0;
        if (missingKMLTag) {
            insertionPoint = insertKmlTag(output, prologue, missingKMLMatcher);
        } else {
            insertionPoint = addXsiNamespace(output, prologue, insertionPoint);
        }
        writeRemainderOfProlouge(prologue, insertionPoint, output);
        writeRemainderOfInput(input, output);

        if (missingKMLTag) {
            IOUtils.write("</kml>", output);
        }
    }

    private int addXsiNamespace(OutputStream output, String prologue, int insertionPoint) throws IOException {
        Matcher usingXsiMatcher = USING_XSI_PATTERN.matcher(prologue);
        Matcher xsiDefinedMatcher = XSI_DEFINED_PATTERN.matcher(prologue);
        boolean needsXsi = usingXsiMatcher.find() && !xsiDefinedMatcher.find();
        if(needsXsi){
            Matcher kmlTagMatcher = KML_TAG_PATTERN.matcher(prologue);
            if(kmlTagMatcher.find()){
                insertionPoint = kmlTagMatcher.end();
                insertionPoint = copyInputToFile(prologue,0, insertionPoint, output);
                insertIntoFile(XSI_ATTRIBUTE, output);
            }
        }
        return insertionPoint;
    }

    private int insertKmlTag(OutputStream output, String prologue, Matcher missingKMLMatcher) throws IOException {
        int insertionPoint;
        insertionPoint = missingKMLMatcher.end() - 10; // Insertion point, before <Document> tag.
        insertionPoint = copyInputToFile(prologue,0, insertionPoint, output);
        insertIntoFile(KML_ROOT_START_TAG, output);
        return insertionPoint;
    }


    private void insertIntoFile(String data, OutputStream  output) throws IOException {
        IOUtils.write(data, output);
    }

    private int copyInputToFile(String data, int from, int to, OutputStream output) throws IOException {
        IOUtils.write(data.substring(from, to), output);
        return to;
    }
    private void writeRemainderOfProlouge(String prologue, int from, OutputStream output) throws IOException {
        IOUtils.write(prologue.substring(from), output);

    }

    private void writeRemainderOfInput(InputStream input, OutputStream output) throws IOException {
        IOUtils.copy(input, output);

    }
}
