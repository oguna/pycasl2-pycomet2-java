package pycomet2;

class JMI extends Instruction {
    JMI(PyComet2 machine) {
        super(machine, 0x61, "JMI", ArgType.AdrX);
    }

    @Override
    public void execute() {
        int[] adrx = getAdRx();
        int adr = adrx[0];
        int x = adrx[1];
        if (this.m.SF == 1) {
            this.m.PR = this.getEffectiveAddress(adr, x);
        } else {
            this.m.PR += 2;
        }
    }
}
