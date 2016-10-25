package pycomet2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PyComet2EnshuDTest extends PyComet2Test {
    @Before
    public void before() {
        tmpDir.mkdir();
    }

    @Test(timeout = 1000)
    public void test001() throws Exception {
        test("001");
    }

    @Test(timeout = 1000)
    public void test002() throws Exception {
        test("002");
    }

    @Test(timeout = 1000)
    public void test003() throws Exception {
        test("003");
    }

    @Test(timeout = 1000)
    public void test004() throws Exception {
        test("004");
    }

    @Test(timeout = 1000)
    public void test005() throws Exception {
        test("005");
    }

    @Test(timeout = 1000)
    public void test006() throws Exception {
        test("006");
    }

    @Test(timeout = 1000)
    public void test007() throws Exception {
        test("007");
    }

    @Test(timeout = 1000)
    public void test008() throws Exception {
        test("008");
    }

    @Test(timeout = 1000)
    public void test009() throws Exception {
        test("009");
    }

    @Test(timeout = 1000)
    public void test010() throws Exception {
        test("010", "36\n48\n");
    }

    @Test(timeout = 1000)
    public void test011() throws Exception {
        test("011");
    }

    @Test(timeout = 1000)
    public void test012() throws Exception {
        test("012");
    }

    @Test(timeout = 1000)
    public void test013() throws Exception {
        test("013");
    }

    private static void test(String target) throws Exception {
        Path comPath = Paths.get(PyComet2BatchModeTest.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path ansPath = Paths.get(PyComet2BatchModeTest.class.getResource("/enshu-d/" + target + ".ans").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream defaultOut = System.out;
        PrintStream defaultErr = System.err;
        try {
            System.setOut(new PrintStream(out));
            System.setErr(new PrintStream(out));
            PyComet2.main(new String[]{inputPath.toString(), "-r"});
        } finally {
            System.setOut(defaultOut);
            System.setErr(defaultErr);
            System.out.println(out.toString());
        }
        String actual = out.toString().replace("\r", "");
        String expected = parseExpected(ansPath);
        Assert.assertEquals(expected, actual);
    }

    private static void test(String target, String input) throws Exception {
        Path comPath = Paths.get(PyComet2BatchModeTest.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path ansPath = Paths.get(PyComet2BatchModeTest.class.getResource("/enshu-d/" + target + ".ans").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(input.getBytes());
        PrintStream defaultOut = System.out;
        InputStream defaultIn = System.in;
        try {
            System.setOut(new PrintStream(out));
            System.setIn(in);
            PyComet2.main(new String[]{inputPath.toString(), "-r"});
        } finally {
            System.setOut(defaultOut);
            System.setIn(defaultIn);
            System.out.println(out.toString());
        }
        String actual = out.toString().replace("\r", "");
        String expected = parseExpected(ansPath);
        Assert.assertEquals(expected, actual);
    }

}
