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

		int firstObservedIndex = 0;

		double[] firstObserved = observed.get(firstObservedIndex);
		for (int stateIndex = 0; stateIndex < statesCount; stateIndex++) {
			double stateProbability = firstObserved[stateIndex];
			String state = states[stateIndex];
			double stateStartProbability = transitionProbability.start(state);

			double logStartProbability = this.log(stateProbability) + this.log(stateStartProbability);
			matrix[stateIndex][firstObservedIndex].setValue(logStartProbability);
		}

		for (int observedIndex = firstObservedIndex + 1; observedIndex < observedLength; observedIndex++) {

			double[] currentObserved = observed.get(observedIndex);

			for (int currStateIndex = 0; currStateIndex < statesCount; currStateIndex++) {

				String currentState = states[currStateIndex];

				Cell bestParent = null;
				double bestParentLogProbability = Double.NEGATIVE_INFINITY;

				for (int prevStateIndex = 0; prevStateIndex < statesCount; prevStateIndex++) {

					String parentState = states[prevStateIndex];
					double previousToCurrentTransitionProbability = transitionProbability.transition(parentState, currentState);

					Cell currentParent = matrix[prevStateIndex][observedIndex - 1];
					double currentParentLogProbability = currentParent.getValue() + this.log(previousToCurrentTransitionProbability);

					if (currentParentLogProbability > bestParentLogProbability) {
						bestParentLogProbability = currentParentLogProbability;
						bestParent = currentParent;
					}
				}

				Cell current = matrix[currStateIndex][observedIndex];
				double currentStateProbability = currentObserved[currStateIndex];
				double currentLogProbability = bestParentLogProbability + this.log(currentStateProbability);

				current.setParent(bestParent);
				current.setValue(currentLogProbability);
			}
		}

		int lastObservedIndex = observedLength - 1;

		for (int stateIndex = 0; stateIndex < statesCount; stateIndex++) {
			String state = states[stateIndex];
			double stateEndProbability = transitionProbability.end(state);

			Cell cell = matrix[stateIndex][lastObservedIndex];
			double logEndProbability = cell.getValue() + this.log(stateEndProbability);

			cell.setValue(logEndProbability);
		}

		Cell lastCell = matrix[0][lastObservedIndex];
		for (int lastStateIndex = 1; lastStateIndex < statesCount; lastStateIndex++) {
			Cell cell = matrix[lastStateIndex][lastObservedIndex];

			if (cell.getValue() > lastCell.getValue()) {
				lastCell = cell;
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
		for (int stateIndex = 0; stateIndex < statesCount; stateIndex++) {
			for (int observedIndex = 0; observedIndex < observedLength; observedIndex++) {
				String state = states[stateIndex];
				matrix[stateIndex][observedIndex] = new Cell(state);
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
