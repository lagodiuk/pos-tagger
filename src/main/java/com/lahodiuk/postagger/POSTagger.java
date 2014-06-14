package com.lahodiuk.postagger;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.tokenizers.Tokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import com.lahodiuk.postagger.Feature.FeatureType;

public class POSTagger {

	public static void main(String[] args) throws Exception {
		// Corpus downloaded from: http://opencorpora.org/?page=downloads
		// (version without disambiguation)
		List<TaggedSentence> taggedSentences =
				XMLCorpusReader.getTaggedSentences("/Users/yura/workspaces/pos-tagger/src/main/resources/annot.opcorpora.no_ambig.xml", 2);

		new POSTagger().train(taggedSentences);
	}

	private static final Feature[] FEATURES = getFeatures();

	private static final int ATTRIBUTES_COUNT = FEATURES.length + 1;

	private static final Attribute TAG_ATTRIBUTE = getTagAttribute();

	private static final int WINDOW_NEIGHBOURS_NUMBER = 1;

	private Classifier classifier;

	public void train(List<TaggedSentence> taggedSentences) throws Exception {

		Collections.shuffle(taggedSentences, new Random(1));
		int toIndex = (taggedSentences.size() * 7) / 10;
		System.out.println(toIndex);
		List<TaggedSentence> trainingSet = taggedSentences.subList(0, toIndex);
		List<TaggedSentence> validationSet = taggedSentences.subList(toIndex + 1, taggedSentences.size());

		Instances trainingInstancesSet = this.createInstancesSet(trainingSet, "trainingSet");

		this.classifier = this.buildClassifier(trainingInstancesSet);

		System.out.println(this.classifier);

		Instances validationInstancesSet = this.createInstancesSet(validationSet, "validationSet");

		Evaluation eval = new Evaluation(trainingInstancesSet);
		eval.evaluateModel(this.classifier, validationInstancesSet);
		System.out.println(eval.toSummaryString());
		System.out.println(eval.toMatrixString());
	}

	private Classifier buildClassifier(Instances trainingInstancesSet) throws Exception {
		MultiFilter graphemesFilter = this.initializeFiltersForGraphemes(trainingInstancesSet);

		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(graphemesFilter);

		// SVM
		//
		SMO svm = new SMO();
		svm.setBuildLogisticModels(true);
		// // PolyKernel polyKernel = new PolyKernel();
		// // polyKernel.setExponent(2);
		// svm.setKernel(polyKernel);
		filteredClassifier.setClassifier(svm);

		// Naive Bayes
		//
		// filteredClassifier.setClassifier(new NaiveBayes());

		// Select 50 most informative attributes, after this - SVM
		//
		// AttributeSelectedClassifier attributeSelectionClassifier = new
		// AttributeSelectedClassifier();
		// attributeSelectionClassifier.setEvaluator(new
		// InfoGainAttributeEval());
		// Ranker ranker = new Ranker();
		// ranker.setNumToSelect(50);
		// attributeSelectionClassifier.setSearch(ranker);
		// attributeSelectionClassifier.setClassifier(filteredClassifier);
		// attributeSelectionClassifier.setClassifier(svm);
		// filteredClassifier.setClassifier(attributeSelectionClassifier);

		filteredClassifier.buildClassifier(trainingInstancesSet);

		return filteredClassifier;
	}

	private MultiFilter initializeFiltersForGraphemes(Instances trainingInstancesSet) throws Exception {
		StringToWordVector currentFilter = new StringToWordVector();
		currentFilter.setAttributeIndices("first");
		currentFilter.setAttributeNamePrefix("current_");
		currentFilter.setOutputWordCounts(false);
		currentFilter.setTokenizer(new SpaceTokenizer());

		StringToWordVector previousFilter = new StringToWordVector();
		previousFilter.setAttributeIndices("first-1");
		previousFilter.setAttributeNamePrefix("previous_");
		previousFilter.setOutputWordCounts(false);
		previousFilter.setTokenizer(new SpaceTokenizer());

		StringToWordVector followingFilter = new StringToWordVector();
		followingFilter.setAttributeIndices("first-2");
		followingFilter.setAttributeNamePrefix("following_");
		followingFilter.setOutputWordCounts(false);
		followingFilter.setTokenizer(new SpaceTokenizer());

		MultiFilter multiFilter = new MultiFilter();
		multiFilter.setInputFormat(trainingInstancesSet);
		multiFilter.setFilters(new Filter[]{currentFilter, previousFilter, followingFilter});
		return multiFilter;
	}

	private Instances createInstancesSet(List<TaggedSentence> trainingSet, String instancesSetName) {
		Instances trainingInstancesSet = this.getEmptyInstances(instancesSetName);
		for (TaggedSentence ts : trainingSet) {
			for (TaggedWordWindow tww : ts.getTaggedWordWindows(WINDOW_NEIGHBOURS_NUMBER, "")) {
				Instance i = this.wordWindowToInstance(tww.getWordWindow());
				i.setValue(TAG_ATTRIBUTE, tww.getTag().name());
				trainingInstancesSet.add(i);
				i.setDataset(trainingInstancesSet);

				// System.out.println(tww);
				// @SuppressWarnings("unchecked")
				// Enumeration<Attribute> attrEnum = i.enumerateAttributes();
				// while (attrEnum.hasMoreElements()) {
				// Attribute at = attrEnum.nextElement();
				// Object val = (at.isNumeric()) ? i.value(at) :
				// i.stringValue(at);
				// System.out.println(at.name() + "\t" + val);
				// }
				// System.out.println(i.classAttribute().name() + "\t" +
				// i.stringValue(i.classAttribute()));
				// System.out.println();
			}
		}
		return trainingInstancesSet;
	}

