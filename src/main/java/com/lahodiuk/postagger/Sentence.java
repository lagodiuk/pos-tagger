package com.lahodiuk.postagger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sentence {

	public static void main(String[] args) {
		Sentence sentence = new Sentence("В истории программы это уже не первый «ребрендинг».");
		System.out.println(sentence.getSentence());
		for (String s : sentence.getTokens()) {
			System.out.println(s);
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
}
