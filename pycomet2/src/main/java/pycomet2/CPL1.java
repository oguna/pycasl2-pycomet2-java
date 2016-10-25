package pycomet2;

class CPL1 extends Instruction {

    CPL1(PyComet2 machine) {
        super(machine, 0x45, "CPL", ArgType.R1R2);
    }

    @Override
    public void execute() {
        int[] r1r2 = getR1R2();
        int r1 = r1r2[0];
        int r2 = r1r2[1];
        int diff = this.m.GR[r1] - this.m.GR[r2];
        this.m.SF = diff >= 0 ? 0 : 1;
        this.m.ZF = diff == 0 ? 1 : 0;
        this.m.OF = 0;
        this.m.PR += 1;
    }
}
