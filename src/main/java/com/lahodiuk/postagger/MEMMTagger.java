package com.lahodiuk.postagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.lahodiuk.postagger.viterbi.ViterbiAlgorithm;
import com.lahodiuk.postagger.viterbi.ViterbiAlgorithm.TransitionProbability;

/**
 * Maximum-entropy Markov Model Tagger
 */
public class MEMMTagger {

	private static final Tag[] TAGS = Tag.values();

	private POSTagger classiferBasedTagger;

	public MEMMTagger(List<TaggedSentence> taggedSentences) throws Exception {
		this.classiferBasedTagger = new POSTagger();
		this.classiferBasedTagger.train(taggedSentences);
	}

	public void inference(Sentence sentence) throws Exception {
		String[] states = new String[TAGS.length];
		int i = 0;
		for (Tag tag : TAGS) {
			states[i++] = tag.name();
		}

		List<WordWindow> wordWindows = sentence.getWordWindows(POSTagger.WINDOW_NEIGHBOURS_NUMBER, POSTagger.MARGIN);

		List<Object> items = new ArrayList<>();
		for (WordWindow ww : wordWindows) {
			items.add(ww);
		}

		Iterable<String> tags = new ViterbiAlgorithm().getMostProbablePath(states, items, this.getTransitionProbability());
		Iterator<WordWindow> wordWindowsIterator = wordWindows.iterator();
		for (String tag : tags) {
			System.out.print(wordWindowsIterator.next().getCurrentToken() + "(" + tag + ")" + " ");
		}
	}

	private TransitionProbability getTransitionProbability() {
		return new TransitionProbability() {

			private WordWindow ww;

			@Override
			public double transition(String fromState, String toState) {
				try {
					this.ww.setPreviousTags(Arrays.asList(Tag.valueOf(fromState)));
					return MEMMTagger.this.classiferBasedTagger.classifyWordWindow(this.ww).getProbability(Tag.valueOf(toState));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public double start(String state) {
				try {
					return MEMMTagger.this.classiferBasedTagger.classifyWordWindow(this.ww).getProbability(Tag.valueOf(state));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public double end(String state) {
				return 1;
			}

			@Override
			public double emit(String state, Object observation) {
				return 1;
			}

			@Override
			public void setCurrentObservation(Object observation) {
				this.ww = (WordWindow) observation;
			}
		};
	}
}