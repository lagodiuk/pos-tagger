package com.lahodiuk.postagger.crf;

import java.util.EnumMap;
import java.util.Map;

import com.lahodiuk.postagger.Tag;

public class Edge {

	private Node node1;

	private Node node2;

	private Map<Tag, Double> node1ToNode2Messages = new EnumMap<>(Tag.class);

	private Map<Tag, Double> node1ToNode2MessagesNew = new EnumMap<>(Tag.class);

	private Map<Tag, Double> node2ToNode1Messages = new EnumMap<>(Tag.class);

	private Map<Tag, Double> node2ToNode1MessagesNew = new EnumMap<>(Tag.class);

	public Edge(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
		for (Tag t : Tag.values()) {
			this.node1ToNode2Messages.put(t, 1.0);
			this.node1ToNode2MessagesNew.put(t, 1.0);
			this.node2ToNode1Messages.put(t, 1.0);
			this.node2ToNode1MessagesNew.put(t, 1.0);
		}
	}

	public double getIncomingMessage(Node source, Tag tag) {
		if (source == this.node1) {
			return this.node2ToNode1Messages.get(tag);
		}
		if (source == this.node2) {
			return this.node1ToNode2Messages.get(tag);
		}
		throw new UnsupportedOperationException();
	}

	public void setOutcomingMessage(Node source, Tag tag, double message) {
		if (source == this.node1) {
			this.node1ToNode2MessagesNew.put(tag, message);
			return;
		}
		if (source == this.node2) {
			this.node2ToNode1MessagesNew.put(tag, message);
			return;
		}
		throw new UnsupportedOperationException();
	}

	public double getCompatibility(Tag t1, Tag t2) {
		if (t1 != t2) {
			return 0.2;
		} else {
			return 0.8;
		}
	}

	public void normalizeOutcomingMessages(Node source) {
		if (source == this.node1) {
			this.normalize(this.node1ToNode2MessagesNew);
			this.node1ToNode2Messages = this.node1ToNode2MessagesNew;
			return;
		}
		if (source == this.node2) {
			this.normalize(this.node2ToNode1MessagesNew);
			this.node2ToNode1Messages = this.node2ToNode1MessagesNew;
			return;
		}
		throw new UnsupportedOperationException();
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
}
