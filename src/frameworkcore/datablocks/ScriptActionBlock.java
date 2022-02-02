package frameworkcore.datablocks;

import frameworkcore.excelreaders.ScriptReader;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo), Surbhi Sinha
 *         (surbhi.b.sinha)
 */
public class ScriptActionBlock {

	private int stepNo = 0;
	private String keyword = null;
	private String keywordType = null;
	private String elementName = null;
	private String screenAction = null;
	private String value = null;
	private String elementType = null;
	private String environmentStepGroup = null;
	private String xpath = null;

	public ScriptActionBlock(ScriptReader scriptReader, int stepNo, int offset) {
		this.stepNo = stepNo;
		this.keywordType = scriptReader.readStep(stepNo, 0 + offset);
		this.environmentStepGroup = scriptReader.readStep(stepNo, 6 + offset);
	}

	public ScriptActionBlock(String elementName, String elementType, String screenAction, String xpath, String value) {
		this.elementName = elementName;
		this.elementType = elementType;
		this.screenAction = screenAction;
		this.xpath = xpath;
		this.value = value;

	}

	public void setAttributes(ScriptReader scriptReader, int stepNo, int offset) {
		this.keyword = scriptReader.readStep(stepNo, 1 + offset);
		this.elementName = scriptReader.readStep(stepNo, 2 + offset);
		this.screenAction = scriptReader.readStep(stepNo, 3 + offset);
		this.value = scriptReader.readStep(stepNo, 4 + offset);
		this.elementType = scriptReader.readStep(stepNo, 5 + offset);
		this.xpath = scriptReader.readStep(stepNo, 7 + offset);
	}

	public int getStepNo() {
		return stepNo;
	}

	public String getKeywordType() {
		return keywordType;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getElementName() {
		return elementName;
	}

	public String getAction() {
		return screenAction;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String getElementType() {
		return elementType;
	}

	public String getEnvironmentStepGroup() {
		return environmentStepGroup;
	}

	public String getElementXpath() {
		return xpath;
	}

}
