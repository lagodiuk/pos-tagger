package com.lahodiuk.postagger;

/**
 * Taken from: http://opencorpora.org/dict.php?act=gram 
 */
public enum Tag {
	GRND("деепричастие"),
	ADJF("имя прилагательное (полное)"),
	PRTS("причастие (краткое)"),
	PRED("предикатив"),
	PREP("предлог"),
	NUMR("числительное"),
	PNCT("пунктуация"),
	PRCL("частица"),
	INFN("глагол (инфинитив)"),
	COMP("компаратив"),
	ADVB("наречие"),
	PRTF("причастие (полное)"),
	ADJS("имя прилагательное (краткое)"),
	VERB("глагол (личная форма)"),
	CONJ("союз"),
	NUMB("число"),
	INTJ("междометие"),
	NPRO("местоимение-существительное"),
	NOUN("имя существительное");
	
	private String russianName;
	
	private Tag(String russianName) {
		this.russianName = russianName;
	}
	
	public String getRussianName() {
		return russianName;
	}
}
