package pycomet2;

class PUSH extends Instruction {
    PUSH(PyComet2 machine) {
        super(machine, 0x70, "PUSH", ArgType.AdrX);
    }

    @Override
    public void execute() {
        int[] adrx = getAdRx();
        int adr = adrx[0];
        int x = adrx[1];
        this.m.setSP(this.m.getSP() - 1);
        this.m.memory[this.m.getSP()] = this.getEffectiveAddress(adr, x);
        this.m.PR += 2;
    }
}
