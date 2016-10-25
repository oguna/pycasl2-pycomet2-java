package pycomet2;

class CPL2 extends Instruction {
    CPL2(PyComet2 machine) {
        super(machine, 0x41, "CPL", ArgType.RAdrX);
    }

    @Override
    public void execute() {
        int[] radrx = getRAdRx();
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        int v = this.getValueAtEffectiveAddress(adr, x);
        int diff = this.m.GR[r] - v;
        this.m.SF = diff >= 0 ? 0 : 1;
        this.m.ZF = diff == 0 ? 1 : 0;
        this.m.OF = 0;
        this.m.PR += 2;
    }
}
