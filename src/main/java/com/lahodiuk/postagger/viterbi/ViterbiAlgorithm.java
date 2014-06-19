package com.lahodiuk.postagger.viterbi;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ViterbiAlgorithm {

	public static void main(String[] args) {
		Iterable<String> path = new ViterbiAlgorithm().getMostProbablePath(
				new String[] { "1", "0" },
				Arrays.asList(
						new double[] { 0.6, 0.5 },
						new double[] { 0.6, 0.81 },
						new double[] { 0.4, 0.5 },
						new double[] { 0.7, 0.5 }),
				(s1, s2) -> (s1.equals(s2)) ? 0.8 : 0.2);

		for (String s : path) {
			System.out.println(s);
		}
	}

	public Iterable<String> getMostProbablePath(String[] states, List<double[]> observed, TransitionProbability transitionProbability) {
		Cell[][] matrix = new Cell[states.length][observed.size()];
		for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
			for (int columnIndex = 0; columnIndex < observed.size(); columnIndex++) {
				matrix[rowIndex][columnIndex] = new Cell(states[rowIndex]);
			}
		}

		int firstColumnIndex = 0;
		double[] firstObserved = observed.get(firstColumnIndex);
		for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
			matrix[rowIndex][firstColumnIndex].setProbability(firstObserved[rowIndex]);
		}

		for (int columnIndex = 1; columnIndex < observed.size(); columnIndex++) {
			for (int currStateIndex = 0; currStateIndex < states.length; currStateIndex++) {
				Cell parent = matrix[0][columnIndex - 1];
				double parentProbability = parent.getProbability() * transitionProbability.transition(states[0], states[currStateIndex]);
				for (int prevStateIndex = 1; prevStateIndex < states.length; prevStateIndex++) {
					double tmp = matrix[prevStateIndex][columnIndex - 1].getProbability() * transitionProbability.transition(states[prevStateIndex], states[currStateIndex]);
					if (tmp > parentProbability) {
						parentProbability = tmp;
						parent = matrix[prevStateIndex][columnIndex - 1];
					}
				}

				matrix[currStateIndex][columnIndex].setParent(parent);
				matrix[currStateIndex][columnIndex].setProbability(parentProbability * observed.get(columnIndex)[currStateIndex]);
			}
		}

		int lastColumnIndex = observed.size() - 1;
		Cell lastCell = matrix[0][lastColumnIndex];
		for (int lastStateIndex = 1; lastStateIndex < states.length; lastStateIndex++) {
			if (matrix[lastStateIndex][lastColumnIndex].getProbability() > lastCell.getProbability()) {
				lastCell = matrix[lastStateIndex][lastColumnIndex];
			}
		}

		Stack<String> path = new Stack<>();

		while (lastCell != null) {
			path.push(lastCell.getState());
			lastCell = lastCell.getParent();
		}

		return path;
	}

	private static class Cell {
		private String state;
		private Cell parent;
		private double probability;

		public Cell(String state) {
			this.state = state;
		}

		public void setParent(Cell parent) {
			this.parent = parent;
		}

		public void setProbability(double probability) {
			this.probability = probability;
		}

		public Cell getParent() {
			return this.parent;
		}

		public double getProbability() {
			return this.probability;
		}

		public String getState() {
			return this.state;
		}
	}

	public static interface TransitionProbability {
		double transition(String fromState, String toState);
	}
}
