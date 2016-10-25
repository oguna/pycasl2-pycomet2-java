package pycomet2;

class ADDL1 extends Instruction {

    ADDL1(PyComet2 machine) {
        super(machine, 0x26, "ADDL", ArgType.R1R2);
    }

    @Override
    public void execute() {
        int[] r1r2 = getR1R2();
        int r1 = r1r2[0];
        int r2 = r1r2[1];
        int result = this.m.GR[r1] + this.m.GR[r2];
        this.m.GR[r1] = result & 0xffff;
        this.updateFlags(result, true);
        m.PR += 1;
    }
}
