package pycomet2;

interface Util {
    default int l2a(int x) {
        x &= 0xffff;
        if (0 <= x && x <= 0x7fff) {
            return x;
        } else if (0x8000 <= x && x <= 0xffff) {
            return x - (1 << 16);
        } else {
            throw new IllegalArgumentException();
        }
    }

    default int a2l(int x) {
        x &= 0xffff;
        if (0 <= x) {
            return x;
        } else {
            return x + (1 << 16);
        }
    }

    default int getBit(int x, int n) {
        return ((x & (1 << n)) == 0) ? 0 : 1;
    }
}
