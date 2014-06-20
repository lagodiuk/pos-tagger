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
		int statesCount = states.length;
		int observedLength = observed.size();

		Cell[][] matrix = this.createMatrix(states, statesCount, observedLength);

		int firstColumnIndex = 0;
		double[] firstObserved = observed.get(firstColumnIndex);
		for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
			matrix[rowIndex][firstColumnIndex].setValue(this.log(firstObserved[rowIndex]) + this.log(transitionProbability.start(states[rowIndex])));
		}

		for (int columnIndex = firstColumnIndex + 1; columnIndex < observedLength; columnIndex++) {
			for (int currStateIndex = 0; currStateIndex < statesCount; currStateIndex++) {
				Cell parent = matrix[0][columnIndex - 1];
				double parentProbability = parent.getValue()
						+ this.log(transitionProbability.transition(states[0], states[currStateIndex]));
				for (int prevStateIndex = 1; prevStateIndex < statesCount; prevStateIndex++) {
					double tmp = matrix[prevStateIndex][columnIndex - 1].getValue()
							+ this.log(transitionProbability.transition(states[prevStateIndex], states[currStateIndex]));
					if (tmp > parentProbability) {
						parentProbability = tmp;
						parent = matrix[prevStateIndex][columnIndex - 1];
					}
				}

				matrix[currStateIndex][columnIndex].setParent(parent);
				matrix[currStateIndex][columnIndex].setValue(parentProbability + this.log(observed.get(columnIndex)[currStateIndex]));
			}
		}

		int lastColumnIndex = observedLength - 1;
		for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
			matrix[rowIndex][lastColumnIndex].setValue(matrix[rowIndex][lastColumnIndex].getValue() + this.log(transitionProbability.end(states[rowIndex])));
		}

		Cell lastCell = matrix[0][lastColumnIndex];
		for (int lastStateIndex = 1; lastStateIndex < statesCount; lastStateIndex++) {
			if (matrix[lastStateIndex][lastColumnIndex].getValue() > lastCell.getValue()) {
				lastCell = matrix[lastStateIndex][lastColumnIndex];
			}
		}

		Stack<String> path = this.backtrace(lastCell);
		return path;
	}

	private Stack<String> backtrace(Cell lastCell) {
		Stack<String> path = new Stack<>();

		while (lastCell != null) {
			path.push(lastCell.getState());
			lastCell = lastCell.getParent();
		}
		return path;
	}

	private Cell[][] createMatrix(String[] states, int statesCount, int observedLength) {
		Cell[][] matrix = new Cell[statesCount][observedLength];
		for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
			for (int columnIndex = 0; columnIndex < observedLength; columnIndex++) {
				matrix[rowIndex][columnIndex] = new Cell(states[rowIndex]);
			}
		}
		return matrix;
	}

	private double log(double x) {
		return Math.log(x + 1e-5);
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
