package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

import java.util.List;

public class DefineBlock {
    private DefineStatement defineStatement;
    private List<DefineStatementBody> defineStatementBodies;

    public DefineBlock(DefineStatement defineStatement, List<DefineStatementBody> defineStatementBodies) {
        this.defineStatement = defineStatement;
        this.defineStatementBodies = defineStatementBodies;
    }

    public DefineBlock() {

    }

    public DefineStatement getDefineStatement() {
        return defineStatement;
    }

    public void setDefineStatement(DefineStatement defineStatement) {
        this.defineStatement = defineStatement;
    }

    public List<DefineStatementBody> getDefineStatementBody() {
        return defineStatementBodies;
    }

    public void setDefineStatementBody(List<DefineStatementBody> defineStatementBodies) {
        this.defineStatementBodies = defineStatementBodies;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(defineStatement.toString());
        defineStatementBodies.forEach(defineStatementBody -> stringBuilder.append(defineStatementBody.toString()));
        String content = stringBuilder.toString();
        return content;
    }
}
