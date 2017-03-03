package edu.mit.ll.em.api.formatter;

import edu.mit.ll.em.api.rs.impl.DatalayerServiceImpl;
import edu.mit.ll.nics.common.entity.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

public class KmlFormatterTest {
    KmlFormatter formatter;

    @Before
    public void setup(){
        formatter = new KmlFormatter();
    }

    @Test
    public void testRegularExpressionDoesNotMatchStringBeginingwithXmlAndFollowedByDocumentTag(){
        String inputString = "<xml foo='asdf'><Document></Document></xml>";
        Matcher matcher = KmlFormatter.MALFORMED_KML_PATTERN.matcher(inputString);
        assertFalse ("Potential bug! Test is documenting current behavior. Fix test if bug in code fixed", matcher.find());
    }

    @Test
    public void testRegularExpressionDoesNotMatchStringBeginingWithDocumentTags(){
        String inputString = "<Document></Document>";
        Matcher matcher = KmlFormatter.MALFORMED_KML_PATTERN.matcher(inputString);
        assertFalse ("Potential bug! Test is documenting current behavior. Fix test if bug in code fixed", matcher.find());
    }
    @Test
    public void testRegularExpressionDoesNotMatchStringBeginingWithXmlKmlAndDocumentTags(){
        String inputString = "<xml><kml xmlns:somenamespace ><Document></Document>";
        Matcher matcher = KmlFormatter.MALFORMED_KML_PATTERN.matcher(inputString);
        assertFalse ("Potential bug! Test is documenting current behavior. Fix test if bug in code fixed", matcher.find());
    }

    @Test
    public void testValidInputIsNotFormatted() throws IOException {
        String fileData = "<xml><Document></Document>";
        byte[] inputData = fileData.getBytes();
        InputStream input = new ByteArrayInputStream(inputData);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        formatter.format(input, output);

        String writtenString = new String(output.toByteArray());
        assertEquals("Expected the input to not have been changed", fileData, writtenString);
    }

    //This is probably a bug. The intended behavior should insert a kml tag if it is not present.
    // Flip assertion once bug is fixed or delete test if this is not expected behavior
    @Test
    public void testMissingXMLTagIsAddedToContent() throws IOException {
        String fileData = "<xml xmlns:atom><Document></Document>";
        byte[] inputData = fileData.getBytes();
        InputStream input = new ByteArrayInputStream(inputData);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        formatter.format(input, output);

        String writtenString = new String(output.toByteArray());
        String expectedOutputString = "<xml xmlns:atom>" + KmlFormatter.KML_ROOT_START_TAG + "<Document></Document></kml>";
        assertNotEquals("Expected input to have kml tag added around document", expectedOutputString, writtenString);
    }
}