package com.ntu.timetabling.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmailTemplateBuilder {

    private String heading;
    private String greetingName;
    private String introHtml;
    private final Map<String, String> details = new LinkedHashMap<>();
    private String closingHtml;
    private String signOff = "NTU Timetabling Team";

    public static EmailTemplateBuilder create() {
        return new EmailTemplateBuilder();
    }

    public EmailTemplateBuilder heading(String heading) {
        this.heading = heading;
        return this;
    }

    public EmailTemplateBuilder greeting(String name) {
        this.greetingName = name;
        return this;
    }

    public EmailTemplateBuilder intro(String html) {
        this.introHtml = html;
        return this;
    }

    public EmailTemplateBuilder detail(String label, String value) {
        this.details.put(label, value);
        return this;
    }

    public EmailTemplateBuilder closing(String html) {
        this.closingHtml = html;
        return this;
    }

    public EmailTemplateBuilder signOff(String signOff) {
        this.signOff = signOff;
        return this;
    }

    public String build() {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;")
                .append("border:1px solid #e5e5e5;border-radius:10px;overflow:hidden;background:#ffffff;\">");

        html.append("<div style=\"padding:26px 26px 24px;\">");

        // small brand mark (badge + system name) rather than a full colour banner - keeps the
        // pink focused on the actual title below, not spread across a background block
        html.append("<div style=\"margin-bottom:18px;\">")
                .append("<span style=\"background:#d0006f;color:#ffffff;font-weight:800;font-size:11px;")
                .append("padding:3px 8px;border-radius:5px;margin-right:8px;letter-spacing:0.3px;vertical-align:middle;\">NTU</span>")
                .append("<span style=\"color:#666666;font-size:12.5px;font-weight:600;vertical-align:middle;\">Timetabling Requests Management</span>")
                .append("</div>");

        html.append("<h2 style=\"margin:0 0 16px;color:#d0006f;font-size:19px;\">").append(escape(heading)).append("</h2>");
        html.append("<p style=\"margin:0 0 12px;color:#333;\">Hi ").append(escape(greetingName)).append(",</p>");
        html.append("<p style=\"margin:0 0 16px;color:#333;line-height:1.55;\">").append(introHtml).append("</p>");

        if (!details.isEmpty()) {
            // a distinct full soft-pink card (rounded, bordered, tinted background) rather than
            // a plain grey block or a left-accent bar - its own clearly-defined look
            html.append("<div style=\"background:#fdf0f6;border:1px solid #f4c9de;border-radius:10px;")
                    .append("padding:16px 20px;margin:18px 0;\">");
            for (Map.Entry<String, String> entry : details.entrySet()) {
                html.append("<p style=\"margin:5px 0;font-size:14px;color:#1a1a1a;\"><strong>")
                        .append(escape(entry.getKey())).append(":</strong> ").append(escape(entry.getValue())).append("</p>");
            }
            html.append("</div>");
        }

        if (closingHtml != null) {
            html.append("<p style=\"margin:16px 0 0;color:#333;line-height:1.55;\">").append(closingHtml).append("</p>");
        }
        html.append("</div>");

        // footer bar - a subtle divider + muted disclaimer + signature, rather than the
        // signature just trailing off at the bottom of the body
        html.append("<div style=\"border-top:1px solid #eeeeee;background:#fafafa;padding:14px 26px;\">")
                .append("<p style=\"margin:0;font-size:11.5px;color:#8a8a8a;\">")
                .append("This is an automated message from the NTU Timetabling Requests Management system.</p>")
                .append("<p style=\"margin:6px 0 0;font-size:12.5px;color:#555;font-weight:600;\">")
                .append(escape(signOff)).append("</p>")
                .append("</div>");

        html.append("</div>");
        return html.toString();
    }

    // basic HTML-escaping for anything sourced from user-entered data (names, module titles) -
    // this system's users are all admin-provisioned rather than self-registered, so the risk
    // here is low, but escaping costs nothing and avoids a name like "O'Brien <b>" doing
    // anything unexpected in an email client. Public so callers embedding their own inline HTML
    // (e.g. a bolded module reference inside an intro sentence) can still escape the dynamic
    // parts safely.
    public static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String escape(String s) {
        return escapeHtml(s);
    }
}
