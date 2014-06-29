package com.lahodiuk.postagger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class XMLCorpusReader {

	public static void main(String[] args) throws Exception {
		String xmlCorpusPath = "/Users/yura/workspaces/pos-tagger/src/main/resources/annot.opcorpora.no_ambig.xml";

		List<TaggedSentence> taggedSentences = getTaggedSentences(xmlCorpusPath, 2);

		for (TaggedSentence ts : taggedSentences) {
			System.out.println(ts);
			List<TaggedWordWindow> twws = ts.getTaggedWordWindows(2, "_");
			for (TaggedWordWindow tww : twws) {
				System.out.println(tww);
			}
			System.out.println();
		}
	}

	public static List<TaggedSentence> getTaggedSentences(String xmlCorpusPath, int minNumberOfTokens) throws XMLStreamException, FactoryConfigurationError,
			FileNotFoundException {
		List<TaggedSentence> taggedSentences = new ArrayList<>();

		XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(xmlCorpusPath));

		while (xmlEventReader.hasNext()) {
			XMLEvent event = xmlEventReader.nextEvent();
			if (event.isStartElement() && "source".equals(event.asStartElement().getName().getLocalPart())) {

				String sentence = extractSentence(xmlEventReader);

				List<StringTaggedToken> taggedTokens = extractTaggedTokens(xmlEventReader);

				if (!allTagsRecognized(taggedTokens) || (taggedTokens.size() < minNumberOfTokens)) {
					continue;
				}

				TaggedSentence taggedSentence = createTaggedSentence(sentence, taggedTokens);
				taggedSentences.add(taggedSentence);
			}
		}
		return taggedSentences;
	}

	private static TaggedSentence createTaggedSentence(String sentence, List<StringTaggedToken> taggedTokens) {
		TaggedSentence taggedSentence = new TaggedSentence();
		taggedSentence.setSentence(sentence);
		for (StringTaggedToken tt : taggedTokens) {
			taggedSentence.addTaggedToken(tt.getToken(), Tag.valueOf(tt.getTag()));
		}
		return taggedSentence;
	}

	private static boolean allTagsRecognized(List<StringTaggedToken> taggedTokens) {
		boolean allTagsRecognized = true;
		for (StringTaggedToken tt : taggedTokens) {
			try {
				Tag.valueOf(tt.getTag());
				allTagsRecognized = true;
			} catch (Exception ex) {
				allTagsRecognized = false;
				break;
			}
		}
		return allTagsRecognized;
	}

	private static List<StringTaggedToken> extractTaggedTokens(XMLEventReader xmlEventReader) throws XMLStreamException {
		List<StringTaggedToken> taggedTokens = new ArrayList<>();

		while (xmlEventReader.hasNext()) {
			XMLEvent e = xmlEventReader.nextEvent();
			if (e.isEndElement() && e.asEndElement().getName().getLocalPart().equals("sentence")) {
				break;
			}

			if (e.isStartElement() && e.asStartElement().getName().getLocalPart().equals("token")) {
				String token = e.asStartElement().getAttributeByName(new QName("text")).getValue();
				String tag = extractTag(xmlEventReader);
				taggedTokens.add(new StringTaggedToken(token, tag));
			}
		}
		return taggedTokens;
	}

	private static String extractTag(XMLEventReader xmlEventReader) throws XMLStreamException {
		String tag = null;

		while (xmlEventReader.hasNext()) {
			XMLEvent e = xmlEventReader.nextEvent();
			if (e.isEndElement() && e.asEndElement().getName().getLocalPart().equals("token")) {
				break;
			}

			if (e.isStartElement() && e.asStartElement().getName().getLocalPart().equals("g")) {
				tag = e.asStartElement().getAttributeByName(new QName("v")).getValue();
				if (tag.matches("\\p{Lu}+")) {
					break;
				}
			}
		}
		return tag;
	}

	private static String extractSentence(XMLEventReader xmlEventReader) throws XMLStreamException {
		String sentence = xmlEventReader.nextEvent().asCharacters().getData();
		while (xmlEventReader.hasNext()) {
			XMLEvent event = xmlEventReader.nextEvent();
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("source")) {
				break;
			}

			sentence += event.asCharacters().getData();
		}
		return sentence;
	}

	private static class StringTaggedToken {
		private String token;
		private String tag;

		public StringTaggedToken(String token, String tag) {
			this.tag = tag;
			this.token = token;
		}

		public String getTag() {
			return this.tag;
		}

		public String getToken() {
			return this.token;
		}
	}
}
