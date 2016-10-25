package pycomet2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

public class PyComet2InteractiveModeTest extends PyComet2Test {
    @Before
    public void before() {
        tmpDir.mkdir();
    }

    @Test(timeout = 1000)
    public void testStep() throws Exception {
        String target = "001";
        String input = "s\ns\ns\ns\ns\ns\ns\ns\ns\ns\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("pycomet2> ", "").replace("tmp\\001.com", "001.com");
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-step.txt").toURI());
        String expected = Files.lines(expectedPath).filter(e -> !e.startsWith("pycomet2> ")).collect(Collectors.joining("\n"));
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 1000)
    public void testDisassemble() throws Exception {
        String target = "001";
        String input = "di\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("tmp\\001.com", "001.com");
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-di.txt").toURI());
        String expected = new String(Files.readAllBytes(expectedPath)).replace("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 1000)
    public void testDump() throws Exception {
        String target = "001";
        String input = "du\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("tmp\\001.com", "001.com").trim();
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-du.txt").toURI());
        String expected = new String(Files.readAllBytes(expectedPath)).replace("\r", "").trim();
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 1000)
    public void testJump() throws Exception {
        String target = "001";
        String input = "j 2\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("tmp\\001.com", "001.com").trim();
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-jump.txt").toURI());
        String expected = new String(Files.readAllBytes(expectedPath)).replace("\r", "").trim();
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 1000)
    public void testMemory() throws Exception {
        String target = "001";
        String input = "m #a52 1\ndu #a52\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("tmp\\001.com", "001.com").trim();
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-memory.txt").toURI());
        String expected = new String(Files.readAllBytes(expectedPath)).replace("\r", "").trim();
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 1000)
    public void testBreak() throws Exception {
        String target = "001";
        String input = "b 7\nr\np\ni\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("tmp\\001.com", "001.com").trim();
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-break.txt").toURI());
        String expected = new String(Files.readAllBytes(expectedPath)).replace("\r", "").trim();
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 1000)
    public void testHep() throws Exception {
        String target = "001";
        String input = "h\nq\n";
        Path comPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + ".com").toURI());
        Path inputPath = Paths.get(tmpDir.toString(), target + ".com");
        Files.copy(comPath, inputPath, StandardCopyOption.REPLACE_EXISTING);
        String out = runPyComet2(new String[] {inputPath.toString()}, input);
        String actual = out.replace("\r", "").replace("tmp\\001.com", "001.com").trim();
        Path expectedPath = Paths.get(PyComet2Test.class.getResource("/enshu-d/" + target + "-help.txt").toURI());
        String expected = new String(Files.readAllBytes(expectedPath)).replace("\r", "").trim();
        Assert.assertEquals(expected, actual);
    }
}
