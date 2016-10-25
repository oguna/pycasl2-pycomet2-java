package pycomet2;

abstract class Instruction implements Util {
    final PyComet2 m;
    final int opcode;
    private final String opname;
    public final ArgType argtype;

    public Instruction(PyComet2 machine, int opcode, String opname, ArgType argtype) {
        this.m = machine;
        this.opcode = opcode;
        this.opname = opname;
        this.argtype = argtype;
    }

    int getR() {
        return getR(m.PR);
    }

    int getR(int addr) {
        int a = m.memory[addr];
        return (0x00f0 & a) >> 4;
    }

    int[] getR1R2() {
        return getR1R2(m.PR);
    }

    int[] getR1R2(int addr) {
        int a = m.memory[addr];
        int r1 = ((0x00f0 & a) >> 4);
        int r2 = 0x000f & a;
        return new int[]{r1, r2};
    }

    public int[] getAdRx() {
        return getAdRx(m.PR);
    }

    public int[] getAdRx(int addr) {
        int a = m.memory[addr];
        int b = m.memory[addr + 1];
        int x = 0x000f & a;
        int adr = b;
        return new int[]{adr, x};
    }

    public int[] getRAdRx() {
        return getRAdRx(m.PR);
    }

    public int[] getRAdRx(int addr) {
        int a = m.memory[addr];
        int b = m.memory[addr + 1];
        int r = (0x00f0 & a) >> 4;
        int x = (0x000f & a);
        int adr = b;
        return new int[]{r, adr, x};
    }

    public int[] getStrLen() {
        return getStrLen(m.PR);
    }

    public int[] getStrLen(int addr) {
        int s = m.memory[addr + 1];
        int l = m.memory[addr + 2];
        return new int[]{s, l};
    }

    void updateFlags(int result) {
        updateFlags(result, false);
    }

    void updateFlags(int result, boolean isLogical) {
        m.ZF = (result == 0) ? 1 : 0;
        m.SF = (getBit(result, 15) == 0) ? 0 : 1;
        if (isLogical) {
            m.OF = (result < 0 || 0xffff < result) ? 1 : 0;
        } else {
            m.OF = (result < -32768 || 0x7fff < result) ? 1 : 0;
        }
    }

    int getEffectiveAddress(int adr, int x) {
        return x == 0 ? adr : a2l(adr + m.GR[x]);
    }

    int getValueAtEffectiveAddress(int adr, int x) {
        return x == 0 ? m.memory[adr] : m.memory[a2l(adr + m.GR[x])];
    }


    String disassemble(int address) {
        switch (this.argtype) {
            case NoArg:
                return this.disassembleNoArg(address);
            case R:
                return this.disassembleR(address);
            case R1R2:
                return this.disassembleR1R2(address);
            case AdrX:
                return this.disassembleAdrX(address);
            case RAdrX:
                return this.disassembleRAdrX(address);
            case StrLen:
                return this.disassembleStrLen(address);
            default:
                throw new RuntimeException();
        }
    }

    private String disassembleNoArg(int address) {
        return String.format("%-8s", this.opname);
    }

    private String disassembleR(int address) {
        return String.format("%-8sGR%1d", this.opname, getR(address));
    }

    private String disassembleR1R2(int address) {
        int[] r1r2 = getR1R2(address);
        int r1 = r1r2[0];
        int r2 = r1r2[1];
        return String.format("%-8sGR%1d, GR%1d", opname, r1, r2);
    }

    private String disassembleAdrX(int address) {
        int[] adrx = getAdRx(address);
        int adr = adrx[0];
        int x = adrx[1];
        return x == 0 ? String.format("%-8s#%04x", opname, adr) : String.format("%-8s#%04x, GR%1d", opname, adr, x);
    }

    private String disassembleRAdrX(int address) {
        int[] radrx = getRAdRx(address);
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        return x == 0 ? String.format("%-8sGR%1d, #%04x", opname, r, adr) : String.format("%-8sGR%1d, #%04x, GR%1d", opname, r, adr, x);
    }

    private String disassembleStrLen(int address) {
        int[] sl = getStrLen(address);
        int s = sl[0];
        int l = sl[1];
        return String.format("%-8s#%04x, #%04x", opname, s, l);
    }

    public abstract void execute();
}
