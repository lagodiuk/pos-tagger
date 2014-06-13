package com.lahodiuk.postagger;

import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TestWeka {
	public static void main(String[] args) throws Exception {
		Attribute currentWordGraphemes = new Attribute("currentWordGraphemes", (FastVector) null);
		Attribute previousWordGraphemes = new Attribute("previousWordGraphemes", (FastVector) null);
		Attribute followingWordGraphemes = new Attribute("followingWordGraphemes", (FastVector) null);
		Attribute currentLength = new Attribute("currentLength");

		FastVector tagValues = new FastVector(2);
		tagValues.addElement("NOUN");
		tagValues.addElement("VERB");
		Attribute tag = new Attribute("tag", tagValues);

		FastVector attributes = new FastVector(5);
		attributes.addElement(currentWordGraphemes);
		attributes.addElement(previousWordGraphemes);
		attributes.addElement(followingWordGraphemes);
		attributes.addElement(currentLength);
		attributes.addElement(tag);

		Instances instances = new Instances("", attributes, 1);
		instances.setClass(tag);

		StringToWordVector currentFilter = new StringToWordVector();
		currentFilter.setAttributeIndices("first");
		currentFilter.setAttributeNamePrefix("current_");
		currentFilter.setOutputWordCounts(false);

		StringToWordVector previousFilter = new StringToWordVector();
		previousFilter.setAttributeIndices("first-1");
		previousFilter.setAttributeNamePrefix("previous_");
		previousFilter.setOutputWordCounts(false);

		StringToWordVector followingFilter = new StringToWordVector();
		followingFilter.setAttributeIndices("first-2");
		followingFilter.setAttributeNamePrefix("following_");
		followingFilter.setOutputWordCounts(false);

		MultiFilter multiFilter = new MultiFilter();
		multiFilter.setInputFormat(instances);
		multiFilter.setFilters(new Filter[]{currentFilter, previousFilter, followingFilter});

		Instance inst = new Instance(5);
		inst.setValue(currentWordGraphemes, "a b c aa bb");
		inst.setValue(previousWordGraphemes, "d e f");
		inst.setValue(followingWordGraphemes, "g h i");
		inst.setValue(currentLength, 5);
		inst.setValue(tag, "VERB");
		inst.setDataset(instances);
		instances.add(inst);

		Instances filtered = Filter.useFilter(instances, multiFilter);
		@SuppressWarnings("unchecked")
		Enumeration<Instance> iter = filtered.enumerateInstances();
		while (iter.hasMoreElements()) {
			Instance i = iter.nextElement();
			@SuppressWarnings("unchecked")
			Enumeration<Attribute> attr = i.enumerateAttributes();
			while (attr.hasMoreElements()) {
				Attribute at = attr.nextElement();
				if (at.isString()) {
					System.out.println(at + "\t" + i.stringValue(at));
				}
				else if (at.isNumeric()) {
					System.out.println(at + "\t" + i.value(at));
				}
			}
			System.out.println("Class attribute:");
			System.out.println(i.classAttribute() + "\t" + i.stringValue(i.classAttribute()));
			System.out.println();
		}
	}
}
