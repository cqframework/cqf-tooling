package org.opencds.cqf.modelinfo.fhir;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.modelinfo.ModelInfoBuilder;
import org.opencds.cqf.modelinfo.ModelInfoSettings;
import org.hl7.elm_modelinfo.r1.ClassInfo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FHIRModelInfoBuilder extends ModelInfoBuilder {
    private String fhirHelpersPath;

    public FHIRModelInfoBuilder(String version, Collection<TypeInfo> typeInfos, String fhirHelpersPath) {
        super(typeInfos);
        this.fhirHelpersPath = fhirHelpersPath;
        this.settings = new FHIRModelInfoSettings(version);
    }

    private List<ClassInfo> getFhirElementInfos() {
        return  this.typeInfos.stream().map(x -> (ClassInfo)x)
        .filter(x -> x != null && x.getBaseType() != null && x.getBaseType().equals("FHIR.Element"))
        .filter(x -> x.getElement().size() == 1).collect(Collectors.toList());
    }

    @Override
    protected void beforeBuild() {
        List<ClassInfo> fhirElementInfos = this.getFhirElementInfos();
        fhirElementInfos.stream().map(x -> new ConversionInfo()
            .withFromType(x.getName())
            .withToType(x.getElement().get(0).getType())
            .withFunctionName("FHIRHelpers.To" + this.unQualify(x.getElement().get(0).getType())))
        .forEach(x -> this.settings.conversionInfos.add(x));

        List<String> statements = new ArrayList<>();
        fhirElementInfos.stream().forEach(x -> {
            String sourceTypeName = x.getName();
            String targetTypeName = x.getElement().get(0).getType();
            String functionName = "To" + this.unQualify(targetTypeName);
            statements.add("define function " + functionName + "(value " + sourceTypeName + "): value.value");
        });

        // TODO: File naming?
        try {
            PrintWriter pw = new PrintWriter(this.fhirHelpersPath);
            statements.stream().forEach(x -> pw.println(x));
            pw.close();
        }
        catch (Exception e) {
            System.out.println("Unable to write FileHelpers");
        }
    }
}