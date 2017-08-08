package cc.mallet.topics.gui;

public class OptionStrings { 
	private String optionA;
	private String optionB;
	private String optionC;
	private boolean optionD;

	public OptionStrings(addOptionA, addOptionB, addOptionC, addOptionD) {
		this.optionA = addOptionA;
		this.optionB = addOptionB;
		this.optionC = addOptionC;
		this.optionD = addOptionD;
	}

	public String getOptionA() {
		return this.optionA;
	}

	public String getOptionB() {
		return this.optionB;
	}

	public String getOptionC() {
		return this.optionC;
	}

	public boolean getOptionD() {
		return this.optionD;
	}
}
