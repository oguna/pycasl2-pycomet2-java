package pycasl2;

import java.io.File;

class Label {
    private final String label;
    final int lines;
    private final File file;
    final int addr;
    String jumpto;

    private Label(String label, int lines, File file, int addr, String jumpto) {
        this.label = label;
        this.lines = lines;
        this.file = file;
        this.addr = addr;
        this.jumpto = jumpto;
    }

    Label(String label, int lines, File file, int addr) {
        this(label, lines, file, addr, "");
    }

    @Override
    public String toString() {
        String[] sl = this.label.split("\\.");
        String scope = sl[0];
        String label = sl[1];
        if (scope.length() == 0) {
            return String.format("%s:%d\t%04x\t%s", file, lines, addr, label);
        } else {
            return String.format("%s:%d\t%04x\t%s (%s)", file, lines, addr, label, scope);
        }
    }
}
