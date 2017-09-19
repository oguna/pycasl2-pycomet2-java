package pycasl2;

class Error extends RuntimeException {
    private static final long serialVersionUID = 1L;
		private final int lineNum;
    private final String src;

    Error(int lineNum, String src, String message) {
        super(message);
        this.lineNum = lineNum;
        this.src = src;
    }

    @Override
    public String getMessage() {
        return String.format("Error: %s%nLine %d: %s", super.getMessage(), this.lineNum, this.src);
    }
}
