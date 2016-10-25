package pycomet2;

class JOV extends Instruction {
    JOV(PyComet2 machine) {
        super(machine, 0x66, "JOV", ArgType.AdrX);
    }

    @Override
    public void execute() {
        int[] adrx = getAdRx();
        int adr = adrx[0];
        int x = adrx[1];
        if (this.m.OF == 1) {
            this.m.PR = this.getEffectiveAddress(adr, x);
        } else {
            this.m.PR += 2;
        }
    }
}
