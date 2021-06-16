package org.opencds.cqf.tooling.quick;

import java.net.URI;
import java.net.URISyntaxException;

public class HtmlBuilder {

    private QuickAtlas atlas;

    private final String header = "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\"/>\n" +
            "\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
            "\n" +
            "    <link href=\"../fhir.css\" rel=\"stylesheet\"> </link>\n" +
            "\n" +
            "    <!-- Bootstrap core CSS -->\n" +
            "    <link href=\"../dist/css/bootstrap.css\" rel=\"stylesheet\"> </link>\n" +
            "    <link href=\"../assets/css/bootstrap-fhir.css\" rel=\"stylesheet\"> </link>\n" +
            "    <link href=\"../assets/css/bootstrap-glyphicons.css\" rel=\"stylesheet\"> </link>\n" +
            "\n" +
            "    <!-- Project extras -->\n" +
            "    <link href=\"../assets/css/project.css\" rel=\"stylesheet\"> </link>\n" +
            "    <link href=\"../assets/css/pygments-manni.css\" rel=\"stylesheet\"> </link>\n" +
            "    <link href=\"../jquery-ui.css\" rel=\"stylesheet\"> </link>\n" +
            "\n" +
            "    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->\n" +
            "    <!--[if lt IE 9]>\n" +
            "    <script src=\"../assets/js/html5shiv.js\"> </script>\n" +
            "    <script src=\"../assets/js/respond.min.js\"> </script>\n" +
            "    <![endif]-->\n" +
            "\n" +
            "    <!-- Favicons -->\n" +
            "    <link rel=\"apple-touch-icon-precomposed\" sizes=\"144x144\" href=\"../assets/ico/apple-touch-icon-144-precomposed.png\"> </link>\n" +
            "    <link rel=\"apple-touch-icon-precomposed\" sizes=\"114x114\" href=\"../assets/ico/apple-touch-icon-114-precomposed.png\"> </link>\n" +
            "    <link rel=\"apple-touch-icon-precomposed\" sizes=\"72x72\" href=\"../assets/ico/apple-touch-icon-72-precomposed.png\"> </link>\n" +
            "    <link rel=\"apple-touch-icon-precomposed\" href=\"../assets/ico/apple-touch-icon-57-precomposed.png\"> </link>\n" +
            "    <link rel=\"shortcut icon\" href=\"../assets/ico/favicon.png\"> </link>\n" +
            "\n" +
            "</head>\n" +
            "<body>";

    private final String footer = "</body>\n" +
            "</html>";

    private final String mustSupportIcon = "<span class=\"glyphicon glyphicon-ok\"> </span>";
    private final String modifierIcon = "<span class=\"glyphicon glyphicon-exclamation-sign\"> </span>";
    private final String extensionIcon = "<span class=\"glyphicon glyphicon-star\"> </span>";

    private StringBuilder html = new StringBuilder();

    @SuppressWarnings("unused")
    private String profileName;

    private String fileName;
    public String getFileName() {
        return fileName;
    }

    public HtmlBuilder(String profileName, QuickAtlas atlas) {
        this.profileName = profileName;
        this.atlas = atlas;
        this.fileName = "QUICK-" + profileName + ".html";
        html.append(header);
    }

    public String build() {
        html.append("</div>\n");
        html.append(footer);
        return html.toString();
    }

    public HtmlBuilder buildLegend() {
        html.append(String.format("<p>%s = Must Support, %s = Is Modifier, %s = QiCore defined extension</p>\n", mustSupportIcon, modifierIcon, extensionIcon));
        return this;
    }

    public HtmlBuilder buildHeader(String name) {
        String header = String.format(
                "<div><a name='%s'> </a>\n<h2> %s <a href='#%s' title='link to here' class='self-link'> <img src='../target.png' width='20' class='self-link' height='20'/></a></h2>\n", name, name, name
        );
        html.append(header);
        return this;
    }

    public HtmlBuilder buildOverviewHeader(String name) {
        String header = String.format(
                "<div><a name='overview'> </a>\n<h2> %s <a href='#overview' title='link to here' class='self-link'> <img src='../target.png' width='20' class='self-link' height='20'/></a></h2>\n", name
        );
        html.append(header);
        return this;
    }

    public HtmlBuilder buildParagraph(String content) {
        html.append("<p>").append(content).append("</p>\n");
        return this;
    }

