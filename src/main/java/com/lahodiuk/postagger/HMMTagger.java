package com.lahodiuk.postagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lahodiuk.postagger.viterbi.ViterbiAlgorithm;
import com.lahodiuk.postagger.viterbi.ViterbiAlgorithm.TransitionProbability;

public class HMMTagger {

	private static final Tag[] TAGS = Tag.values();

	private Map<Tag, Map<Tag, Double>> tag2tagCount = new HashMap<>();

	private Map<Tag, Double> tagStart = new HashMap<>();

	private Map<Tag, Double> tagEnd = new HashMap<>();

	private POSTagger classiferBasedTagger;

	public HMMTagger(List<TaggedSentence> taggedSentences) throws Exception {
		for (Tag prevTag : TAGS) {
			this.tag2tagCount.put(prevTag, new HashMap<>());
			for (Tag currTag : TAGS) {
				this.tag2tagCount.get(prevTag).put(currTag, 1.0);
			}
		}

		for (Tag tag : TAGS) {
			this.tagStart.put(tag, 1.0);
		}

		for (Tag tag : TAGS) {
			this.tagEnd.put(tag, 1.0);
		}

		// for (TaggedSentence ts : taggedSentences) {
		// Tag previousTag = null;
		//
		// for (TaggedToken tt : ts.getTaggedTokens()) {
		// Tag currentTag = tt.getTag();
		//
		// if (previousTag == null) {
		// double count = this.tagStart.get(currentTag);
		// this.tagStart.put(currentTag, count + 1);
		//
		// previousTag = currentTag;
		// continue;
		// }
		//
		// double count = this.tag2tagCount.get(previousTag).get(currentTag);
		// this.tag2tagCount.get(previousTag).put(currentTag, count + 1);
		// previousTag = currentTag;
		// }
		//
		// double count = this.tagEnd.get(previousTag);
		// this.tagEnd.put(previousTag, count + 1);
		// }
		//
		// this.normalize(this.tagStart);
		// this.normalize(this.tagEnd);
		// for (Tag previousTag : this.tag2tagCount.keySet()) {
		// this.normalize(this.tag2tagCount.get(previousTag));
		// }
		//
		// System.out.println(this.tagStart);
		// System.out.println(this.tagEnd);
		// System.out.println(this.tag2tagCount);

		this.classiferBasedTagger = new POSTagger();
		this.classiferBasedTagger.train(taggedSentences);
	}

	// private void normalize(Map<Tag, Double> map) {
	// double sum = 0.0;
	// for (Double v : map.values()) {
	// sum += v;
	// }
	// for (Tag key : map.keySet()) {
	// double normalized = map.get(key) / sum;
	// map.put(key, normalized);
	// }
	// }

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
				// return
				// HMMTagger.this.tag2tagCount.get(Tag.valueOf(fromState)).get(Tag.valueOf(toState));
				try {
					this.ww.setPreviousTags(Arrays.asList(Tag.valueOf(fromState)));
					return HMMTagger.this.classiferBasedTagger.classifyWordWindow(this.ww).getProbability(Tag.valueOf(toState));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public double start(String state) {
				// return HMMTagger.this.tagStart.get(Tag.valueOf(state));
				// return 1;
				try {
					return HMMTagger.this.classiferBasedTagger.classifyWordWindow(this.ww).getProbability(Tag.valueOf(state));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public double end(String state) {
				// return HMMTagger.this.tagEnd.get(Tag.valueOf(state));
				return 1;
			}

			@Override
			public double emit(String state, Object observation) {
				// ClassifiedToken ct = (ClassifiedToken) observation;
				// return ct.getProbability(Tag.valueOf(state));
				return 1;
			}

			@Override
			public void setCurrentObservation(Object observation) {
				this.ww = (WordWindow) observation;
			}
		};
	}
}