	private Instances getEmptyInstances(String datasetName) {
		FastVector attributes = new FastVector(ATTRIBUTES_COUNT);
		for (Feature feature : FEATURES) {
			attributes.addElement(feature.getAttribute());
		}
		attributes.addElement(TAG_ATTRIBUTE);;
		Instances instances = new Instances(datasetName, attributes, 1);
		instances.setClass(TAG_ATTRIBUTE);
		return instances;
	}

	private Instance wordWindowToInstance(WordWindow window) {
		Instance instance = new Instance(ATTRIBUTES_COUNT);
		for (Feature feature : FEATURES) {
			feature.addFeature(window, instance);
		}
		return instance;
	}

	private static Attribute getTagAttribute() {
		FastVector tagValues = new FastVector(Tag.values().length);
		for (Tag t : Tag.values()) {
			tagValues.addElement(t.name());
		}
		Attribute attr = new Attribute("tagAttribute", tagValues);
		return attr;
	}

	private static Feature[] getFeatures() {
		return new Feature[]{
				new Feature("currentTokenGraphemes", FeatureType.STRING) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						String currentToken = window.getCurrentToken();
						int currentTokenLength = currentToken.length();

						StringBuilder graphemes = new StringBuilder();

						for (int i = 1; i <= 4; i++) {
							if (currentTokenLength > (i - 1)) {
								String sufix = currentToken.substring(currentTokenLength - i, currentTokenLength).toLowerCase();
								graphemes.append(sufix).append("$").append(" ");
							}
						}

						if (graphemes.length() > 0) {
							graphemes.setLength(graphemes.length() - 1);
						}

						instance.setValue(this.getAttribute(), graphemes.toString());
					}
				},
				new Feature("previousTokenGraphemes", FeatureType.STRING) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						String previousToken = window.getPreviousTokens().get(0);
						int previousTokenLength = previousToken.length();

						StringBuilder graphemes = new StringBuilder();

						for (int i = 1; i <= 4; i++) {
							if (previousTokenLength > (i - 1)) {
								String sufix = previousToken.substring(previousTokenLength - i, previousTokenLength).toLowerCase();
								graphemes.append(sufix).append("$").append(" ");
							}
						}

						if (graphemes.length() > 0) {
							graphemes.setLength(graphemes.length() - 1);
						}

						instance.setValue(this.getAttribute(), graphemes.toString());
					}
				},
				new Feature("followingTokenGraphemes", FeatureType.STRING) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						String followingToken = window.getFollowingTokens().get(0);
						int followingTokenLength = followingToken.length();

						StringBuilder graphemes = new StringBuilder();

						for (int i = 1; i <= 4; i++) {
							if (followingTokenLength > (i - 1)) {
								String sufix = followingToken.substring(followingTokenLength - i, followingTokenLength).toLowerCase();
								graphemes.append(sufix).append("$").append(" ");
							}
						}

						if (graphemes.length() > 0) {
							graphemes.setLength(graphemes.length() - 1);
						}

						instance.setValue(this.getAttribute(), graphemes.toString());
					}
				},
				new Feature("currentTokenLength", FeatureType.NUMERIC) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						instance.setValue(this.getAttribute(), window.getCurrentToken().length());
					}
				},
				new Feature("previousTokenLength", FeatureType.NUMERIC) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						instance.setValue(this.getAttribute(), window.getPreviousTokens().get(0).length());
					}
				},
				new Feature("followingTokenLength", FeatureType.NUMERIC) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						instance.setValue(this.getAttribute(), window.getFollowingTokens().get(0).length());
					}
				},
				new Feature("currentTokenStartsUppercase", FeatureType.NUMERIC) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						String currentToken = window.getCurrentToken();
						if ((currentToken.length() > 0) && Character.isUpperCase(currentToken.charAt(0))) {
							instance.setValue(this.getAttribute(), 1);
						} else {
							instance.setValue(this.getAttribute(), 0);
						}
					}
				},
				new Feature("currentTokenContainsOnlyLetters", FeatureType.NUMERIC) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						String currentToken = window.getCurrentToken();
						if (currentToken.matches("\\p{L}+")) {
							instance.setValue(this.getAttribute(), 1);
						} else {
							instance.setValue(this.getAttribute(), 0);
						}
					}
				},
				new Feature("currentTokenContainsDigits", FeatureType.NUMERIC) {
					@Override
					public void addFeature(WordWindow window, Instance instance) {
						String currentToken = window.getCurrentToken();
						if (currentToken.matches(".*\\d+.*")) {
							instance.setValue(this.getAttribute(), 1);
						} else {
							instance.setValue(this.getAttribute(), 0);
						}
					}
				},
		};
	}
	
	private static final class SpaceTokenizer extends Tokenizer {
		private static final long serialVersionUID = 1L;
		
		private String[] tokens;
		private int position;
		
		@Override
		public void tokenize(String s) {
			tokens = s.split("\\s+");
			position = 0;
		}
		@Override
		public Object nextElement() {
			return tokens[position++];
		}
		@Override
		public boolean hasMoreElements() {
			return position < tokens.length;
		}
		@Override
		public String getRevision() {
			return null;
		}
		@Override
		public String globalInfo() {
			return null;
		}
	}
}