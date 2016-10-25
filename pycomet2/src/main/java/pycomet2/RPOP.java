package pycomet2;

class RPOP extends Instruction {
    RPOP(PyComet2 machine) {
        super(machine, 0xa1, "RPOP", ArgType.NoArg);
    }

    @Override
    public void execute() {
        for (int i = 8; i > 0; i--) {
            this.m.GR[i] = this.m.memory[this.m.getSP()];
            this.m.setSP(this.m.getSP() + 1);
        }
        this.m.PR += 1;
    }
}
