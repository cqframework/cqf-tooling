package org.opencds.cqf.tooling.modelinfo;

import org.hl7.elm_modelinfo.r1.*;
import org.hl7.fhir.r4.model.*;

import java.util.*;

public class SearchInfoBuilder {
    protected Atlas atlas;
    protected Map<String, TypeInfo> typeInfos;
    protected ModelInfoSettings settings;

    public SearchInfoBuilder(ModelInfoSettings settings, Atlas atlas, Map<String, TypeInfo> typeInfos) {
        this.settings = settings;
        this.atlas = atlas;
        this.typeInfos = typeInfos;
    }

    public void build() {
        this.innerBuild();
    }

    protected SearchInfo buildSearchInfo(SearchParameter sp, String resourceType) {
        // Do not add search infos for types that cannot be resolved
        String typeName = this.settings.name + "." + resourceType;
        TypeInfo baseType = typeInfos.get(typeName);
        if (baseType == null) {
            return null;
        }

        // Do not add search infos for non-class types (Should never be a thing)
        if (!(baseType instanceof ClassInfo)) {
            return null;
        }

        // TODO: Consider supporting non-computable search parameters?
        // Do not add search infos for search parameters without an expression, not computable...
        if (!sp.hasExpression()) {
            return null;
        }

        ClassInfo ci = (ClassInfo)baseType;

        SearchInfo si = new SearchInfo();
        si.setName(sp.getCode());

        // Path syntax expectations:
        // <type>.<property> (| <type>.<property>)*
        List<String> typePaths = new ArrayList<String>();
        for (String path : sp.getExpression().split("\\|")) {
            path = path.trim();
            if (path.startsWith(ci.getName() + ".")) {
                typePaths.add(path.substring(path.indexOf(".") + 1));
            }
        }

        // Do not add search infos for search parameters without paths matching the base type.
        if (typePaths.size() == 0) {
            return null;
        }

        si.setPath(String.join("|", typePaths));

        switch (sp.getType()) {
            case NUMBER: si.setType("System.Decimal"); break;
            case DATE: si.setType("System.DateTime"); break;
            case STRING: si.setType("System.String"); break;
            case TOKEN: si.setType("System.Code"); break;
            case REFERENCE: {
                if (sp.getTarget() != null) {
                    List<TypeSpecifier> targets = new ArrayList<TypeSpecifier>();
                    for (CodeType code : sp.getTarget()) {
                        TypeInfo ti = typeInfos.get(String.format("%s.%s", settings.name, code.getCode()));
                        if (ti != null && ti instanceof ClassInfo) {
                            targets.add(new NamedTypeSpecifier().withNamespace(((ClassInfo)ti).getNamespace()).withName(((ClassInfo)ti).getName()));
                        }
                    }
                    if (targets.size() > 1) {
                        si.setTypeSpecifier(new ChoiceTypeSpecifier().withChoice(targets));
                    }
                    else if (targets.size() == 1) {
                        NamedTypeSpecifier ts = (NamedTypeSpecifier)targets.get(0);
                        si.setType(String.format("%s.%s", ts.getNamespace(), ts.getName()));
                    }
                }

                if (si.getTypeSpecifier() == null && si.getType() == null) {
                    si.setType("FHIR.Reference");
                }
            }
            break;
            case QUANTITY: si.setType("System.Quantity"); break;
            case URI: si.setType("System.String"); break;
            case SPECIAL: si.setType("System.Any"); break;
        default:
            break;
        }

        ci.getSearch().add(si);

        return si;
    }

    protected Iterable<SearchInfo> buildSearchInfo(SearchParameter sp) {
        // TODO: Composite search parameter support?
        if (sp.getType() == Enumerations.SearchParamType.COMPOSITE) {
            return null;
        }

        List<SearchInfo> sis = new ArrayList<SearchInfo>();
        for (CodeType resourceType : sp.getBase()) {
            SearchInfo si = buildSearchInfo(sp, resourceType.getCode());
            if (si != null) {
                sis.add(si);
            }
        }

        return sis;
    }

/*
    private String primarySearchPath(Iterable<SearchInfo> searches, String typeName) {
        if (this.settings.primarySearchPath.containsKey(typeName)) {
            return this.settings.primarySearchPath.get(typeName);
        }
        else if (searches != null) {
            for (SearchInfo s : searches) {
                if (s.getName().toLowerCase().equals("code")) {
                    return s.getName();
                }
            }
        }

        return null;
    }
 */

    protected void innerBuild() {
        // for (SearchParameter sp : atlas.getSearchParameters().values()) {
        //     Iterable<SearchInfo> sis = buildSearchInfo(sp);
        //     // NOTE: There is nothing to add here, search infos are added to the classinfos they are part of
        // }
/*
        for (TypeInfo ti : typeInfos.values()) {
            if (ti instanceof ClassInfo) {
                ClassInfo ci = (ClassInfo)ti;
                ci.setPrimarySearchPath(primarySearchPath(ci.getSearch(), ci.getName()));
            }
        }
*/
    }
}
