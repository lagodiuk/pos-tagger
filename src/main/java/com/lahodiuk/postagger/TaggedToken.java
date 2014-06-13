package com.lahodiuk.postagger;

public class TaggedToken {
	private Tag tag;
	private String token;
	public TaggedToken(String token, Tag tag) {
		this.token = token;
		this.tag = tag;
	}
	public Tag getTag() {
		return this.tag;
	}
	public String getToken() {
		return this.token;
	}
	@Override
	public String toString() {
		return "[tag=" + this.tag + ", token=" + this.token + "]";
	}
}