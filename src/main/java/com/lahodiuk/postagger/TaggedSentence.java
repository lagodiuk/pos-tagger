package com.lahodiuk.postagger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class TaggedSentence {

	private String sentence;

	private List<TaggedToken> taggedTokens = new ArrayList<>();

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public void addTaggedToken(String token, Tag tag) {
		this.taggedTokens.add(new TaggedToken(token, tag));
	}

	public String getSentence() {
		return this.sentence;
	}

	public List<TaggedToken> getTaggedTokens() {
		return this.taggedTokens;
	}

	public List<TaggedWordWindow> getTaggedWordWindows(int neighboursCount, String margin) {
		List<TaggedWordWindow> windows = new ArrayList<>();

		Iterator<TaggedToken> followingIterator = this.taggedTokens.iterator();
		Queue<TaggedToken> followingTaggedTokens = new ArrayDeque<>();
		Queue<String> followingTokens = new ArrayDeque<>();
		int i = 0;
		while (i < (neighboursCount + 1)) {
			if (followingIterator.hasNext()) {
				TaggedToken next = followingIterator.next();
				followingTaggedTokens.add(next);
				followingTokens.add(next.getToken());
			} else {
				followingTokens.add(margin);
			}
			i++;
		}
		Queue<String> previousTokens = new ArrayDeque<>();
		for (int j = 0; j < neighboursCount; j++) {
			previousTokens.add(margin);
		}

		while (!followingTaggedTokens.isEmpty()) {
			TaggedToken currentToken = followingTaggedTokens.poll();
			followingTokens.poll();

			WordWindow ww = new WordWindow();
			ww.setCurrentToken(currentToken.getToken());
			ww.setPreviousTokens(previousTokens);
			ww.setFollowingTokens(followingTokens);
			TaggedWordWindow tww = new TaggedWordWindow();
			tww.setTag(currentToken.getTag());
			tww.setWordWindow(ww);
			windows.add(tww);

			previousTokens.add(currentToken.getToken());
			if (previousTokens.size() > neighboursCount) {
				previousTokens.poll();
			}

			if (followingIterator.hasNext()) {
				TaggedToken next = followingIterator.next();
				followingTaggedTokens.add(next);
				followingTokens.add(next.getToken());
			} else {
				followingTokens.add(margin);
			}
		}

		return windows;
	}

	@Override
	public String toString() {
		return this.taggedTokens.stream()
				.collect(() -> new StringBuilder(this.sentence),
						(sb, tt) -> sb.append('\n')
								.append("[tag=").append(tt.getTag())
								.append(", token=").append(tt.getToken())
								.append("]"),
						StringBuilder::append)
				.toString();
	}
}
