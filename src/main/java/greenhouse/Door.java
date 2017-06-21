package greenhouse;

interface Door {
    void open();
    void close();

    boolean isOpened();

    boolean isClosed();

    void waitUntilFinished();
}
