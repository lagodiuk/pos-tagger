package com.lahodiuk.postagger.crf;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.lahodiuk.postagger.Tag;

public class Node {

	private int id;

	private Map<Tag, Double> tagsPriorProbability = new EnumMap<>(Tag.class);

	private List<Edge> edges = new ArrayList<>();

	public Node(int id) {
		this.id = id;
		for (Tag t : Tag.values()) {
			this.tagsPriorProbability.put(t, Math.random());
			this.normalize(this.tagsPriorProbability);
		}
	}

	public Map<Tag, Double> calculatePosterioirTagsProbability() {
		Map<Tag, Double> result = this.getProductOfIncomingMessages();

		for (Tag tag : result.keySet()) {
			result.put(tag, result.get(tag) * this.tagsPriorProbability.get(tag));
		}

		this.normalize(result);

		return result;
	}

	public void updateMessages() {
		Map<Tag, Double> productIncomingMessages = this.getProductOfIncomingMessages();

		for (Edge edge : this.edges) {
			for (Tag targetTag : Tag.values()) {
				double message = 0;
				for (Tag sourceTag : Tag.values()) {
					message += (this.tagsPriorProbability.get(sourceTag) * edge.getCompatibility(sourceTag, targetTag) * productIncomingMessages.get(sourceTag))
							/ edge.getIncomingMessage(this, sourceTag);
				}
				edge.setOutcomingMessage(this, targetTag, message);
			}
			edge.normalizeOutcomingMessages(this);
		}
	}

	private Map<Tag, Double> getProductOfIncomingMessages() {
		Map<Tag, Double> result = new EnumMap<>(Tag.class);
		for (Tag tag : Tag.values()) {
			double product = 1.0;
			for (Edge edge : this.edges) {
				product *= edge.getIncomingMessage(this, tag);
			}
			result.put(tag, product);
		}
		return result;
	}

	private void normalize(Map<Tag, Double> map) {
		double sum = 0;
		for (Double val : map.values()) {
			sum += val;
		}
		for (Tag tag : map.keySet()) {
			map.put(tag, map.get(tag) / sum);
		}
	}

	public void setTagsPriorProbability(Map<Tag, Double> tagsPriorProbability) {
		this.tagsPriorProbability = tagsPriorProbability;
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

	public int getId() {
		return this.id;
	}
}
