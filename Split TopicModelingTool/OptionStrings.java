package cc.mallet.topics.gui;

public class OptionStrings { 
	private String optionStringA;
	private String optionStringB;
	private String optionStringC;
	private String optionStringD;
	private String[] optionStringArray;

	public OptionStrings(addStringA, addStringB, addStringC, addStringD) {
		this.optionStringA = addStringA;
		this.optionStringB = addStringB;
		this.optionStringC = addStringC;
		this.optionStringD = addStringD;
		this.optionStringArray = new String[] {this.optionStringA, this.optionStringB, 
			this.optionStringC, this.optionStringD};
	}

	public String[] getStringArray() {
		return this.optionStringArray;
	}

	public String getOptionStringA() {
		return this.optionStringA;
	}

	public String getOptionStringB() {
		return this.optionStringB;
	}

	public String getOptionStringC() {
		return this.optionStringC;
	}

	public String getOptionStringD() {
		return this.optionStringD;
	}
}
