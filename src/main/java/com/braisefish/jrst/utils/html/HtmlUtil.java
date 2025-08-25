package com.braisefish.jrst.utils.html;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class HtmlUtil {
    public final String DEFAULT_TABLE_STYLE = ".sl-show-table{page-break-inside:auto;margin:0 auto;font-size:12px;width:100%;min-height:25px;line-height:25px;text-align:left;border:1px solid#ccc;border-collapse:collapse}.sl-show-table tr{page-break-inside:avoid;page-break-after:auto}.sl-show-table tr th,.sl-show-table tr td{border:1px solid#ccc}.sl-show-table td,.sl-show-table th{text-align:center}.sl-show-table thead{display:table-header-group}.sl-show-table tr td{padding:5px}";
    public final String DEFAULT_A4_STYLE = "@page a4{size:8.27in 11.69in;margin:.5in.5in.5in.5in;mso-header-margin:.5in;mso-footer-margin:.5in;mso-paper-source:0}div.a4{page:a4}thead{display:table-header-group}tfoot{display:table-row-group}tr{page-break-inside:avoid; page-break-after: auto;}p.page-break-after{page-break-after:always}";
    private Builder builder;
    private HtmlUtil(Builder builder) {
        this.builder = builder;
    }

    public String toStr(String htmlTags) {
        String content = builder.getContent();
        if (StrUtil.isBlank(content)) {
            content = "";
        }
        if(StrUtil.isNotBlank(htmlTags)){
            content += htmlTags;
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>" + builder.getTitle() + "</title>\n" +
                "    <style>\n" +
                "        " + StrUtil.join("\n", builder.getStyles()) + "\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    " + content + "\n" +
                "</body>\n" +
                "    <script>\n" +
                "        " + StrUtil.join("\n", builder.getScripts()) + "\n" +
                "    </script>\n" +
                "</html>";
    }

    public String toStr() {
        return toStr("");
    }


    public static class Builder {
        public HtmlUtil build() {
            return new HtmlUtil(this);
        }

        private List<String> styles;
        private List<String> scripts;
        /**
         * html标题
         */
        private String title;
        /**
         * html内容
         */
        private String content;


        public List<String> getStyles() {
            return styles;
        }

        public Builder addStyle(String style) {
            if (this.styles == null) {
                this.styles = new ArrayList<>();
            }
            this.styles.add(style);
            return this;
        }

        public Builder setStyles(List<String> styles) {
            this.styles = styles;
            return this;
        }

        public List<String> getScripts() {
            return scripts;
        }

        public Builder addScript(String script) {
            if (this.scripts == null) {
                this.scripts = new ArrayList<>();
            }
            this.scripts.add(script);
            return this;
        }

        public Builder setScripts(List<String> scripts) {
            this.scripts = scripts;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getContent() {
            return content;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }
    }
}
