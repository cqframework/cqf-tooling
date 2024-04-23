package org.opencds.cqf.tooling.qdm;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import info.bliki.wiki.model.WikiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QdmToQiCore extends Operation {

    private static final Logger logger = LoggerFactory.getLogger(QdmToQiCore.class);

    private final String[] typeURLS = {
            "Adverse_Event_(QDM)", "Allergy/Intolerance_(QDM)", "Assessment_(QDM)",
            "Care_Experience_(QDM)", "Care_Goal_(QDM)", "Communication_(QDM)",
            "Condition/Diagnosis/Problem_(QDM)", "Device_(QDM)", "Diagnostic_Study_(QDM)",
            "Encounter_(QDM)", "Family_History_(QDM)", "Immunization_(QDM)",
            "Individual_Characteristic_(QDM)", "Intervention_(QDM)", "Laboratory_Test_(QDM)",
            "Medication_(QDM)", "Participation_(QDM)", "Physical_Exam_(QDM)",
            "Procedure_(QDM)", "Substance_(QDM)", "Symptom_(QDM)"
    };

    @Override
    public void execute(String[] args) {
        if (args.length > 1) {
            setOutputPath(args[1]);
        }
        else {
            setOutputPath("src/main/resources/org/opencds/cqf/tooling/qdm/output");
        }

        // For each type, scrape the html
        int subsection = 0;
        for (String typeURL : typeURLS) {
            String baseURL = "http://wiki.hl7.org/index.php?title=";
            String viewSourceQuery = "&action=edit";
            String fullURL = baseURL + typeURL + viewSourceQuery;
            URL url;
            try {
                url = new URL(fullURL);
            } catch (MalformedURLException e) {
                logger.error("Encountered the following malformed URL: {}", fullURL);
                e.printStackTrace();
                return;
            }

            String content;
            try {
                content = getCleanContent(getPageContent(url));
            } catch (IOException e) {
                logger.error("Encountered the following exception while scraping content from {} : {}", fullURL, e.getMessage());
                e.printStackTrace();
                return;
            }

            String html = WikiModel.toHtml(content);
            if (StringUtils.countMatches(html, "<h2>") == 2) {
                // remove toc
                html = removeToc(html);
                html = removeSubHeading(html);
            }

            String type = typeURL.replaceAll("_\\(QDM\\)", "").replaceAll("([_/])", " ");
            html = addHeading(html, type, ++subsection);
            html = transformToc(html);
            html = removeToc(html);
            html = addSubHeadings(html, type, subsection);
            String htmlHeader = "---\n" +
                    "# jekyll header\n" +
                    "---\n" +
                    "{% include header.html %}\n" +
                    "{% include container-start.html %}\n";
            String htmlFooter = "\n" +
                    "{% include container-end.html %}\n" +
                    "{% include footer.html %}";
            html = htmlHeader + html + htmlFooter;

            String fileName = typeURL.replaceAll("([_/])", "").replaceAll("\\(QDM\\)", "");
            try {
                writeOutput(fileName, html);
            } catch (IOException e) {
                logger.error("Encountered the following exception while creating file {}.html: {}", fileName, e.getMessage());
                e.printStackTrace();
                return;
            }
        }
    }

    private String removeToc(String html) {
        Pattern pattern = Pattern.compile("(?<=<table)(.*)(?=<hr/>)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.replaceAll("").replaceAll("<table<hr/>", "");
        }
        return html;
    }

    private String removeSubHeading(String html) {
        Pattern pattern = Pattern.compile("(?<=<h2>)(.*)(?=</h2>)");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return html.replaceAll(matcher.group(), "").replaceAll("<h2></h2>", "");
        }
        return html;
    }

    private String transformToc(String html) {
        StringBuilder tocBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile("(?<=<table)(.*)(?=<hr/>)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            tocBuilder.append("<ul id=\"markdown-toc\">");
            pattern = Pattern.compile("<li class=\"toclevel-1\">.*</a>");
            Matcher liMatcher = pattern.matcher(matcher.group());
            while (liMatcher.find()) {
                tocBuilder.append(liMatcher.group());
            }
            tocBuilder.append("</ul>");
        }
        return tocBuilder.toString().replaceAll("<li class=\"toclevel-1\">", "<li>").replaceAll("</a>", "</a></li>") + html;
    }

    private String addHeading(String html, String type, int subsection) {
        String typeSansSpaces = type.replaceAll("\\s", "");
        String heading = String.format(
                "\n<a name=\"%s\"> </a>\n" +
                        "<h2>\n" +
                        "    <span class=\"sectioncount\">%s.%d.%s</span> %s <a href=\"%s.html#%s\" title=\"link to here\" class=\"self-link\"> <img src=\"target.png\" width=\"20\" class=\"self-link\" height=\"20\"/></a>\n" +
                        "</h2>\n", typeSansSpaces, "7", subsection, "0", type, typeSansSpaces, typeSansSpaces);
        return heading + html;
    }

    private String addSubHeadings(String html, String type, int subsection) {
        Pattern pattern = Pattern.compile("(?<=<h2>)(.*)(?=</h2>)");
        Matcher matcher = pattern.matcher(html);
        int subsubsection = 0;
        while (matcher.find()) {
            String match = matcher.group();
            pattern = Pattern.compile("(?<=id=\")(.*)(?=\")");
            Matcher idMatch = pattern.matcher(match);
            if (idMatch.find()) {
                String id = idMatch.group();
                pattern = Pattern.compile("(?<=" + id + "\">)" + "(.*)(?=" + "</span>)");
                Matcher typeMatch = pattern.matcher(match);
                if (typeMatch.find()) {
                    String subType = typeMatch.group();
                    html = html.replaceAll(regexEscape(match), "")
                            .replaceAll("<h2></h2>",
                                    String.format("\n<a name=\"%s\"> </a>\n" +
                                            "<h3>\n" +
                                            "    <span class=\"sectioncount\">%s.%d.%d</span> %s <a href=\"%s.html#%s\" title=\"link to here\" class=\"self-link\"> <img src=\"target.png\" width=\"20\" class=\"self-link\" height=\"20\"/></a>\n" +
                                            "</h3>", id, "7", subsection, ++subsubsection, subType, type.replaceAll("\\s", ""), id)
                            );
                }
            }
        }
        return html;
    }

    private String regexEscape(String regex) {
        return regex
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\[", "\\\\[")
                .replaceAll("]", "\\\\]");
    }

    private void writeOutput(String fileName, String content) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(
                IOUtils.concatFilePath(getOutputPath(),fileName + ".html"))) {
            writer.write(content.getBytes());
            writer.flush();
        }
    }

    private String getCleanContent(String content) {
        content = content.split("<textarea.*")[1].split("</textarea>")[0];
        return content.replaceAll("&lt;br>", System.lineSeparator());//.replaceAll("__FORCETOC__", "");
    }

    private String getPageContent(URL url) throws IOException {
        StringBuilder content = new StringBuilder();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER || status == 307 || status == 308) {
                String newUrl = conn.getHeaderField("Location");
                return getPageContent(new URL(newUrl));
            } else {
                throw new IOException("Unexpected response code: " + status);
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

}
