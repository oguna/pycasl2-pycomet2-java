package pycomet2;

class SUBL1 extends Instruction {

    SUBL1(PyComet2 machine) {
        super(machine, 0x27, "SUBL", ArgType.R1R2);
    }

    @Override
    public void execute() {
        int[] r1r2 = getR1R2();
        int r1 = r1r2[0];
        int r2 = r1r2[1];
        int result = this.m.GR[r1] - this.m.GR[r2];
        this.m.GR[r1] = result & 0xffff;
        this.updateFlags(result, true);
        this.m.PR += 1;
    }
}