    public HtmlBuilder buildTableStart() {
        html.append("<table class='table table-striped table-bordered'>\n").append("<tr><th>Field</th><th>Card.</th><th>Type</th><th>Description</th></tr>\n");
        return this;
    }

    public HtmlBuilder buildTableEnd() {
        html.append("</table>\n");
        return this;
    }

    public HtmlBuilder buildOverviewTableStart() {
        html.append("<table class='table table-striped table-bordered'>\n").append("<tr><th>FHIR Type</th><th>CQL Type</th></tr>\n");
        return this;
    }

    public HtmlBuilder buildOverviewTableEnd() {
        html.append("</table>\n");
        return this;
    }

    public HtmlBuilder buildRow(boolean mustSupport, boolean isModifier, boolean qicoreExt,
                                String field, String card, String type,
                                String description)
    {
        String row = String.format(
                "<tr><th>%s%s%s%s</th><td>%s</td><td>%s</td><td>%s</td></tr>\n", mustSupport ? mustSupportIcon : "",
                isModifier ? modifierIcon : "", qicoreExt ? extensionIcon : "", field, card, type, description
        );
        html.append(row);
        return this;
    }

    public HtmlBuilder buildOverviewRow(String fhirType, String cqlType, String href) {
        String row = String.format(
                "<tr><th>%s</th><td><a href='%s' target='_blank'>%s</a></td></tr>\n", fhirType, href, cqlType
        );
        html.append(row);
        return this;
    }

    public HtmlBuilder buildOverviewRowWithInterval(String fhirType, String cqlType, String href) {
        String intervalUrl = atlas.getCqlIntervalUrl();
        String row = String.format(
                "<tr><th>%s</th><td><a href='%s' target='_blank'>Interval</a>&lt;<a href='%s' target='_blank'>%s</a>&gt;</td></tr>\n", intervalUrl, fhirType, href, cqlType
        );
        html.append(row);
        return this;
    }

    public HtmlBuilder appendHtml(String content) {
        html.append(content);
        return this;
    }

    public static String buildLink(String href, String label) {
        if (href == null) {
            return String.format("<a href='%s'>%s</a>", "", label);
        }
        try {
            URI uri = new URI(href);
            if (uri.isAbsolute()) {
                return String.format("<a href='%s' target='_blank'>%s</a>", href, label);
            }
        } catch (URISyntaxException use) {
            // ignore
        }
        return String.format("<a href='%s'>%s</a>", href, label);
    }

    public static String buildNewTabLink(String href, String label) {
        return String.format("<a href='%s' target='_blank'>%s</a>", href, label);
    }

    public static String buildBinding(String href, String label, String strength) {
        if (href.contains("us/core/ValueSet/")) {
            href = href.replace("ValueSet/", "ValueSet-") + ".html";
        }
        else if (href.contains("http://hl7.org/fhir/us/qicore/ValueSet/")) {
            href = href.replace("http://hl7.org/fhir/us/qicore/ValueSet/", "../ValueSet-") + ".html";
        }
        String binding = "<br /><strong>Binding: </strong>";
        binding += String.format("<a href='%s' target='_blank'>%s</a> ", href, label);

        switch (strength) {
            case "required":
                binding += "(<a href='http://hl7.org/fhir/STU3/terminologies.html#required' target='_blank'>required</a>)";
                break;
            case "extensible":
                binding += "(<a href='http://hl7.org/fhir/STU3/terminologies.html#extensible' target='_blank'>extensible</a>)";
                break;
            case "preferred":
                binding += "(<a href='http://hl7.org/fhir/STU3/terminologies.html#preferred' target='_blank'>preferred</a>)";
                break;
            case "example":
                binding += "(<a href='http://hl7.org/fhir/STU3/terminologies.html#example' target='_blank'>example</a>)";
                break;
        }
        return binding;
    }

//    public HtmlBuilder buildRowWithBinding(boolean mustSupport, boolean isModifier, boolean qicoreExt,
//                                           String field, String title, String card, String type,
//                                           String description, String binding)
//    {
//        String row = String.format(
//                "<tr><td title='%s'>%s</td><td>%s</td><td>%s</td><td>%s\n<b>Binding: </b>%s</td></tr>\n", title, field, card, type, description, binding
//        );
//        html.append(row);
//        return this;
//    }
}
