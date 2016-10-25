package pycomet2;

class ADDA2 extends Instruction {

    ADDA2(PyComet2 machine) {
        super(machine, 0x20, "ADDA", ArgType.RAdrX);
    }

    @Override
    public void execute() {
        int[] radrx = getRAdRx();
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        int v = this.getValueAtEffectiveAddress(adr, x);
        int result = l2a(this.m.GR[r]) + l2a(v);
        m.GR[r] = a2l(result);
        this.updateFlags(result);
        m.PR += 2;
    }
}
