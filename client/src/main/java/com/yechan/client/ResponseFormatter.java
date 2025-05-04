package com.yechan.client;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResponseFormatter {

    public String formatToHtml(String response) {
        if (response == null || response.isEmpty()) {
            return "<p>검색 결과가 없습니다.</p>";
        }

        String html = response;
        html = html.replaceAll("(https?://[^\\s<]+)", "<a href=\"$1\" target=\"_blank\" class=\"text-primary\">$1</a>");
        html = html.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("##\\s+([^\n]+)", "<h2>$1</h2>");

        Pattern codeBlockPattern = Pattern.compile("```([^`]+)```", Pattern.DOTALL);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (codeBlockMatcher.find()) {
            String codeContent = codeBlockMatcher.group(1).trim();
            codeBlockMatcher.appendReplacement(sb, "<pre class=\"bg-light p-3 rounded\"><code>" + 
                                               codeContent.replace("$", "\\$") + "</code></pre>");
        }
        codeBlockMatcher.appendTail(sb);
        html = sb.toString();
        html = html.replaceAll("\n\n+", "</p><p>");
        html = html.replaceAll("\n", "<br>");
        if (!html.startsWith("<")) {
            html = "<p>" + html + "</p>";
        }
        return html;
    }
}
