package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class DefineStatementBody {
    private Retrieve retrieve;
    private WhereClause whereClause;
    private String conjunction;
    private String alias;

    public DefineStatementBody(Retrieve retrieve, WhereClause whereClause, String conjunction, String alias) {
        this.retrieve = retrieve;
        this.whereClause = whereClause;
        this.conjunction = conjunction;
        this.alias = alias;
    }

    public DefineStatementBody() {

    }

    public Retrieve getRetrieve() {
        return retrieve;
    }

    public void setRetrieve(Retrieve retrieve) {
        this.retrieve = retrieve;
    }

    public WhereClause getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(WhereClause whereClause) {
        this.whereClause = whereClause;
    }

    public String getConjunction() {
        return conjunction;
    }

    public void setConjunction(String conjunction) {
        this.conjunction = conjunction;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        String content;
        if (this.conjunction == null) {
            if (this.alias != null) {
                content = "\"" + this.getAlias() + "\"\n";
            } else {
                content = "(\n  " + this.retrieve.toString() + this.whereClause.toString() + ")\n";
            }
        } else {
            if (this.alias != null) {
                content = "    " + this.conjunction.toLowerCase() + "  (\n     \"" + this.getAlias() + "\"\n    )\n";
            } else {
                content = "    " + this.conjunction.toLowerCase() + "  (\n    " + this.retrieve.toString() + this.whereClause.toString() + "    )\n";
            }
        }
        return content;
    }
}
