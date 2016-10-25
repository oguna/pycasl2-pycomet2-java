package pycasl2;

class ByteCode {
    final Object[] code;
    final int addr;
    final int lineNumber;
    final String src;

    ByteCode(Object[] code, int addr, int lineNumber, String src) {
        this.code = code;
        this.addr = addr;
        this.lineNumber = lineNumber;
        this.src = src;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.code.length > 0) {
            assert this.code[0] instanceof Integer;
            int op = (int)this.code[0];
            String s = String.format("%04x\t%04x\t\t%d\t%s", this.addr, op, this.lineNumber, this.src);
            sb.append(s);
        } else {
            String s = String.format("%04x\t    \t\t%d\t%s", this.addr, this.lineNumber, this.src);
            sb.append(s);
        }
        if (1 < this.code.length) {
            sb.append("\n");
            assert this.code[1] instanceof Integer || this.code[1] instanceof String;
            if (this.code[1] instanceof Integer) {
                int arg = (int)this.code[1];
                sb.append(String.format("%04x\t%04x", this.addr + 1, arg));
            } else {
                sb.append(String.format("%04x\t%s", this.addr + 1, this.code[1]));
            }
        }
        if (2 < this.code.length) {
            sb.append("\n");
            assert this.code[2] instanceof Integer || this.code[2] instanceof String;
            if (this.code[2] instanceof Integer) {
                int arg = (int)this.code[2];
                sb.append(String.format("%04x\t%04x", this.addr + 2, arg));
            } else {
                sb.append(String.format("%04x\t%s", this.addr + 2, this.code[2]));
            }
        }
        return sb.toString();
    }
}
