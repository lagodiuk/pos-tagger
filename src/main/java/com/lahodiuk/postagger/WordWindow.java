package com.lahodiuk.postagger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WordWindow {

	private String currentToken;

	private List<Tag> previousTags = new ArrayList<>();

	private List<String> previousTokens = new ArrayList<>();

	private List<String> followingTokens = new ArrayList<>();

	public String getCurrentToken() {
		return this.currentToken;
	}

	public List<String> getPreviousTokens() {
		return this.previousTokens;
	}

	public List<String> getFollowingTokens() {
		return this.followingTokens;
	}

	public void setCurrentToken(String currentToken) {
		this.currentToken = currentToken;
	}

	public void setFollowingTokens(Collection<String> followingTokens) {
		this.followingTokens.addAll(followingTokens);
	}

	public void setPreviousTokens(Collection<String> previousTokens) {
		this.previousTokens.addAll(previousTokens);
	}

	public List<Tag> getPreviousTags() {
		return this.previousTags;
	}

	public void setPreviousTags(Collection<Tag> previousTags) {
		this.previousTags.addAll(previousTags);
	}

	@Override
	public String toString() {
		return "WordWindow [currentToken=" + this.currentToken + ", previousTags=" + this.previousTags + ", previousTokens=" + this.previousTokens + ", followingTokens="
				+ this.followingTokens + "]";
	}

}
