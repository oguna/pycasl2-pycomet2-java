package pycomet2;

class XOR1 extends Instruction {

    XOR1(PyComet2 machine) {
        super(machine, 0x36, "XOR", ArgType.R1R2);
    }

    @Override
    public void execute() {
        int[] r1r2 = getR1R2();
        int r1 = r1r2[0];
        int r2 = r1r2[1];
        this.m.GR[r1] = this.m.GR[r1] ^ this.m.GR[r2];
        this.updateFlags(this.m.GR[r1]);
        this.m.OF = 0;
        m.PR += 1;
    }
}
