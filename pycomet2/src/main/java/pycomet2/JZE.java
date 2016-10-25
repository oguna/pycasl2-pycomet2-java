package pycomet2;

class JZE extends Instruction {
    JZE(PyComet2 machine) {
        super(machine, 0x63, "JZE", ArgType.AdrX);
    }

    @Override
    public void execute() {
        int[] adrx = getAdRx();
        int adr = adrx[0];
        int x = adrx[1];
        if (this.m.ZF == 1) {
            this.m.PR = this.getEffectiveAddress(adr, x);
        } else {
            this.m.PR += 2;
        }
    }
}
