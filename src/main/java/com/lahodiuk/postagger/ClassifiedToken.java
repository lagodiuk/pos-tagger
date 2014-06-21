package com.lahodiuk.postagger;

import java.util.EnumMap;
import java.util.Map;

public class ClassifiedToken {

	private String token;

	private Map<Tag, Double> probability = new EnumMap<>(Tag.class);

	public ClassifiedToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}

	public void setProbability(Tag tag, double p) {
		this.probability.put(tag, p);
	}

	public double getProbability(Tag tag) {
		return this.probability.get(tag);
	}
}
