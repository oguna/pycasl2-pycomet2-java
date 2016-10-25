package pycomet2;

class ADDL2 extends Instruction {

    ADDL2(PyComet2 machine) {
        super(machine, 0x22, "ADDL", ArgType.RAdrX);
    }

    @Override
    public void execute() {
        int[] radrx = getRAdRx();
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        int v = this.getValueAtEffectiveAddress(adr, x);
        int result = this.m.GR[r] + v;
        this.m.GR[r] = result & 0xffff;
        this.updateFlags(result, true);
        m.PR += 2;
    }
}
