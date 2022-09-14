package org.opencds.cqf.tooling.measure.r4;

import java.util.List;

import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Type;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;

@SuppressWarnings("serial")
@DatatypeDef(name = "TerminologyRef")
public abstract class TerminologyRef extends Type {

	public static enum TerminologyRefType {
		VALUESET, CODE, CODESYSTEM
	}

	protected String name;
	protected String id;
	protected TerminologyRefType type;

	public String getId() {
		return id;
	}

	public Element setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TerminologyRefType getType() {
		return type;
	}

	public void setType(TerminologyRefType type) {
		this.type = type;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean hasFormatComment() {
		return false;
	}

	@Override
	public List<String> getFormatCommentsPre() {
		return null;
	}

	@Override
	public List<String> getFormatCommentsPost() {
		return null;
	}

	public abstract String getDefinition();
}
