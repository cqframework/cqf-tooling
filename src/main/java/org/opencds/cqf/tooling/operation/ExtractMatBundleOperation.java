package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class ExtractMatBundleOperation extends Operation {

	private String inputFile;
	private String version = "r4";
	private FhirContext context;
	private String encoding;
	
	@Override
	public void execute(String[] args) {		
		// Set file to extract
		inputFile = args[1];
		if (inputFile == null) {
			throw new IllegalArgumentException("The path to a bundle file is required");
		}
		
		// Set version
		if (args.length > 2) {
			if (args[2] != null) {
				String[] flagAndValue = args[2].split("=");
				String flag = flagAndValue[0];
		        String value = flagAndValue[1];
		        if (flag != "-v") {
		        	throw new IllegalArgumentException("Invalid argument: " + flag);
		        } else {
		        	version = value;
		        }
			}
		}

		// Open the file to validate it
        File bundleFile = new File(inputFile);
        if (bundleFile.isDirectory()) {
        	throw new IllegalArgumentException("The path to a bundle file is required");
        }
        
        // Set the FhirContext based on the version specified
        if (version == null) {
            context = FhirContext.forR4();
        }
        else {
            switch (version.toLowerCase()) {
                case "stu3":
                    context = FhirContext.forDstu3();
                    break;
                case "r4":
                    context = FhirContext.forR4();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fhir version: " + version);
            }
        }
        
        // Read in the Bundle
        IBaseResource bundle;
        if (bundleFile.getPath().endsWith(".xml")) {
        	encoding = "xml";
            try {
                bundle = context.newXmlParser().parseResource(new FileReader(bundleFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        else if (bundleFile.getPath().endsWith(".json")) {
        	encoding = "json";
            try {
                bundle = context.newJsonParser().parseResource(new FileReader(bundleFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        else {
        	throw new IllegalArgumentException("The path to a bundle file of type json or xml is required");
        }
        
        // Now call the Bundle utilities to extract the bundle
        String outputDir = bundleFile.getAbsoluteFile().getParent();
        if (version == "stu3") {
        	BundleUtils.extractStu3Resources((org.hl7.fhir.dstu3.model.Bundle)bundle, encoding, outputDir);
        } else if (version == "r4") {
        	BundleUtils.extractR4Resources((org.hl7.fhir.r4.model.Bundle)bundle, encoding, outputDir);
        }
        
        // Now move and properly rename the files
        moveAndRenameFiles(outputDir);
        
	}
	
	/**
	 * Iterates through the files and properly renames and moves them to the proper place
	 * 
	 * @param outputDir
	 */
	private void moveAndRenameFiles(String outputDir) {
		File[] extractedFiles = new File(outputDir).listFiles();
        for (File extractedFile : extractedFiles) {
        	IBaseResource theResource = null;
        	if (extractedFile.getPath().endsWith(".xml")) {
        		try {
                    theResource = context.newXmlParser().parseResource(new FileReader(extractedFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                } 
        	}
        	else if (extractedFile.getPath().endsWith(".json")) {
        		try {
                    theResource = context.newJsonParser().parseResource(new FileReader(extractedFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                } 
        	}
        	
        	// The extractor code names them using the resource type and ID
        	// We want to name them without the resource type, use name, and if needed version
        	String resourceName;
        	String newOutputDirectory = outputDir.substring(0, outputDir.indexOf("bundles/")) + "input/";
        	if (version == "stu3) ") {
        		if (theResource instanceof org.hl7.fhir.dstu3.model.Library) {
        			org.hl7.fhir.dstu3.model.Library theLibrary = (org.hl7.fhir.dstu3.model.Library)theResource;
        			resourceName = theLibrary.getName();
        			
        			// Forcing the encoding to JSON here to make everything the same in input directory
        			ResourceUtils.outputResourceByName(theResource, "json", context,
        					newOutputDirectory + "resources/library/", resourceName);
        			
        			// Now extract the CQL from the library file
        			String cqlFilename = newOutputDirectory + "cql/" + resourceName + ".cql";
        			extractStu3CQL(theLibrary, cqlFilename);
        		}
        		else if (theResource instanceof org.hl7.fhir.dstu3.model.Measure) {
        			resourceName = ((org.hl7.fhir.dstu3.model.Measure)theResource).getName();
        			
        			// Forcing the encoding to JSON here to make everything the same in input directory
        			ResourceUtils.outputResourceByName(theResource, "json", context,
        					newOutputDirectory + "resources/measure/", resourceName);
        		}
        	}
        	else if (version == "r4") {
        		if (theResource instanceof org.hl7.fhir.r4.model.Library) {
        			org.hl7.fhir.r4.model.Library theLibrary = (org.hl7.fhir.r4.model.Library)theResource;
        			resourceName = theLibrary.getName();
        			
        			// Forcing the encoding to JSON here to make everything the same in input directory
        			ResourceUtils.outputResourceByName(theResource, "json", context,
        					newOutputDirectory + "resources/library/", resourceName);
        			
        			// Now extract the CQL from the library file
        			String cqlFilename = newOutputDirectory + "cql/" + resourceName + ".cql";
        			extractR4CQL(theLibrary, cqlFilename);
        		}
        		else if (theResource instanceof org.hl7.fhir.r4.model.Measure) {
        			resourceName = ((org.hl7.fhir.r4.model.Measure)theResource).getName();
        			
        			// Forcing the encoding to JSON here to make everything the same in input directory
        			ResourceUtils.outputResourceByName(theResource, "json", context,
        					newOutputDirectory + "resources/measure/", resourceName);
        		}
        	}
        }
	}
	
	/**
	 * Looks at the content of the Library passed in and if the type is texl/cql extracts it and decodes
	 * it and writes it to the filename passed in
	 * 
	 * @param theLibrary
	 * @param cqlFilename
	 */
	private void extractStu3CQL(org.hl7.fhir.dstu3.model.Library theLibrary, String cqlFilename) {
		List<org.hl7.fhir.dstu3.model.Attachment> contents = theLibrary.getContent();
		for (org.hl7.fhir.dstu3.model.Attachment content : contents) {
			if (content.getContentType().equals("text/cql")) {
				byte[] encodedBytes = content.getData();
				String encodedString = Base64.getEncoder().encodeToString(encodedBytes);
				byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
				try {
					FileUtils.writeByteArrayToFile(new File(cqlFilename), decodedBytes);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Looks at the content of the Library passed in and if the type is texl/cql extracts it and decodes
	 * it and writes it to the filename passed in
	 * 
	 * @param theLibrary
	 * @param cqlFilename
	 */
	private void extractR4CQL(org.hl7.fhir.r4.model.Library theLibrary, String cqlFilename) {
		List<org.hl7.fhir.r4.model.Attachment> contents = theLibrary.getContent();
		for (org.hl7.fhir.r4.model.Attachment content : contents) {
			if (content.getContentType().equals("text/cql")) {
				byte[] encodedBytes = content.getData();
				String encodedString = Base64.getEncoder().encodeToString(encodedBytes);
				byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
				try {
					FileUtils.writeByteArrayToFile(new File(cqlFilename), decodedBytes);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

}
