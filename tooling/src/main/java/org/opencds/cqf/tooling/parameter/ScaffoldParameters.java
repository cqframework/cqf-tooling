package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.common.SoftwareSystem;

import java.util.List;
import java.util.Map;

public class ScaffoldParameters {
    //I believe all we need is what kind of Content they want to create
    //Maybe it is a list of Name value pairs, (NAME_OF_CONTENT, TYPE)
    //example:  (CMS_130, Measure)

    //Not sure this is what we're after, adding start of something to facilitate/house "stamping" code.

    public String igPath;
    public String igVersion;
    public org.opencds.cqf.tooling.utilities.IOUtils.Encoding outputEncoding;

    // Probably ought to be some common type for the set of resources we'll be scaffolding rather than list of Strings for class names.
    // NOTE: Map<NameOfArtifact, List<ResourceTypesToCreate>> E.e., { "CMS_130", { "Measure", "Library" } }
    public Map<String, List<String>> resourcesToScaffold;
    public List<SoftwareSystem> softwareSystems;
}
