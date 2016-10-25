package pycomet2;

class SVC extends Instruction {
    SVC(PyComet2 machine) {
        super(machine, 0xf0, "SVC", ArgType.AdrX);
    }

    @Override
    public void execute() {
    }
}
