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
				new TransitionProbability() {
					@Override
					public double transition(String fromState, String toState) {
						return fromState.equals(toState) ? 0.8 : 0.2;
					}

					@Override
					public double start(String state) {
						return 1;
					}

					@Override
					public double end(String state) {
						return 1;
					}
				});

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
			matrix[rowIndex][firstColumnIndex].setValue(Math.log(firstObserved[rowIndex] + 1e-5) + Math.log(transitionProbability.start(states[rowIndex]) + 1e-5));
		}

		for (int columnIndex = 1; columnIndex < observed.size(); columnIndex++) {
			for (int currStateIndex = 0; currStateIndex < states.length; currStateIndex++) {
				Cell parent = matrix[0][columnIndex - 1];
				double parentProbability = parent.getValue()
						+ Math.log(transitionProbability.transition(states[0], states[currStateIndex]) + 1e-5);
				for (int prevStateIndex = 1; prevStateIndex < states.length; prevStateIndex++) {
					double tmp = matrix[prevStateIndex][columnIndex - 1].getValue()
							+ Math.log(transitionProbability.transition(states[prevStateIndex], states[currStateIndex]) + 1e-5);
					if (tmp > parentProbability) {
						parentProbability = tmp;
						parent = matrix[prevStateIndex][columnIndex - 1];
					}
				}

				matrix[currStateIndex][columnIndex].setParent(parent);
				matrix[currStateIndex][columnIndex].setValue(parentProbability + Math.log(observed.get(columnIndex)[currStateIndex] + 1e-5));
			}
		}

		int lastColumnIndex = observed.size() - 1;
		for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
			matrix[rowIndex][lastColumnIndex].setValue(matrix[rowIndex][lastColumnIndex].getValue() + Math.log(transitionProbability.end(states[rowIndex]) + 1e-5));
		}

		Cell lastCell = matrix[0][lastColumnIndex];
		for (int lastStateIndex = 1; lastStateIndex < states.length; lastStateIndex++) {
			if (matrix[lastStateIndex][lastColumnIndex].getValue() > lastCell.getValue()) {
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
		private double value;

		public Cell(String state) {
			this.state = state;
		}

		public void setParent(Cell parent) {
			this.parent = parent;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public Cell getParent() {
			return this.parent;
		}

		public double getValue() {
			return this.value;
		}

		public String getState() {
			return this.state;
		}
	}

	public static interface TransitionProbability {
		double transition(String fromState, String toState);

		double start(String state);

		double end(String state);
	}
}
