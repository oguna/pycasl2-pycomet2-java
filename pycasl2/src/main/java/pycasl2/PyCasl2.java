package pycasl2;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PyCasl2 {

    private final static Map<String, Integer> regStr =
            IntStream.rangeClosed(0, 9).mapToObj(e -> e).collect(Collectors.toMap(e -> String.format("GR%1d", e), e -> e));

    private final static Map<String, Map.Entry<Integer, ArgType>> opTable = new HashMap<String, Map.Entry<Integer, ArgType>>() {
        {
            put("NOP", new SimpleEntry<>(0x00, ArgType.NoArg));
            put("LD2", new SimpleEntry<>(0x10, ArgType.RAdrX));
            put("ST", new SimpleEntry<>(0x11, ArgType.RAdrX));
            put("LAD", new SimpleEntry<>(0x12, ArgType.RAdrX));
            put("LD1", new SimpleEntry<>(0x14, ArgType.R1R2));
            put("ADDA2", new SimpleEntry<>(0x20, ArgType.RAdrX));
            put("SUBA2", new SimpleEntry<>(0x21, ArgType.RAdrX));
            put("ADDL2", new SimpleEntry<>(0x22, ArgType.RAdrX));
            put("SUBL2", new SimpleEntry<>(0x23, ArgType.RAdrX));
            put("ADDA1", new SimpleEntry<>(0x24, ArgType.R1R2));
            put("SUBA1", new SimpleEntry<>(0x25, ArgType.R1R2));
            put("ADDL1", new SimpleEntry<>(0x26, ArgType.R1R2));
            put("SUBL1", new SimpleEntry<>(0x27, ArgType.R1R2));
            put("AND2", new SimpleEntry<>(0x30, ArgType.RAdrX));
            put("OR2", new SimpleEntry<>(0x31, ArgType.RAdrX));
            put("XOR2", new SimpleEntry<>(0x32, ArgType.RAdrX));
            put("AND1", new SimpleEntry<>(0x34, ArgType.R1R2));
            put("OR1", new SimpleEntry<>(0x35, ArgType.R1R2));
            put("XOR1", new SimpleEntry<>(0x36, ArgType.R1R2));
            put("CPA2", new SimpleEntry<>(0x40, ArgType.RAdrX));
            put("CPL2", new SimpleEntry<>(0x41, ArgType.RAdrX));
            put("CPA1", new SimpleEntry<>(0x44, ArgType.R1R2));
            put("CPL1", new SimpleEntry<>(0x45, ArgType.R1R2));
            put("SLA", new SimpleEntry<>(0x50, ArgType.RAdrX));
            put("SRA", new SimpleEntry<>(0x51, ArgType.RAdrX));
            put("SLL", new SimpleEntry<>(0x52, ArgType.RAdrX));
            put("SRL", new SimpleEntry<>(0x53, ArgType.RAdrX));
            put("JMI", new SimpleEntry<>(0x61, ArgType.AdrX));
            put("JNZ", new SimpleEntry<>(0x62, ArgType.AdrX));
            put("JZE", new SimpleEntry<>(0x63, ArgType.AdrX));
            put("JUMP", new SimpleEntry<>(0x64, ArgType.AdrX));
            put("JPL", new SimpleEntry<>(0x65, ArgType.AdrX));
            put("JOV", new SimpleEntry<>(0x66, ArgType.AdrX));
            put("PUSH", new SimpleEntry<>(0x70, ArgType.AdrX));
            put("POP", new SimpleEntry<>(0x71, ArgType.R));
            put("CALL", new SimpleEntry<>(0x80, ArgType.AdrX));
            put("RET", new SimpleEntry<>(0x81, ArgType.NoArg));
            put("SVC", new SimpleEntry<>(0xf0, ArgType.AdrX));
            put("IN", new SimpleEntry<>(0x90, ArgType.StrLen));
            put("OUT", new SimpleEntry<>(0x91, ArgType.StrLen));
            put("RPUSH", new SimpleEntry<>(0xa0, ArgType.NoArg));
            put("RPOP", new SimpleEntry<>(0xa1, ArgType.NoArg));
            put("LD", new SimpleEntry<>(-1, null));
            put("ADDA", new SimpleEntry<>(-2, null));
            put("SUBA", new SimpleEntry<>(-3, null));
            put("ADDL", new SimpleEntry<>(-4, null));
            put("SUBL", new SimpleEntry<>(-5, null));
            put("AND", new SimpleEntry<>(-6, null));
            put("OR", new SimpleEntry<>(-7, null));
            put("XOR", new SimpleEntry<>(-8, null));
            put("CPA", new SimpleEntry<>(-9, null));
            put("CPL", new SimpleEntry<>(-10, null));
            put("START", new SimpleEntry<>(-100, ArgType.Start));
            put("END", new SimpleEntry<>(-101, null));
            put("DS", new SimpleEntry<>(0, ArgType.Ds));
            put("DC", new SimpleEntry<>(0, ArgType.Dc));
        }
    };

    private static int a2l(int x) {
        x &= 0xffff;
        if (0 <= x) {
            return x;
        } else {
            return x + (1 << 16);
        }
    }

    private final Map<String, Label> symbols;
    private final EnumMap<ArgType ,BiFunction<String, String[], Object[]>> genCodeFunc;
    private int addr;
    private int labelCount;
    private final List<ByteCode> additionalDC;
    private boolean startFound;
    private String currentScope;
    private File file;
    private BufferedReader fp;
    private int currentLineNumber;
    private Instruction nextLine;
    private String nextSrc;
    private List<ByteCode> tmpCode = new ArrayList<>();
    private String currentSrc;

    private PyCasl2() {
        this.symbols = new HashMap<>();
        this.genCodeFunc = new EnumMap<>(ArgType.class);
        this.genCodeFunc.put(ArgType.NoArg, this::genCodeNoArg);
        this.genCodeFunc.put(ArgType.R, this::genCodeR);
        this.genCodeFunc.put(ArgType.R1R2, this::genCodeR1R2);
        this.genCodeFunc.put(ArgType.AdrX, this::genCodeAdrX);
        this.genCodeFunc.put(ArgType.RAdrX, this::genCodeRAdrX);
        this.genCodeFunc.put(ArgType.Ds, this::genCodeDs);
        this.genCodeFunc.put(ArgType.Dc, this::genCodeDc);
        this.genCodeFunc.put(ArgType.StrLen, this::genCodeStrLen);
        this.genCodeFunc.put(ArgType.Start, this::genCodeStart);
        this.addr = 0;
        this.labelCount = 0;
        this.additionalDC = new ArrayList<>();
        this.startFound = false;
        this.currentScope = "";
    }

    private void dump(ByteCode[] code) {
        System.out.println("Addr\tOp\t\tLine\tSource code");
        for (ByteCode c : code) {
            if (c.code != null) {
                if (c.code[0].equals(0x4341)) {
                    continue;
                }
            }
            System.out.println(c);
        }
        System.out.println();
        System.out.println("Defined labels");
        List<Label> labels = this.symbols.values().stream().sorted((o1, o2) -> o1.lines - o2.lines).collect(Collectors.toList());
        labels.forEach(System.out::println);
    }

    private ByteCode[] assemble(File file) throws IOException {
        this.file = file;
        this.addr = 0;
        this.fp = new BufferedReader(new FileReader(file));
        this.currentLineNumber = -1;
        this.nextLine = new Instruction(null, "", null, -1, "");
        this.nextSrc = "";
        this.tmpCode = new ArrayList<>();

        try {
            this.getLine();
            this.isValidProgram();
        } catch (Error e) {
            e.report();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: Unexpected error at the following line.");
            System.err.println(String.format("Line %d: %s", currentLineNumber, currentSrc));
            System.err.println(e.toString());
            System.exit(1);
        } finally {
            fp.close();
        }

        List<ByteCode> codeList = null;
        try {
            codeList = this.tmpCode.stream().filter(e -> e != null).map(this::replaceLabel).collect(Collectors.toList());
        } catch (Error e) {
            e.report();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: Unexpected error.");
            System.err.println("Exception type:" + e.getClass());
            System.err.println(e.toString());
            System.exit(1);
        }

        codeList.addAll(this.additionalDC);

        return codeList.toArray(new ByteCode[0]);
    }

    private boolean isValidProgram() {
        while (true) {
            if (!this.isSTART()) {
                throw new Error(currentLineNumber, currentSrc, "START is not found.");
            }
            boolean retFlg = false;
            while (!(this.nextLine.op.equals("END") || this.nextLine.op.equals("EOF"))) {
                if (this.nextLine.op.equals("RET")) {
                    retFlg = true;
                }
                if (!isValidInstruction()) {
                    throw new Error(currentLineNumber, currentSrc, "Invalid operation is found.");
                }
            }

            if (!retFlg) {
                throw new Error(currentLineNumber, currentSrc, "RET is not found.");
            }

            if (!isEND()) {
                throw new Error(currentLineNumber, currentSrc, "END is not found.");
            }

            if (nextLine.op.equals("EOF")) {
                break;
            }
        }
        return true;
    }

    private Instruction getLine() {
        Instruction current = this.nextLine;
        this.currentSrc = this.nextSrc;
        String line;
        while (true) {
            try {
                line = fp.readLine();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (line != null) {
                line = line.replaceAll("\\s+$", "");
            }
            this.currentLineNumber += 1;
            this.nextSrc = line;

            if (line == null) {
                this.nextLine = new Instruction(null, "EOF", null, this.currentLineNumber + 1, "");
                return current;
            }
            line = line.split(";")[0].replaceAll("\\s+$", "");
            if (!line.isEmpty()) {
                break;
            }
        }
        this.nextLine = this.splitLine(line, this.currentLineNumber + 1);
        return current;
    }

    private boolean isSTART() {
        Instruction i = this.getLine();
        if (!Objects.equals(i.op, "START")) {
            return false;
        }
        this.tmpCode.add(this.convert(i));
        return true;
    }

    private boolean isEND() {
        Instruction i = this.getLine();
        if (!i.op.equals("END")) {
            return false;
        }
        this.tmpCode.add(this.convert(i));
        return true;
    }

    private boolean isValidInstruction() {
        Instruction i = this.getLine();
        if (i.op.equals("END") || i.op.equals("START")) {
            return false;
        }
        this.tmpCode.add(this.convert(i));
        return true;
    }

    private ByteCode replaceLabel(ByteCode byteCode) {
        BiFunction<Object, ByteCode, Integer> conv = (x, bcode) -> {
            assert x instanceof String || x instanceof Integer;
            if (x instanceof String) {
                String s = (String) x;
                if (s.startsWith("=")) {
                    return this.genAdditionalDC(s, bcode.lineNumber);
                }
                String globalName = "." + s.split("\\.")[1];
                if (this.symbols.containsKey(s)) {
                    return this.symbols.get(s).addr;
                } else if (this.symbols.containsKey(globalName)) {
                    if (this.symbols.get(globalName).jumpto.isEmpty()) {
                        return this.symbols.get(globalName).addr;
                    } else {
                        throw new Error(bcode.lineNumber, bcode.src, String.format("Undefined label \"%s\" was found.", ((String) x).split(".")[1]));
                    }
                } else {
                    throw new Error(bcode.lineNumber, bcode.src, String.format("Undefined label \"%s\" was found.", ((String) x).split(".")[1]));
                }
            } else {
                return (Integer) x;
            }
        };
        Object[] code = Arrays.stream(byteCode.code).map(e -> conv.apply(e, byteCode)).toArray();
        return new ByteCode(code, byteCode.addr, byteCode.lineNumber, byteCode.src);
    }

    private Instruction splitLine(String line, int lineNumber) {
        Matcher result = Pattern.compile("^\\s*$").matcher(line);
        if (result.matches()) {
            return null;
        }

        String re_label = "(?<label>[A-Za-z_][A-Za-z0-9_]*)?";
        String re_op = "\\s+(?<op>[A-Z]+)";
        String re_arg1 = "(?<arg1>=?(([-#]?[A-Za-z0-9_]+)|(\'.*\')))";
        String re_arg2 = "(?<arg2>=?(([-#]?[A-Za-z0-9_]+)|(\'.*\')))";
        String re_arg3 = "(?<arg3>=?(([-#]?[A-Za-z0-9_]+)|(\'.*\')))";
        String re_args = "(\\s+" + re_arg1 + "(\\s*,\\s*" + re_arg2 + "(\\s*,\\s*" + re_arg3 + ")?)?)?";
        String re_comment = "(\\s*(;(?<comment>.+)?)?)?";
        String pattern = "(^" + re_label + re_op + re_args + ")?" + re_comment;

        result = Pattern.compile(pattern).matcher(line);
        if (!result.matches()) {
            System.err.println(String.format("Line %d: Invalid line was found.", lineNumber));
            System.err.println(line);
            System.exit(1);
        }

        String label = result.group("label");
        String op = result.group("op");
        List<String> args = new ArrayList<>();
        if (result.group("arg1") != null) {
            args.add(result.group("arg1"));
            if (result.group("arg2") != null) {
                args.add(result.group("arg2"));
            }
            if (result.group("arg3") != null) {
                args.add(result.group("arg3"));
            }
        }
        return new Instruction(label, op, args.toArray(new String[0]), lineNumber, line);
    }

    private void registerLabel(Instruction inst) {
        if (inst.label != null && !inst.label.isEmpty()) {
            String labelName = currentScope + "." + inst.label;
            if (this.symbols.containsKey(labelName)) {
                System.err.println(String.format("Line %d: Label \"%s\" is already defined.", inst.lineNumber, inst.label));
                System.exit(1);
            }
            this.symbols.put(labelName, new Label(labelName, inst.lineNumber, this.file, this.addr));
        }
    }

    private Object[] convR(String[] args) {
        return new Object[]{regStr.get(args[0])};
    }

    private Object[] convR1R2(String[] args) {
        return new Object[]{regStr.get(args[0]), regStr.get(args[1])};
    }

    private Object[] convAdrX(String[] args) {
        Object addr = this.convAdr(args[0]);
        if (args.length == 1) {
            return new Object[]{addr, 0};
        } else {
            return new Object[]{addr, regStr.get(args[1])};
        }
    }

    private Object[] convRAdrX(String[] args) {
        Object addr = this.convAdr(args[1]);
        if (args.length == 2) {
            return new Object[]{regStr.get(args[0]), addr, 0};
        } else {
            return new Object[]{regStr.get(args[0]), addr, regStr.get(args[2])};
        }
    }

    private Object convAdr(String addr) {
        Object a;
        if (Pattern.matches("-?[0-9]+", addr)) {
            a = a2l(Integer.parseInt(addr));
        } else if (Pattern.matches("#[A-Za-z0-9]+", addr)) {
            a = Integer.parseInt(addr.substring(1), 16);
        } else if (Pattern.matches("[A-Za-z_][A-Za-z0-9_]*", addr)) {
            a = this.currentScope + "." + addr;
        } else if (Pattern.matches("=.+", addr)) {
            a = addr;
        } else {
            throw new Error(this.currentLineNumber, this.currentSrc, "Invalid address format.");
        }
        return a;
    }

    private Object[] genCodeNoArg(String op, String[] args) {
        return new Object[]{opTable.get(op).getKey() << 8};
    }

    private Object[] genCodeR(String op, String[] args) {
        return new Object[]{(opTable.get(op).getKey() << 8) | ((int) this.convR(args)[0] << 4)};
    }

    private Object[] genCodeR1R2(String op, String[] args) {
        Object[] r1r2 = this.convR1R2(args);
        int r1 = (int) r1r2[0];
        int r2 = (int) r1r2[1];
        return new Object[]{((opTable.get(op).getKey() << 8) | (r1 << 4) | r2)};
    }

    private Object[] genCodeAdrX(String op, String[] args) {
        Object[] adrx = this.convAdrX(args);
        Object adr = adrx[0];
        int x = (int) adrx[1];
        return new Object[]{((opTable.get(op).getKey() << 8) | x), adr};
    }

    private Object[] genCodeRAdrX(String op, String[] args) {
        Object[] radrx = this.convRAdrX(args);
        int r = (int) radrx[0];
        Object adr = radrx[1];
        int x = (int) radrx[2];
        return new Object[]{((opTable.get(op).getKey() << 8) | (r << 4) | x), adr};
    }

    private Object[] genCodeDs(String op, String[] args) {
        int size = Integer.parseInt(args[0]);
        Object[] code = new Object[size];
        Arrays.fill(code, 0);
        return code;
    }

    private Object[] genCodeDc(String op, String[] args) {
        int[] c = this.castLiteral(args[0]);
        return Arrays.stream(c).mapToObj(e -> e).toArray(Object[]::new);
    }

    private Object[] genCodeStrLen(String op, String[] args) {
        return new Object[]{(opTable.get(op)).getKey() << 8, this.convAdr(args[0]), this.convAdr(args[1])};
    }

    private Object[] genCodeStart(String op, String[] args) {
        Object[] code = new Object[8];
        code[0] = ('C' << 8) + 'A';
        code[1] = ('S' << 8) + 'L';
        if (args.length != 0) {
            Object[] adrx = this.convAdrX(args);
            code[2] = adrx[0];
        } else {
            code[2] = 0;
        }
        code[3] = code[4] = code[5] = code[6] = code[7] = 0;
        return code;
    }

    private int[] castLiteral(String arg) {
        int[] value;
        if (arg.startsWith("#")) {
            value = new int[]{Integer.parseInt(arg.substring(1), 16)};
        } else if (arg.startsWith("'")) {
            value = arg.substring(1, arg.length() - 1).chars().toArray();
        } else {
            value = new int[]{a2l(Integer.parseInt(arg))};
        }
        return value;
    }

    private String genLabel() {
        String l = String.format("_L%04d", labelCount);
        this.labelCount += 1;
        return l;
    }

    private int genAdditionalDC(String x, int n) {
        String l = this.genLabel();
        String labelName = "." + l;
        this.symbols.put(labelName, new Label(labelName, n, this.file, this.addr));
        int[] c = this.castLiteral(x.substring(1));
        Object[] code = Arrays.stream(c).mapToObj(e -> e).toArray();
        this.addr += code.length;
        this.additionalDC.add(new ByteCode(code, this.symbols.get(labelName).addr, n, String.format("%s\tDC\t%s", l, x.substring(1))));
        return this.symbols.get(labelName).addr;
    }

    private ByteCode convert(Instruction inst) {
        this.registerLabel(inst);
        if (inst.op == null) {
            return null;
        }
        if (!opTable.containsKey(inst.op)) {
            System.err.println(String.format("Line %d: Invalid instruction \"%s\" was found.", inst.lineNumber, inst.op));
            System.exit(1);
            throw new RuntimeException();
        }
        if (-100 < opTable.get(inst.op).getKey() && opTable.get(inst.op).getKey() < 0) {
            if (this.isArgRegister(inst.args[1])) {
                inst.op += "1";
            } else {
                inst.op += "2";
            }
        }
        if (opTable.get(inst.op).getKey() == -100) {
            if (inst.label == null) {
                System.err.println(String.format("Line %d: Label should be defined for START.", inst.lineNumber));
                System.exit(1);
            }
            this.currentScope = inst.label;
            if (this.startFound) {
                if (inst.args != null && inst.args.length != 0) {
                    this.symbols.get("." + inst.label).jumpto = (String) this.convAdr(inst.args[0]);
                }
                return null;
            } else {
                this.startFound = true;
                return new ByteCode(this.genCodeStart(inst.op, inst.args), this.addr, inst.lineNumber, inst.src);
            }
        } else if (opTable.get(inst.op).getKey() == -101) {
            this.currentScope = "";
            return null;
        } else if (opTable.get(inst.op).getKey() < 0) {
            return null;
        }

        Object[] code = this.genCodeFunc.get(opTable.get(inst.op).getValue()).apply(inst.op, inst.args);
        ByteCode byteCode = new ByteCode(code, this.addr, inst.lineNumber, inst.src);
        this.addr += byteCode.code.length;
        return byteCode;
    }

    private boolean isArgRegister(String arg) {
        return arg.startsWith("GR");
    }

    private void write(String filename, ByteCode[] codeList) {
        try (FileOutputStream fos = new FileOutputStream(new File(filename));
             DataOutputStream dos = new DataOutputStream(fos)) {
            for (ByteCode code : codeList) {
                for (Object i : code.code) {
                    dos.writeShort((int) i);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String usage = "Usage: " + PyCasl2.class.getSimpleName() + " [options] input.cas [output.com]";
        List<String> argList = new ArrayList<>();
        boolean dump = false;
        boolean version = false;
        boolean help = false;
        for (String arg : args) {
            switch (arg) {
                case "-a":
                    dump = true;
                    break;
                case "-v":
                case "--version":
                    version = true;
                    break;
                case "-h":
                case "--help":
                    help = true;
                    break;
                default:
                    if (arg.startsWith("-")) {
                        System.err.println(usage);
                        System.err.println("no such option: " + arg);
                        System.exit(1);
                    } else {
                        argList.add(arg);
                    }
            }
        }
        if (version) {
            System.out.println("PyCASL2 version 1.1.6");
            System.out.println("$Revision: 42606859abf2 $");
            System.out.println("Copyright (c) 2009,2011, Masahiko Nakamoto.");
            System.out.println("All rights reserved.");
        } else if (argList.size() < 1 || help) {
            System.out.println(usage);
            System.out.println("  -a           turn on verbose listings");
            System.out.println("  -v --version display version and exit");
        } else {
            String comName = argList.size() < 2 ? argList.get(0).replaceAll("\\.[^.]+$", ".com") : argList.get(1);
            PyCasl2 casl2 = new PyCasl2();
            ByteCode[] x = casl2.assemble(new File(argList.get(0)));
            if (dump) {
                casl2.dump(x);
            }
            casl2.write(comName, x);
        }
    }
}
