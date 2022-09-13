package org.opencds.cqf.tooling.npm;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.json.JsonTrackingParser;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.npm.ToolsVersion;
import org.opencds.cqf.tooling.exception.NpmPackageManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NpmPackageManager {

    private static final Logger logger = LoggerFactory.getLogger(NpmPackageManager.class);

    private FilesystemPackageCacheManager pcm;
    private List<NpmPackage> npmList = new ArrayList<>();

    public List<NpmPackage> getNpmList() {
        return npmList;
    }

    private String version; // FHIR version as a string, e.g. 4.0.1

    public String getVersion() {
        return version;
    }

    private ImplementationGuide sourceIg;

    public ImplementationGuide getSourceIg() {
        return sourceIg;
    }

    /*
     * @param igPath Fully qualified path to the IG resource
     */
    public static NpmPackageManager fromPath(String igPath, String version) throws IOException {
        if (igPath == null || igPath.equals("")) {
            throw new IllegalArgumentException("igPath is required");
        }
        VersionConvertor_40_50 versionConvertor_40_50 = new VersionConvertor_40_50(new BaseAdvisor_40_50());
        return new NpmPackageManager(
                (ImplementationGuide) versionConvertor_40_50.convertResource(FormatUtilities.loadFile(igPath)),
                version);
    }

    public static NpmPackageManager fromStream(InputStream is, String version) throws IOException {
        VersionConvertor_40_50 versionConvertor_40_50 = new VersionConvertor_40_50(new BaseAdvisor_40_50());
        return new NpmPackageManager(
                (ImplementationGuide) versionConvertor_40_50.convertResource(FormatUtilities.loadFile(is)), version);
    }

    public NpmPackageManager(ImplementationGuide sourceIg, String version) throws IOException {
        if (version == null || version.equals("")) {
            throw new IllegalArgumentException("version is required");
        }

        this.version = version;

        if (sourceIg == null) {
            throw new IllegalArgumentException("sourceIg is required");
        }

        this.sourceIg = sourceIg;

        try {
            // userMode indicates whether the packageCache is within the working directory
            // or in the user home
            pcm = new FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION);
        } catch (IOException e) {
            throw new NpmPackageManagerException("error creating the FilesystemPackageCacheManager", e);
        }

        loadCorePackage();

        int i = 0;
        for (ImplementationGuide.ImplementationGuideDependsOnComponent dep : sourceIg.getDependsOn()) {
            loadIg(dep, i);
            i++;
        }
    }

    private void loadCorePackage() {
        NpmPackage pi = null;

        String v = version.equals(Constants.VERSION) ? "current" : version;

        logger.info("Core Package {}#{}", VersionUtilities.packageForVersion(v), v);
        try {
            pi = pcm.loadPackage(VersionUtilities.packageForVersion(v), v);
        } catch (Exception e) {
            try {
                logger.warn("First attempt at loading Core Package {}#{} failed with message {}", VersionUtilities.packageForVersion(v), v, e.getMessage());
                // Appears to be race condition in FHIR core where they are
                // loading a custom cert provider.
                pi = pcm.loadPackage(VersionUtilities.packageForVersion(v), v);
            } catch (Exception ex) {
                logger.error("Second attempt at loading Core Package {}#{} failed with message {}", VersionUtilities.packageForVersion(v), v, ex.getMessage());
                throw new NpmPackageManagerException("Error loading core package", ex);
            }
        }

        if (pi == null) {
            throw new NpmPackageManagerException("Could not load core package");
        }
        if (v.equals("current")) {
            throw new IllegalArgumentException("Current core package not supported");
        }
        npmList.add(pi);
    }

    private void loadIg(ImplementationGuide.ImplementationGuideDependsOnComponent dep, int index) throws IOException {
        String name = dep.getId();
        if (!dep.hasId()) {
            logger.info("Dependency '{}' has no id, so can't be referred to in markdown in the IG", idForDep(dep));
            name = "u" + Utilities.makeUuidLC().replace("-", "");
        }
        if (!isValidIGToken(name)) {
            throw new IllegalArgumentException("IG Name must be a valid token (" + name + ")");
        }

        String canonical = determineCanonical(dep.getUri(), "ImplementationGuide.dependency[" + index + "].url");
        String packageId = dep.getPackageId();
        if (Utilities.noString(packageId))
            packageId = pcm.getPackageId(canonical);
        if (Utilities.noString(canonical) && !Utilities.noString(packageId))
            canonical = pcm.getPackageUrl(packageId);
        if (Utilities.noString(canonical))
            throw new IllegalArgumentException("You must specify a canonical URL for the IG " + name);
        String igver = dep.getVersion();
        if (Utilities.noString(igver))
            throw new IllegalArgumentException(
                    "You must specify a version for the IG " + packageId + " (" + canonical + ")");

        NpmPackage pi = packageId == null ? null : pcm.loadPackageFromCacheOnly(packageId, igver);
        if (pi != null)
            npmList.add(pi);
        if (pi == null) {
            pi = resolveDependency(canonical, packageId, igver);
            if (pi == null) {
                if (Utilities.noString(packageId))
                    throw new IllegalArgumentException(
                            "Package Id for guide at " + canonical + " is unknown (contact FHIR Product Director");
                else
                    throw new IllegalArgumentException("Unknown Package " + packageId + "#" + igver);
            }
        }

        logger.debug(
                "Load " + name + " (" + canonical + ") from " + packageId + "#" + igver);

        if (dep.hasUri() && !dep.getUri().contains("/ImplementationGuide/")) {
            String cu = getIgUri(pi);
            if (cu != null) {
                logger.warn("The correct canonical URL for this dependency is " + cu);
            }
        }
    }

    private String determineCanonical(String url, String path) throws FHIRException {
        if (url == null)
            return url;
        if (url.contains("/ImplementationGuide/"))
            return url.substring(0, url.indexOf("/ImplementationGuide/"));
        if (path != null) {
            logger.warn(
                    "The canonical URL for an Implementation Guide must point directly to the implementation guide resource, not to the Implementation Guide as a whole");
        }
        return url;
    }

    private boolean isValidIGToken(String tail) {
        if (tail == null || tail.length() == 0)
            return false;
        boolean result = Utilities.isAlphabetic(tail.charAt(0));
        for (int i = 1; i < tail.length(); i++) {
            result = result && (Utilities.isAlphabetic(tail.charAt(i)) || Utilities.isDigit(tail.charAt(i))
                    || (tail.charAt(i) == '_'));
        }
        return result;

    }

    private String idForDep(ImplementationGuide.ImplementationGuideDependsOnComponent dep) {
        if (dep.hasPackageId()) {
            return dep.getPackageId();
        }
        if (dep.hasUri()) {
            return dep.getUri();
        }
        return "{no id}";
    }

    private String getIgUri(NpmPackage pi) throws IOException {
        for (String rs : pi.listResources("ImplementationGuide")) {
            JsonObject json = JsonTrackingParser.parseJson(pi.loadResource(rs));
            if (json.has("packageId") && json.get("packageId").getAsString().equals(pi.name()) && json.has("url")) {
                return json.get("url").getAsString();
            }
        }
        return null;
    }

    private NpmPackage resolveDependency(String canonical, String packageId, String igver) throws IOException {
        if (packageId != null)
            return pcm.loadPackage(packageId, igver);

        JsonObject pl;
        logger.debug("Fetch Package history from " + Utilities.pathURL(canonical, "package-list.json"));

        try {
            pl = fetchJson(Utilities.pathURL(canonical, "package-list.json"));
        } catch (Exception e) {
            return null;
        }
        if (!canonical.equals(pl.get("canonical").getAsString()))
            throw new IllegalArgumentException("Canonical mismatch fetching package list for " + canonical + "#" + igver
                    + ", package-list.json says " + pl.get("canonical"));
        for (JsonElement e : pl.getAsJsonArray("list")) {
            JsonObject o = (JsonObject) e;
            if (igver.equals(o.get("version").getAsString())) {
                InputStream src = fetchFromSource(pl.get("package-id").getAsString() + "-" + igver,
                        Utilities.pathURL(o.get("path").getAsString(), "package.tgz"));
                return pcm.addPackageToCache(pl.get("package-id").getAsString(), igver, src,
                        Utilities.pathURL(o.get("path").getAsString(), "package.tgz"));
            }
        }
        return null;
    }

    private JsonObject fetchJson(String source) throws IOException {
        URL url = new URL(source + "?nocache=" + System.currentTimeMillis());
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setInstanceFollowRedirects(true);
        return JsonTrackingParser.parseJson(c.getInputStream());
    }

    private InputStream fetchFromSource(String id, String source) throws IOException {
        logger.debug("Fetch " + id + " package from " + source);
        URL url = new URL(source + "?nocache=" + System.currentTimeMillis());
        URLConnection c = url.openConnection();
        return c.getInputStream();
    }
}
