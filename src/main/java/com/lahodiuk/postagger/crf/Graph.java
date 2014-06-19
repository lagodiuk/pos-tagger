package com.lahodiuk.postagger.crf;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.lahodiuk.postagger.Tag;

public class Graph {

	private Map<Integer, Node> nodes = new LinkedHashMap<>();

	public void add(Node node) {
		this.nodes.put(node.getId(), node);
	}

	public void addEdge(int id1, int id2) {
		Node node1 = this.nodes.get(id1);
		Node node2 = this.nodes.get(id2);
		Edge edge = new Edge(node1, node2);
		node1.addEdge(edge);
		node2.addEdge(edge);
	}

	public void doInference() {
		for (int i = 0; i < 10; i++) {
			for (Node n : this.nodes.values()) {
				n.updateMessages();
			}
		}
	}

	public Collection<Node> getNodes() {
		return this.nodes.values();
	}

	public static void main(String[] args) {
		Graph g = new Graph();

		int nodesCount = 5;

		for (int i = 0; i < nodesCount; i++) {
			g.add(new Node(i));
		}

		for (int i = 0; i < (nodesCount - 1); i++) {
			g.addEdge(i, i + 1);
		}

		g.doInference();
		for (Node n : g.getNodes()) {
			Map<Tag, Double> posteriorProbabilities = n.calculatePosterioirTagsProbability();
			Tag t = null;
			double pr = 0;
			for (Tag key : posteriorProbabilities.keySet()) {
				if (posteriorProbabilities.get(key) > pr) {
					pr = posteriorProbabilities.get(key);
					t = key;
				}
			}
			System.out.println(t);
		}
	}
}
