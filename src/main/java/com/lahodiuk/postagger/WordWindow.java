package com.lahodiuk.postagger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WordWindow {

	private String currentToken;

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

	@Override
	public String toString() {
		return "[previousTokens=" + this.previousTokens + ", currentToken=" + this.currentToken + ", followingTokens=" + this.followingTokens + "]";
	}
}
