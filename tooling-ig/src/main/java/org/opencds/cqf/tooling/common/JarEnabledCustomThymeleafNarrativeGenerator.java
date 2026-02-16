package org.opencds.cqf.tooling.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Charsets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.BaseThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;


public class JarEnabledCustomThymeleafNarrativeGenerator extends BaseThymeleafNarrativeGenerator {
	private List<String> myPropertyFile;
	private NarrativeTemplateManifest manifest;

    public JarEnabledCustomThymeleafNarrativeGenerator(String... thePropertyFile) {
		super();
		var propFile = Arrays.asList(thePropertyFile);
		this.myPropertyFile = propFile;
		 try {
			 this.manifest = forManifestFileLocation(propFile);
		 } catch (IOException e) {
			 throw new InternalErrorException(e);
		 }
    }

    @Override
	public boolean populateResourceNarrative(FhirContext theFhirContext, IBaseResource theResource) {
		super.populateResourceNarrative(theFhirContext, theResource);
		return false;
    }

	@Override
	protected NarrativeTemplateManifest getManifest() {
		return this.manifest;
	}

    public static NarrativeTemplateManifest forManifestFileLocation(Collection<String> thePropertyFilePaths) throws IOException {
		List<String> manifestFileContents = new ArrayList<>(thePropertyFilePaths.size());
		for (String next : thePropertyFilePaths) {
			String resource = loadResourceAlsoFromJar(next);
			manifestFileContents.add(resource);
		}

        return NarrativeTemplateManifest.forManifestFileContents(manifestFileContents);
    }

    static String loadResourceAlsoFromJar(String name) throws IOException {
		if (name.startsWith("classpath:")) {
			String cpName = name.substring("classpath:".length());
			try (InputStream resource =  Thread.currentThread().getContextClassLoader().getResourceAsStream(cpName)) {
				if (resource == null) {
					try (InputStream resource2 =Thread.currentThread().getContextClassLoader().getResourceAsStream(File.separator + cpName)) {
						if (resource2 == null) {
							throw new IOException("Can not find '" + cpName + "' on classpath");
						}
						return IOUtils.toString(resource2, Charsets.UTF_8);
					}
				}
				return IOUtils.toString(resource, Charsets.UTF_8);
			}
		} else if (name.startsWith("file:")) {
			File file = new File(name.substring("file:".length()));
			if (file.exists() == false) {
				throw new IOException("File not found: " + file.getAbsolutePath());
			}
			try (FileInputStream inputStream = new FileInputStream(file)) {
				return IOUtils.toString(inputStream, Charsets.UTF_8);
			}
		} else if (name.startsWith("jar:")) {
			URL url = new URL(name);
            try (InputStream resource = url.openStream()) {
				if (resource == null) {
					throw new IOException("Can not find jar url '" + name + "'");
				}
				return IOUtils.toString(resource, Charsets.UTF_8);
			}
        }
        else {
			throw new IOException("Invalid resource name: '" + name + "' (must start with classpath: or file: or jar: )");
        }
	}

	public void setPropertyFile(String... thePropertyFile) {
		Validate.notNull(thePropertyFile, "Property file can not be null");
		myPropertyFile = Arrays.asList(thePropertyFile);
	}

	public List<String> getPropertyFile() {
		return myPropertyFile;
	}
}