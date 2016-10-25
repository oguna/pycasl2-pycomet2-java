package pycomet2;

class SLA extends Instruction {
    SLA(PyComet2 machine) {
        super(machine, 0x50, "SLA", ArgType.RAdrX);
    }

    @Override
    public void execute() {
        int[] radrx = getRAdRx();
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        int v = this.getEffectiveAddress(adr, x);
        int p = l2a(this.m.GR[r]);
        int sign = getBit(this.m.GR[r], 15);
        int ans = (p << v) & 0x7fff;
        if (sign == 0) {
            ans = ans & 0x7fff;
        } else {
            ans = ans | 0x8000;
        }
        this.m.GR[r] = ans;
        this.updateFlags(this.m.GR[r]);
        if (0 < v) {
            this.m.OF = getBit(p, 15 - v);
        }
        this.m.PR += 2;
    }
}
