package pycomet2;

class POP extends Instruction {
    POP(PyComet2 machine) {
        super(machine, 0x71, "POP", ArgType.R);
    }

    @Override
    public void execute() {
        int r = getR();
        this.m.GR[r] = this.m.memory[this.m.getSP()];
        this.m.setSP(this.m.getSP() + 1);
        this.m.PR += 1;
    }
}
