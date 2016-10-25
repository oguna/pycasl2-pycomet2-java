package pycomet2;

class OUT extends Instruction {
    OUT(PyComet2 machine) {
        super(machine, 0x91, "OUT", ArgType.StrLen);
    }

    @Override
    public void execute() {
        int[] sl = this.getStrLen();
        int s = sl[0];
        int l = sl[1];
        int length = this.m.memory[l];
        StringBuilder sb = new StringBuilder();
        for (int i = s; i < s + length; i++) {
            if (Character.isValidCodePoint(this.m.memory[i])) {
                sb.append((char)this.m.memory[i]);
            } else {
                System.err.println("Error:");
                this.m.printStatus();
                this.m.dump(s);
            }
        }
        System.out.println(sb.toString());
        this.m.PR += 3;
    }
}
