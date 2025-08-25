package com.braisefish.jrst.utils.str;

/**
 * @author sunchao
 */
public class StringBuilderExt {

    private final StringBuilder stringBuilder;

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public static StringBuilderExt getInstance() {
        return new StringBuilderExt(new StringBuilder());
    }

    public static StringBuilderExt of(StringBuilder stringBuilder) {
        return new StringBuilderExt(stringBuilder);
    }

    private StringBuilderExt(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }


    public StringBuilderExt clear() {
        stringBuilder.delete(0, stringBuilder.length());
        return this;
    }

    public StringBuilderExt append(String str) {
        stringBuilder.append(str);
        return this;
    }

    public StringBuilderExt appendLine(String str) {
        return append(str + "\n");
    }

    public StringBuilderExt lineBreak() {
        return append("\n");
    }

    public StringBuilderExt insert(int index, String str) {
        stringBuilder.insert(index, str);
        return this;
    }

    public StringBuilderExt insertLine(int index, String str) {
        return insert(index, str + "\n");
    }

    /**
     * 获取最终结果
     *
     * @return 最终结果
     */
    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
