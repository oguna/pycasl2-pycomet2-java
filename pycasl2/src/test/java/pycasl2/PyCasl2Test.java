package pycasl2;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PyCasl2Test {


    private final static File tmpDir = new File("tmp");

    @Before
    public void before() {
        tmpDir.mkdir();
    }

    @Test
    public void test001() throws Exception {
        test("001");
    }

    @Test
    public void test002() throws Exception {
        test("002");
    }

    @Test
    public void test003() throws Exception {
        test("003");
    }

    @Test
    public void test004() throws Exception {
        test("004");
    }

    @Test
    public void test005() throws Exception {
        test("005");
    }

    @Test
    public void test006() throws Exception {
        test("006");
    }

    @Test
    public void test007() throws Exception {
        test("007");
    }

    @Test
    public void test008() throws Exception {
        test("008");
    }

    @Test
    public void test009() throws Exception {
        test("009");
    }

    @Test
    public void test010() throws Exception {
        test("010");
    }

    @Test
    public void test011() throws Exception {
        test("011");
    }

    @Test
    public void test012() throws Exception {
        test("012");
    }

    @Test
    public void test013() throws Exception {
        test("013");
    }

    @Test
    public void bug3() throws Exception {
        Path casPath = Paths.get(PyCasl2Test.class.getResource("/bug3.cas").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), casPath.getFileName().toString());
        Files.copy(casPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        PyCasl2.main(new String[]{inputPath.toString()});
    }

    @Test
    public void bug4() throws Exception {
        test("bug4");
    }

    private void test(String target) throws Exception {
        Path casPath = Paths.get(PyCasl2Test.class.getResource("/enshu-d/" + target + ".cas").toURI());
        Path comPath = Paths.get(PyCasl2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".cas");
        Path outputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(casPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        PyCasl2.main(new String[]{inputPath.toString()});
        byte[] expected = Files.readAllBytes(comPath);
        byte[] actual = Files.readAllBytes(outputPath);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testVersion() throws IOException {
        String actual = runPyCasl2(new String[]{"-v"}).replace("\r", "");
        String expected = "PyCASL2 version 1.1.6\n" +
                "$Revision: 42606859abf2 $\n" +
                "Copyright (c) 2009,2011, Masahiko Nakamoto.\n" +
                "All rights reserved.\n";
        assertEquals(expected, actual);
    }

    @Test
    public void testHelp() throws IOException {
        String actual = runPyCasl2(new String[]{"-h"}).replace("\r", "");
        String expected = "Usage: PyCasl2 [options] input.cas [output.com]\n" +
                "  -a           turn on verbose listings\n" +
                "  -v --version display version and exit\n";
        assertEquals(expected, actual);
    }

    private static String runPyCasl2(String[] args) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream defaultOut = System.out;
        PrintStream defaultErr = System.err;
        try (PrintStream ps = new PrintStream(out)) {
            System.setOut(ps);
            System.setErr(ps);
            PyCasl2.main(args);
        } finally {
            System.setOut(defaultOut);
            System.setErr(defaultErr);
            System.out.println(out.toString());
        }
        return out.toString().replace("\r", "");
    }
}
