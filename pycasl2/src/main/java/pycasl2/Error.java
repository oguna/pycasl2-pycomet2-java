package pycasl2;

class Error extends RuntimeException {
    private final int lineNum;
    private final String src;

    Error(int lineNum, String src, String message) {
        super(message);
        this.lineNum = lineNum;
        this.src = src;
    }

    void report() {
        System.err.println(String.format("Error: %s%nLine %d: %s", this.getMessage(), this.lineNum, this.src));
    }
}
