package pycomet2;

class CALL extends Instruction {
    CALL(PyComet2 machine) {
        super(machine, 0x80, "CALL", ArgType.AdrX);
    }

    @Override
    public void execute() {
        int[] adrx = getAdRx();
        int adr = adrx[0];
        int x = adrx[1];
        this.m.setSP(this.m.getSP() - 1);
        this.m.memory[this.m.getSP()] = this.m.PR;
        this.m.PR = this.getEffectiveAddress(adr, x);
        this.m.callLevel += 1;
    }
}
