package com.braisefish.jrst.utils.html;

import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.i.entity.KeyValuePair;

import java.util.ArrayList;
import java.util.List;

public class HtmlTagUtil {
    private Builder builder;
    private List<HtmlTagUtil> childrenTags;

    public Builder getBuilder() {
        return builder;
    }

    public List<HtmlTagUtil> getChildrenTags() {
        return childrenTags;
    }

    private HtmlTagUtil(Builder builder) {
        childrenTags = new ArrayList<>();
        this.builder = builder;
    }

    public HtmlTagUtil addChildTag(HtmlTagUtil childTag) {
        if (childTag == null) {
            throw new NullPointerException("childTag is null");
        }
        childrenTags.add(childTag);
        return this;
    }

    public String toStr() {
        return toStr(this);
    }

    public static String toStr(HtmlTagUtil htmlTagUtil) {
        Builder builder = htmlTagUtil.getBuilder();

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(builder.tagName);
        if (builder.tagStyle != null) {
            sb.append(" style=\"").append(builder.tagStyle).append("\"");
        }
        if (builder.tagAttrs != null && !builder.tagAttrs.isEmpty()) {
            for (KeyValuePair<String, String> tagAttr : builder.tagAttrs) {
                sb.append(" ").append(tagAttr.getKey()).append("=\"").append(tagAttr.getValue()).append("\"");
            }
        }
        if (StrUtil.isNotBlank(builder.directInsertion)) {
            sb.append(" ").append(builder.directInsertion);
        }
        sb.append(">\n");
        if (StrUtil.isNotBlank(builder.beforeText)) {
            sb.append(builder.beforeText).append("\n");
        }
        if (htmlTagUtil.getChildrenTags() != null && !htmlTagUtil.getChildrenTags().isEmpty()) {
            for (HtmlTagUtil childTag : htmlTagUtil.getChildrenTags()) {
                sb.append(toStr(childTag));
            }
        }
        if (StrUtil.isNotBlank(builder.afterText)) {
            sb.append(builder.afterText).append("\n");
        }
        sb.append("</").append(builder.tagName).append(">\n");
        return sb.toString();
    }

    public static class Builder {
        public Builder() {
            tagAttrs = new ArrayList<>();
        }

        private String tagName;
        private String tagStyle;
        private String directInsertion;
        private String beforeText;
        private String afterText;
        private List<KeyValuePair<String, String>> tagAttrs;

        public String getTagName() {
            return tagName;
        }

        public Builder setTagName(String tagName) {
            this.tagName = tagName;
            return this;
        }

        public String getTagStyle() {
            return tagStyle;
        }

        public Builder setTagStyle(String tagStyle) {
            this.tagStyle = tagStyle;
            return this;
        }

        public String getDirectInsertion() {
            return directInsertion;
        }

        public Builder setDirectInsertion(String directInsertion) {
            this.directInsertion = directInsertion;
            return this;
        }

        public List<KeyValuePair<String, String>> getTagAttrs() {
            return tagAttrs;
        }

        public Builder addTagAttr(KeyValuePair<String, String> tagAttr) {
            if (tagAttr == null) {
                throw new NullPointerException("tagAttr is null");
            }
            if (this.tagAttrs == null) {
                this.tagAttrs = new ArrayList<>();
            }
            this.tagAttrs.add(tagAttr);
            return this;
        }

        public Builder addTagAttr(String key, String value) {
            return this.addTagAttr(new KeyValuePair<>(key, value));
        }

        public Builder setTagAttrs(List<KeyValuePair<String, String>> tagAttrs) {
            this.tagAttrs = tagAttrs;
            return this;
        }

        public String getBeforeText() {
            return beforeText;
        }

        public Builder setBeforeText(String beforeText) {
            this.beforeText = beforeText;
            return this;
        }

        public String getAfterText() {
            return afterText;
        }

        public Builder setAfterText(String afterText) {
            this.afterText = afterText;
            return this;
        }

        public HtmlTagUtil build() {
            return new HtmlTagUtil(this);
        }
    }
}
