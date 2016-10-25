package pycomet2;

class CPA2 extends Instruction {
    CPA2(PyComet2 machine) {
        super(machine, 0x40, "CPA", ArgType.RAdrX);
    }

    @Override
    public void execute() {
        int[] radrx = getRAdRx();
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        int v = this.getValueAtEffectiveAddress(adr, x);
        int diff = l2a(this.m.GR[r]) - l2a(v);
        this.m.SF = diff >= 0 ? 0 : 1;
        this.m.ZF = diff == 0 ? 1 : 0;
        this.m.OF = 0;
        this.m.PR += 2;
    }
}
