package com.braisefish.jrst.utils.str;

import java.util.ArrayList;
import java.util.List;

public class DynamicStringBuilder {

    private final List<String> lines;
    private final StringBuilder stringBuilder;
    private final StringBuilderExt stringBuilderExt;

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public StringBuilderExt getStringBuilderExt() {
        return stringBuilderExt;
    }

    public DynamicStringBuilder() {
        this.stringBuilder = new StringBuilder();
        this.stringBuilderExt = StringBuilderExt.of(this.stringBuilder);
        this.lines = new ArrayList<>();
    }

    public DynamicStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
        this.stringBuilderExt = StringBuilderExt.of(this.stringBuilder);
        this.lines = new ArrayList<>();
    }

    public DynamicStringBuilder(StringBuilderExt stringBuilderExt) {
        this.stringBuilder = stringBuilderExt.getStringBuilder();
        this.stringBuilderExt = stringBuilderExt;
        this.lines = new ArrayList<>();
    }

    public DynamicStringBuilder append(String str) {
        this.lines.add(str);
        return this;
    }

    public DynamicStringBuilder appendLine(String str) {
        this.lines.add(str + "\n");
        return this;
    }

    public DynamicStringBuilder lineBreak() {
        return append("\n");
    }

    public DynamicStringBuilder lineBreak(int count) {
        for (int i = 0; i < count; i++) {
            this.append("\n");
        }
        return this;
    }


    public DynamicStringBuilder insert(int index, String str) {
        this.lines.add(index, str);
        return this;
    }

    public DynamicStringBuilder insertLine(int index, String str) {
        return insert(index, str + "\n");
    }

    public DynamicStringBuilder clear() {
        this.lines.clear();
        this.stringBuilderExt.clear();
        return this;
    }

    @Override
    public String toString() {
        this.stringBuilderExt.clear();
        for (String line : lines) {
            this.stringBuilderExt.append(line);
        }
        return this.stringBuilderExt.toString();
    }
}
