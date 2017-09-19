package pycomet2;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class PyComet2 implements Util {
    static class InvalidOperation extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final int address;

        public InvalidOperation(int address) {
            super(String.format("Invalid operation is found at #%04x", address));
            this.address = address;
        }
    }

    private static class StatusMonitor {
        private final PyComet2 m;
        private final List<Supplier<String>> varList;
        public boolean decimalFlag = false;

        public StatusMonitor(PyComet2 machine) {
            this.m = machine;
            this.varList = new ArrayList<>();
        }

        @Override
        public String toString() {
            return String.format("%04d: ", m.stepCount) + this.varList.stream().map(Supplier::get).collect(Collectors.joining(", "));
        }

        public void append(String s) {
            try {
                if (s.equals("PR")) {
                    this.varList.add(() -> String.format("PR=#%04x", m.PR));
                } else if (s.equals("OF")) {
                    this.varList.add(() -> String.format("OF=%01d", m.OF));
                } else if (s.equals("SF")) {
                    this.varList.add(() -> String.format("SF=%01d", m.SF));
                } else if (s.equals("ZF")) {
                    this.varList.add(() -> String.format("ZF=%01d", m.ZF));
                } else if (s.startsWith("GR")) {
                    if (s.charAt(2) < '0' || '8' < s.charAt(2)) {
                        throw new RuntimeException();
                    }
                    int reg = Character.digit(s.charAt(2), 10);
                    if (this.decimalFlag) {
                        this.varList.add(() -> String.format("GR%d=%d", reg, m.GR[reg]));
                    } else {
                        this.varList.add(() -> String.format("GR%s=#%04x", reg, m.GR[reg]));
                    }
                } else {
                    int adr = this.m.castInt(s);
                    if (adr < 0 || 0xffff < adr) {
                        throw new RuntimeException();
                    }
                    if (this.decimalFlag) {
                        this.varList.add(() -> String.format("#%04x=%d", adr, m.memory[adr]));
                    } else {
                        this.varList.add(() -> String.format("#%04x=#%04x", adr, m.memory[adr]));
                    }
                }
            } catch (Exception e) {
                System.err.println(String.format("Warning: Invalid monitor target is found. %s is ignored.", s));
            }
        }
    }

    private final Map<Integer, Instruction> instTable;
    private final List<Integer> breakPoints;
    public int callLevel = 0;
    public int stepCount = 0;
    private StatusMonitor monitor;

    private final int initSP = 0xff00;
    public int[] memory;
    public int[] GR;
    public int PR;
    public int OF;
    public int SF;
    public int ZF;
    private boolean isCountStep;
    private boolean isAutoDump;
    private boolean exiting = false;
    public Scanner scanner = new Scanner(System.in);

    private PyComet2() {
        List<Instruction> instList = Arrays.asList(
                new NOP(this), new LD2(this), new ST(this), new LAD(this), new LD1(this),
                new ADDA2(this), new SUBA2(this), new ADDL2(this), new SUBL2(this),
                new ADDA1(this), new SUBA1(this), new ADDL1(this), new SUBL1(this),
                new AND2(this), new OR2(this), new XOR2(this), new AND1(this), new OR1(this), new XOR1(this),
                new CPA2(this), new CPL2(this), new CPA1(this), new CPL1(this),
                new SLA(this), new SRA(this), new SLL(this), new SRL(this),
                new JMI(this), new JNZ(this), new JZE(this), new JUMP(this), new JPL(this), new JOV(this),
                new PUSH(this), new POP(this), new CALL(this), new RET(this), new SVC(this),
                new IN(this), new OUT(this), new RPUSH(this), new RPOP(this));
        this.instTable = new HashMap<>();
        for (Instruction i : instList) {
            this.instTable.put(i.opcode, i);
        }
        this.monitor = new StatusMonitor(this);
        this.breakPoints = new ArrayList<>();
    }

    private void initialize() {
        this.memory = new int[65536];
        this.GR = new int[9];
        this.setSP(initSP);
        this.PR = 0;
        this.OF = 0;
        this.SF = 0;
        this.ZF = 1;
    }

    protected void setSP(int v) {
        this.GR[8] = v;
    }

    protected int getSP() {
        return this.GR[8];
    }

    public void printStatus() {
        String code;
        try {
            code = this.getInstruction().disassemble(this.PR);
        } catch (Exception e) {
            code = String.format("%04x", this.memory[this.PR]);
        }
        System.err.println(String.format("PR  #%04x [ %-30s ]  STEP %d",
                this.PR, code, this.stepCount));
        System.err.println(String.format("SP  #%04x(%7d) FR(OF, SF, ZF)  %3s  (%7d)",
                this.getSP(), this.getSP(), this.getFRasString(), this.getFR()));
        System.err.println(String.format("GR0 #%04x(%7d) GR1 #%04x(%7d) GR2 #%04x(%7d) GR3: #%04x(%7d)",
                this.GR[0], l2a(this.GR[0]), this.GR[1], l2a(this.GR[1]),
                this.GR[2], l2a(this.GR[2]), this.GR[3], l2a(this.GR[3])));
        System.err.println(String.format("GR4 #%04x(%7d) GR5 #%04x(%7d) GR6 #%04x(%7d) GR7: #%04x(%7d)",
                this.GR[4], l2a(this.GR[4]), this.GR[5], l2a(this.GR[5]),
                this.GR[6], l2a(this.GR[6]), this.GR[7], l2a(this.GR[7])));
    }

    void exit() {
        if (this.isCountStep) {
            System.out.println("Step count: " + this.stepCount);
        }
        if (this.isAutoDump) {
            System.err.println("dump last status to last_state.txt");
            this.dumpToFile(new File("last_state.txt"));
        }
        this.exiting = true;
    }

    private void setAutoDump(boolean flag) {
        this.isAutoDump = flag;
    }

    private void setCountStep(boolean flag) {
        this.isCountStep = flag;
    }

    private int getFR() {
        return this.OF << 2 | this.SF << 1 | this.ZF;
    }

    private String getFRasString() {
        return Integer.toString(this.OF) + SF + ZF;
    }

    private Instruction getInstruction() {
        return getInstruction(this.PR);
    }

    private Instruction getInstruction(int adr) {
        if (!instTable.containsKey((this.memory[adr] & 0xff00) >> 8)) {
            throw new InvalidOperation(adr);
        }
        return instTable.get((this.memory[adr] & 0xff00) >> 8);
    }

    private void step() {
        this.getInstruction().execute();
        this.stepCount += 1;
    }

    private void watch(String variables, boolean decimalFlag) {
        this.monitor.decimalFlag = decimalFlag;
        for (String v : variables.split(",")) {
            this.monitor.append(v);
        }
        while (!this.exiting) {
            if (this.breakPoints.contains(this.PR)) {
                break;
            } else {
                try {
                    System.out.println(this.monitor);
                    System.out.flush();
                    this.step();
                } catch (InvalidOperation e) {
                    System.err.println(e.toString());
                    this.dump(e.address);
                    break;
                }
            }
        }
    }

    private void run() {
        while (!this.exiting) {
            if (this.breakPoints.contains(this.PR)) {
                break;
            } else {
                try {
                    this.step();
                } catch (InvalidOperation e) {
                    System.err.println(e.toString());
                    this.dump(e.address);
                    break;
                }
            }
        }
    }

    private void load(String filename) throws IOException {
        load(filename, false);
    }

    private void load(String filename, boolean quiet) throws IOException {
        if (!quiet) {
            System.err.print(String.format("load %s ...", filename));
            System.err.flush();
        }
        this.initialize();
        File file = new File(filename);
        long size = Files.size(file.toPath());
        if ((size & 1) == 1) {
            throw new RuntimeException();
        }
        int[] tmp = new int[(int)(size / 2)];
        try (InputStream is = new FileInputStream(new File(filename));
             DataInputStream dis = new DataInputStream(is)) {
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = dis.readUnsignedShort();
            }
        }
        this.PR = tmp[2];
        tmp = Arrays.copyOfRange(tmp, 8, tmp.length);
        System.arraycopy(tmp, 0, this.memory, 0, tmp.length);
        if (!quiet) {
            System.err.println("done.");
        }
    }

    private String dumpMemory(int startAddr, int lines) {
        IntFunction<Character> char2 = i -> {
            int c = 0x00ff & i;
            if (32 <= c && c <= 126) {
                return (char) c;
            } else {
                return '.';
            }
        };
        Function<int[], String> toChar = array -> Arrays.stream(array)
                .mapToObj(e -> Character.toString(char2.apply(e)))
                .collect(Collectors.joining());
        Function<int[], String> toHex = array -> Arrays.stream(array)
                .mapToObj(e -> String.format("%04x", e))
                .collect(Collectors.joining(" "));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            int addr = i * 8 + startAddr;
            if (0xffff < addr) {
                break;
            }
            sb.append(String.format("%04x: %-39s %-8s%n",
                    addr,
                    toHex.apply(Arrays.copyOfRange(this.memory, addr, addr + 8)),
                    toChar.apply(Arrays.copyOfRange(this.memory, addr, addr + 8))));
        }
        return sb.toString();
    }

    private void dump() {
        dump(0x0000);
    }

    void dump(int startAddr) {
        System.out.print(this.dumpMemory(startAddr, 16));
    }

    private void dumpStack() {
        System.out.println(this.dumpMemory(this.getSP(), 16));
    }

    private void dumpToFile(File file) {
        dumpToFile(file, 0xffff / 8);
    }

    private void dumpToFile(File file, int lines) {
        try (Writer writer = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(String.format("Step count: %d%n", this.stepCount));
            bw.write(String.format("PR: #%04x%n", this.PR));
            bw.write(String.format("SP: #%04x%n", this.getSP()));
            bw.write(String.format("OF: #%1d%n", this.OF));
            bw.write(String.format("SF: #%1d%n", this.SF));
            bw.write(String.format("ZF: #%1d%n", this.ZF));
            for (int i = 0; i < 8; i++) {
                bw.write(String.format("GR%d: #%04x%n", i, this.GR[i]));
            }
            bw.write(String.format("Memory:%n"));
            bw.write(this.dumpMemory(0, lines));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void disassemble() {
        disassemble(0x0000);
    }

    private void disassemble(int startAddr) {
        Map<ArgType, Integer> instSize = new HashMap<>();
        instSize.put(ArgType.NoArg, 1);
        instSize.put(ArgType.R, 1);
        instSize.put(ArgType.R1R2, 1);
        instSize.put(ArgType.AdrX, 2);
        instSize.put(ArgType.RAdrX, 2);
        instSize.put(ArgType.Ds, -1);
        instSize.put(ArgType.Dc, -1);
        instSize.put(ArgType.StrLen, 3);
        int addr = startAddr;
        for (int i = 0; i < 16; i++) {
            try {
                Instruction inst = this.getInstruction(addr);
                if (inst != null) {
                    System.out.println(String.format("#%04x\t#%04x\t%s", addr, this.memory[addr],
                            inst.disassemble(addr)));
                    if (1 < instSize.get(inst.argtype)) {
                        System.err.println(String.format("#%04x\t#%04x", addr + 1, this.memory[addr + 1]));
                    } else if (2 < instSize.get(inst.argtype)) {
                        System.err.println(String.format("#%04x\t#%04x", addr + 2, this.memory[addr + 2]));
                    }
                    addr += instSize.get(inst.argtype);
                } else {
                    System.err.println(String.format("#%04x\t#%04x\t%s", addr, this.memory[addr],
                            String.format("%-8s#%04x", "DC", this.memory[addr])));
                    addr += 1;
                }
            } catch (Exception e) {
                System.err.println(String.format("#%04x\t#%04x\t%s",
                        addr, this.memory[addr], String.format("%-8s#%04x", "DC", this.memory[addr])));
            }
        }
    }

    private int castInt(String addr) {
        if (addr.startsWith("#")) {
            return Integer.parseInt(addr.substring(1), 16);
        } else {
            return Integer.parseInt(addr);
        }
    }

    private void setBreakPoint(int addr) {
        if (this.breakPoints.contains(addr)) {
            System.err.println(String.format("#%04x is already set.", addr));
        } else {
            this.breakPoints.add(addr);
        }
    }

    private void printBreakPoints() {
        if (this.breakPoints.size() == 0) {
            System.err.println("No break points.");
        } else {
            for (int i = 0; i < this.breakPoints.size(); i++) {
                int addr = this.breakPoints.get(i);
                System.err.println(String.format("%d: #%04x", i, addr));
            }
        }
    }

    private void deleteBreakPoints(int n) {
        if (0 <= n && n < this.breakPoints.size()) {
            this.breakPoints.remove(n);
            System.err.println(String.format("#%04x is removed.", this.breakPoints.get(n)));
        } else {
            System.err.println("Invalid number is specified.");
        }
    }

    private void writeMemory(int addr, int value) {
        this.memory[addr] = value;
    }

    private void jump(int addr) {
        this.PR = addr;
        this.printStatus();
    }

    private void waitForCommand() {
        while (!this.exiting) {
            System.err.print("pycomet2> ");
            System.err.flush();
            String line = scanner.nextLine();
            String[] args = line.split("\\s");
            if (line.startsWith("q")) {
                break;
            } else if (line.startsWith("b")) {
                if (2 <= args.length) {
                    this.setBreakPoint(this.castInt(args[1]));
                }
            } else if (line.startsWith("df")) {
                this.dumpToFile(new File(args[1]));
            } else if (line.startsWith("di")) {
                if (args.length == 1) {
                    this.disassemble();
                } else {
                    this.disassemble(this.castInt(args[1]));
                }
            } else if (line.startsWith("du")) {
                if (args.length == 1) {
                    this.dump();
                } else {
                    this.dump(this.castInt(args[1]));
                }
            } else if (line.startsWith("d")) {
                if (2 <= args.length) {
                    this.deleteBreakPoints(Integer.valueOf(args[1]));
                }
            } else if (line.startsWith("h")) {
                this.printHelp();
            } else if (line.startsWith("i")) {
                this.printBreakPoints();
            } else if (line.startsWith("j")) {
                this.jump(this.castInt(args[1]));
            } else if (line.startsWith("m")) {
                this.writeMemory(this.castInt(args[1]), this.castInt(args[2]));
            } else if (line.startsWith("p")) {
                this.printStatus();
            } else if (line.startsWith("r")) {
                this.run();
            } else if (line.startsWith("st")) {
                this.dumpStack();
            } else if (line.startsWith("s")) {
                try {
                    this.step();
                } catch (InvalidOperation e) {
                    System.err.println(e.toString());
                    this.dump(e.address);
                }
                this.printStatus();
            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    private void printHelp() {
        System.err.println("b ADDR        Set a breakpoint at specified address.");
        System.err.println("d NUM         Delete breakpoints.");
        System.err.println("di ADDR       Disassemble 32 words from specified address.");
        System.err.println("du ADDR       Dump 128 words of memory.");
        System.err.println("h             Print help.");
        System.err.println("i             Print breakpoints.");
        System.err.println("j ADDR        Set PR to ADDR.");
        System.err.println("m ADDR VAL    Change the memory at ADDR to VAL.");
        System.err.println("p             Print register status.");
        System.err.println("q             Quit.");
        System.err.println("r             Strat execution of program.");
        System.err.println("s             Step execution.");
        System.err.println("st            Dump 128 words of stack image.");
    }

    public static void main(String[] args) {
        boolean countStep = false;
        boolean dump = false;
        boolean run = false;
        String watchVariables = null;
        boolean decimalFlag = false;
        boolean version = false;
        boolean help = false;
        List<String> argList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-c":
                case "--count-step":
                    countStep = true;
                    break;
                case "-d":
                case "--dump":
                    dump = true;
                    break;
                case "-r":
                case "--run":
                    run = true;
                    break;
                case "-w":
                    i++;
                    if (i < args.length) {
                        watchVariables = args[i];
                    } else {
                        System.err.println("error: -w option requires 1 argument");
                        System.exit(1);
                    }
                    break;
                case "--watch":
                    System.err.println("error: -w option requires 1 argument");
                    System.exit(1);
                    break;
                case "-D":
                case "--Decimal":
                    decimalFlag = true;
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
                    if (arg.startsWith("--watch=")) {
                        watchVariables = arg.substring(8);
                    }
                    if (arg.startsWith("-")) {
                        System.err.println(arg + "error: no such option: " + arg);
                        System.exit(1);
                    } else {
                        argList.add(arg);
                    }
            }
        }

        if (version) {
            System.out.println("PyCOMET2 version 1.2.1");
            System.out.println("$Revision: a31dbeeb4d1c $");
            System.out.println("Copyright (c) 2009, Masahiko Nakamoto.");
            System.out.println("All rights reserved.");
        } else if (argList.size() < 1 || help) {
            String options = "Options:\n" +
                    "  -h, --help            show this help message and exit\n" +
                    "  -c, --count-step      count step.\n" +
                    "  -d, --dump            dump last status to last_state.txt.\n" +
                    "  -r, --run             run\n" +
                    "  -w WATCHVARIABLES, --watch=WATCHVARIABLES\n" +
                    "                        run in watching mode. (ex. -w PR,GR0,GR8,#001f)\n" +
                    "  -D, --Decimal         watch GR[0-8] and specified address in decimal\n" +
                    "                        notation. (Effective in watcing mode only)\n" +
                    "  -v, --version         display version information.";
            System.out.println("Usage: " + PyComet2.class.getSimpleName() + " [options] input.com");
            System.out.println(options);
        } else {
            PyComet2 comet2 = new PyComet2();
            comet2.setAutoDump(dump);
            comet2.setCountStep(countStep);
            try {
                if (watchVariables != null) {
                    comet2.load(argList.get(0), true);
                    comet2.watch(watchVariables, decimalFlag);
                } else if (run) {
                    comet2.load(argList.get(0), true);
                    comet2.run();
                } else {
                    comet2.load(argList.get(0));
                    comet2.printStatus();
                    comet2.waitForCommand();
                }
            } catch (IOException e) {
                System.err.println("An I/O error occurred while reading comet file: " + argList.get(0));
                e.printStackTrace();
            }
        }
    }
}
