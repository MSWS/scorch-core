package com.scorch.core.modules.chat;

public class FilterEntry implements Comparable<FilterEntry> {
	private String word;
	private FilterType type;

	public FilterEntry() {

	}

	public FilterEntry(String word, FilterType type) {
		this.word = word;
		this.type = type;
	}

	public String getWord() {
		return word;
	}

	public FilterType getType() {
		return type;
	}

	public void setType(FilterType type) {
		this.type = type;
	}

	@Override
	public int compareTo(FilterEntry o) {
		if (o.getType() == type)
			return o.getWord().length() - getWord().length();
		return o.getType().ordinal() - type.ordinal();
	}

	public enum FilterType {
		ALLOW, REGULAR, MANDATORY, ADVERTISING;
	}

}
