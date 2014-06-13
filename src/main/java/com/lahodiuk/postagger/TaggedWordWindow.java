package com.lahodiuk.postagger;

public class TaggedWordWindow {

	private Tag tag;

	private WordWindow wordWindow;

	public Tag getTag() {
		return this.tag;
	}

	public WordWindow getWordWindow() {
		return this.wordWindow;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public void setWordWindow(WordWindow wordWindow) {
		this.wordWindow = wordWindow;
	}

	@Override
	public String toString() {
		return this.tag + "\t" + this.wordWindow;
	}
}
