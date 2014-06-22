package com.lahodiuk.postagger;

import java.util.ArrayList;
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

	public HMMTagger(List<TaggedSentence> taggedSentences) {
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

		for (TaggedSentence ts : taggedSentences) {
			Tag previousTag = null;

			for (TaggedToken tt : ts.getTaggedTokens()) {
				Tag currentTag = tt.getTag();

				if (previousTag == null) {
					double count = this.tagStart.get(currentTag);
					this.tagStart.put(currentTag, count + 1);

					previousTag = currentTag;
					continue;
				}

				double count = this.tag2tagCount.get(previousTag).get(currentTag);
				this.tag2tagCount.get(previousTag).put(currentTag, count + 1);
				previousTag = currentTag;
			}

			double count = this.tagEnd.get(previousTag);
			this.tagEnd.put(previousTag, count + 1);
		}

		this.normalize(this.tagStart);
		this.normalize(this.tagEnd);
		for (Tag previousTag : this.tag2tagCount.keySet()) {
			this.normalize(this.tag2tagCount.get(previousTag));
		}

		System.out.println(this.tagStart);
		System.out.println(this.tagEnd);
		System.out.println(this.tag2tagCount);
	}

	private void normalize(Map<Tag, Double> map) {
		double sum = 0.0;
		for (Double v : map.values()) {
			sum += v;
		}
		for (Tag key : map.keySet()) {
			double normalized = map.get(key) / sum;
			map.put(key, normalized);
		}
	}

	public void inference(List<ClassifiedToken> classifiedTokens) {
		String[] states = new String[TAGS.length];
		int i = 0;
		for (Tag tag : TAGS) {
			states[i++] = tag.name();
		}

		List<double[]> items = new ArrayList<>();
		for (ClassifiedToken ct : classifiedTokens) {
			double[] item = new double[TAGS.length];
			int j = 0;
			for (Tag tag : TAGS) {
				item[j++] = ct.getProbability(tag);
			}
			items.add(item);
		}

		Iterable<String> tags = new ViterbiAlgorithm().getMostProbablePath(states, items, this.getTransitionProbability());
		Iterator<ClassifiedToken> ctIterator = classifiedTokens.iterator();
		for (String tag : tags) {
			System.out.print(ctIterator.next().getToken() + "(" + tag + ")" + " ");
		}
	}

	public TransitionProbability getTransitionProbability() {
		return new TransitionProbability() {

			@Override
			public double transition(String fromState, String toState) {
				return HMMTagger.this.tag2tagCount.get(Tag.valueOf(fromState)).get(Tag.valueOf(toState));
			}

			@Override
			public double start(String state) {
				return HMMTagger.this.tagStart.get(Tag.valueOf(state));
				// return 1;
			}

			@Override
			public double end(String state) {
				return HMMTagger.this.tagEnd.get(Tag.valueOf(state));
				// return 1;
			}
		};
	}
}