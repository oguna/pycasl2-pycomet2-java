package pycasl2;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

class Instruction {
    final String label;
    String op;
    final String[] args;
    final int lineNumber;
    final String src;

    Instruction(String label, String op, String[] args, int lineNumber, String src) {
        this.label = label;
        this.op = op;
        this.args = args;
        this.lineNumber = lineNumber;
        this.src = src;
    }

    @Override
    public String toString() {
        String argsStr = Arrays.stream(this.args).map(Objects::toString).collect(Collectors.joining(","));
        return String.format("%d: %s, %s, [%s]", this.lineNumber, this.label, this.op, argsStr);
    }
}
