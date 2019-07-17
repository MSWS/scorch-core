package com.scorch.core.modules.chat;

import com.scorch.core.modules.chat.FilterModule.FilterType;

public class FilterEntry {
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
}
