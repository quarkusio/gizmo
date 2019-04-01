package io.quarkus.gizmo;

import java.io.Writer;

public final class NullWriter extends Writer {
    // NOTE: after Java 11, java.io.Writer#nullWriter()
    public static final NullWriter INSTANCE = new NullWriter();

    private NullWriter() {
    }

    public void write(final char[] cbuf, final int off, final int len) {
    }

    public void write(final int c) {
    }

    public void write(final char[] cbuf) {
    }

    public void write(final String str) {
    }

    public void write(final String str, final int off, final int len) {
    }

    public Writer append(final CharSequence csq) {
        return this;
    }

    public Writer append(final CharSequence csq, final int start, final int end) {
        return this;
    }

    public Writer append(final char c) {
        return this;
    }

    public void flush() {
    }

    public void close() {
    }
}
