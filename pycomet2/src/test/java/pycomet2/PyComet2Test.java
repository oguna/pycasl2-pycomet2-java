package pycomet2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class PyComet2Test {
    protected final static File tmpDir = new File("tmp");

    protected static String runPyComet2(String[] args, String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(input.getBytes());
        PrintStream defaultOut = System.out;
        PrintStream defaultErr = System.err;
        InputStream defaultIn = System.in;
        try (PrintStream newOut = new PrintStream(out)) {
            System.setOut(newOut);
            System.setErr(newOut);
            System.setIn(in);
            PyComet2.main(args);
        } finally {
            System.setOut(defaultOut);
            System.setErr(defaultErr);
            System.setIn(defaultIn);
            System.out.println(out.toString());
        }
        return out.toString();
    }

    protected static String runPyComet2(String[] args) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream defaultOut = System.out;
        PrintStream defaultErr = System.err;
        try {
            System.setOut(new PrintStream(out));
            System.setErr(new PrintStream(out));
            PyComet2.main(args);
        } finally {
            System.setOut(defaultOut);
            System.setErr(defaultErr);
            System.out.println(out.toString());
        }
        return out.toString();
    }


    protected static String parseExpected(Path path) throws Exception {
        try (BufferedReader br = Files.newBufferedReader(path)){
            StringBuilder sb = new StringBuilder();
            String line;
            boolean expectedLine = false;
            while ((line = br.readLine()) != null) {
                if (expectedLine) {
                    sb.append(line);
                    sb.append('\n');
                } else if (line.startsWith("## executing")) {
                    expectedLine = true;
                }
            }
            return sb.toString();
        }
    }}
