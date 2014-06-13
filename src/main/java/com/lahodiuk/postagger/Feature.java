package com.lahodiuk.postagger;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public abstract class Feature {

	private Attribute attribute;

	public Feature(String featureName, FeatureType type) {
		switch (type) {
			case NUMERIC :
				this.attribute = new Attribute(featureName);
				break;

			case STRING :
				this.attribute = new Attribute(featureName, (FastVector) null);
				break;
		}
	}

	public abstract void addFeature(WordWindow window, Instance instance);

	public Attribute getAttribute() {
		return this.attribute;
	}

	public enum FeatureType {
		NUMERIC,
		STRING
	}
}
