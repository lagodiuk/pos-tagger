package com.lahodiuk.postagger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sentence {

	public static void main(String[] args) {
		Sentence sentence = new Sentence("В истории программы это уже не первый «ребрендинг».");
		System.out.println(sentence.getSentence());
		for (String s : sentence.getTokens()) {
			System.out.println(s);
		}
		for (WordWindow ww : sentence.getWordWindows(2, "_")) {
			System.out.println(ww);
		}
	}

	private String sentence;

	private List<String> tokens;

	public Sentence(String sentence) {
		this.sentence = sentence;

		this.tokens = new ArrayList<>();
		Pattern pattern = Pattern.compile("(\\d+|\\p{L}+|[^\\s])");
		Matcher matcher = pattern.matcher(sentence);
		while (matcher.find()) {
			this.tokens.add(matcher.group());
		}
	}

	public List<String> getTokens() {
		return this.tokens;
	}

	public String getSentence() {
		return this.sentence;
	}

	public List<WordWindow> getWordWindows(int neighboursCount, String margin) {
		List<WordWindow> wordWindows = new ArrayList<>();

		Queue<String> previousTokens = new ArrayDeque<>(neighboursCount);
		for (int i = 0; i < neighboursCount; i++) {
			previousTokens.add(margin);
		}

		Queue<String> followingTokens = new ArrayDeque<>(neighboursCount);
		Iterator<String> followingTokensIterator = this.tokens.iterator();
		if (followingTokensIterator.hasNext()) {
			// skip first token
			followingTokensIterator.next();
		}
		for (int i = 0; i < neighboursCount; i++) {
			if (followingTokensIterator.hasNext()) {
				followingTokens.add(followingTokensIterator.next());
			} else {
				followingTokens.add(margin);
			}
		}

		for (String token : this.tokens) {
			WordWindow ww = new WordWindow();
			ww.setPreviousTokens(previousTokens);
			ww.setCurrentToken(token);
			ww.setFollowingTokens(followingTokens);

			wordWindows.add(ww);

			previousTokens.poll();
			previousTokens.add(token);

			followingTokens.poll();
			if (followingTokensIterator.hasNext()) {
				followingTokens.add(followingTokensIterator.next());
			} else {
				followingTokens.add(margin);
			}
		}

		return wordWindows;
	}
}
