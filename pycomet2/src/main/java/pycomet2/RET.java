package pycomet2;

class RET extends Instruction {
    RET(PyComet2 machine) {
        super(machine, 0x81, "RET", ArgType.NoArg);
    }

    @Override
    public void execute() {
        if (this.m.callLevel == 0) {
            this.m.stepCount += 1;
            this.m.exit();
        } else {
            this.m.PR = this.m.memory[this.m.getSP()] + 2;
            this.m.setSP(this.m.getSP() + 1);
            this.m.callLevel -= 1;
        }
    }
}
