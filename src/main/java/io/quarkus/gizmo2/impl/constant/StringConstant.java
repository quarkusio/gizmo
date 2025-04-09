package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

public final class StringConstant extends ConstantImpl {

    private final String value;

    public StringConstant(String value) {
        super(ConstantDescs.CD_String);
        this.value = value;
    }

    public StringConstant(final ConstantDesc constantDesc) {
        this((String) constantDesc);
    }

    public String desc() {
        return value;
    }

    public Optional<String> describeConstable() {
        return Optional.of(desc());
    }

    public boolean isNonZero() {
        return true;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof StringConstant other && equals(other);
    }

    public boolean equals(final StringConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    private static final char[] hexDigits = "0123456789abcdef".toCharArray();

    public StringBuilder toShortString(final StringBuilder b) {
        b.append('"');
        int cp;
        for (int i = 0; i < value.length(); i += Character.charCount(cp)) {
            cp = value.codePointAt(i);
            switch (cp) {
                case '\b' -> b.append("\\b");
                case '\f' -> b.append("\\f");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                case '"', '\\' -> b.append('\\').appendCodePoint(cp);
                default -> {
                    if (Character.isISOControl(cp)) {
                        assert cp < 256;
                        b.append('\\').append('u').append("00")
                                .append(hexDigits[cp >>> 4])
                                .append(hexDigits[cp & 0x0f]);
                    } else {
                        b.appendCodePoint(cp);
                    }
                }
            }
        }
        b.append('"');
        return b;
    }
}
