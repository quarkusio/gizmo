package io.quarkus.gizmo;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public final class LineNumberWriter extends Writer {
    private final Writer delegate;
    private int lineNumber = 1;

    public LineNumberWriter(final Writer delegate) {
        this.delegate = delegate;
    }

    public void write(final int c) throws IOException {
        if (c == '\n') {
            lineNumber++;
        }
        delegate.write(c);
    }

    public void write(final char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    public void write(final String str) throws IOException {
        write(str, 0, str.length());
    }

    public void write(final String str, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            if (str.charAt(off + i) == '\n')
                lineNumber++;
        }
        delegate.write(str, off, len);
    }

    public Writer append(final CharSequence csq) throws IOException {
        append(csq, 0, csq.length());
        return this;
    }

    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        for (int i = 0; i < end - start; i++) {
            if (csq.charAt(start + i) == '\n')
                lineNumber++;
        }
        delegate.append(csq, start, end);
        return this;
    }

    public Writer append(final char c) throws IOException {
        write(c);
        return this;
    }

    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            if (cbuf[off + i] == '\n')
                lineNumber++;
        }
        delegate.write(cbuf, off, len);
    }

    public void flush() throws IOException {
        delegate.flush();
    }

    public void close() throws IOException {
        delegate.close();
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
