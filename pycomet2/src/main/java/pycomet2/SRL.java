package pycomet2;

class SRL extends Instruction {
    SRL(PyComet2 machine) {
        super(machine, 0x53, "SRL", ArgType.RAdrX);
    }

    @Override
    public void execute() {
        int[] radrx = getRAdRx();
        int r = radrx[0];
        int adr = radrx[1];
        int x = radrx[2];
        int v = this.getEffectiveAddress(adr, x);
        int p = this.m.GR[r];
        int ans = (p >> v) & 0xffff;
        this.m.GR[r] = ans;
        this.updateFlags(this.m.GR[r]);
        if (0 < v) {
            this.m.OF = getBit(p, v - 1);
        }
        this.m.PR += 2;
    }
}
