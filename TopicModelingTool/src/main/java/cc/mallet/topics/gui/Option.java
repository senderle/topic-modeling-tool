package cc.mallet.topics.gui;

public class Option<OptionType> { 
	private String description;
	private OptionType defaultVal;
	private String category;
	private boolean autogenerate;

	public Option(
            String description, 
            OptionType defaultVal, 
            String category, 
		    boolean autogenerate
    ) {
		this.description = description;
		this.defaultVal = defaultVal;
		this.category = category;
		this.autogenerate = autogenerate;
	}

    public void setDescription(String description) {
        this.description = description;
    }

	public String getDescription() {
		return this.description;
	}

    public void setDefaultVal(OptionType defaultVal) {
        this.defaultVal = defaultVal;
    }

	public OptionType getDefaultVal() {
		return this.defaultVal;
	}

    public void setCategory(String category) {
        this.category = category;
    }

	public String getCategory() {
		return this.category;
	}

    public void setAutogenerate(boolean autogenerate) {
        this.autogenerate = autogenerate;
    }

	public boolean getAutogenerate() {
		return this.autogenerate;
	}
}
