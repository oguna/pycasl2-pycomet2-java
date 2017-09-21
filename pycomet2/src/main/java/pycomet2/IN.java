package pycomet2;

import java.io.IOException;
import java.io.UncheckedIOException;

class IN extends Instruction {
    IN(PyComet2 machine) {
        super(machine, 0x90, "IN", ArgType.StrLen);
    }

    @Override
    public void execute() {
        int[] sl = this.getStrLen();
        int s = sl[0];
        int l = sl[1];
        if (this.m.showInputArrow) {
            System.err.print("->");
            System.err.flush();
        }
        String line;
        try {
            line = this.m.in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (256 < line.length()) {
            line = line.substring(0, 256);
        }
        this.m.memory[l] = line.length();
        for (int i = 0; i < line.length(); i++) {
            this.m.memory[s + i] = line.codePointAt(i);
        }
        this.m.PR += 3;
    }
}
