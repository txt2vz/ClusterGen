package lucene;

import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.PostingsEnum
import org.apache.lucene.index.Term
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.DocIdSetIterator
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.similarities.DefaultSimilarity
import org.apache.lucene.search.similarities.TFIDFSimilarity
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.util.BytesRef

/**
 * GAs return words by selecting form word lists provided by this
 * class. The words should be as far as possible in order of their likely
 * usefulness in query building
 * 
 * @author Laurie 
 */

public class ImportantWords {

	public final static int SPAN_FIRST_MAX_END = 300;
	private final static int MAX_WORDLIST_SIZE = 300;

	private final IndexSearcher indexSearcher = IndexInfo.instance.indexSearcher;
	private final IndexReader indexReader = IndexInfo.instance.indexReader;
	private Terms terms = MultiFields.getTerms(indexReader, IndexInfo.FIELD_CONTENTS)
	private TermsEnum termsEnum = terms.iterator();
	private int maxDoc = indexReader.maxDoc();
	private Set<String> stopSet = StopSet.getStopSetFromFile()

	static main (args){
		IndexInfo.instance.setIndex()
		def iw = new ImportantWords()
		iw.getTFIDFWordList()
	}

	public String[] getTFIDFWordList(){

		println "Important words terms.getDocCount: ${terms.getDocCount()}"

		def wordMap = [:]
		BytesRef text;
		while((text = termsEnum.next()) != null) {

			def word = text.utf8ToString()
			Term t = new Term(IndexInfo.FIELD_CONTENTS, word);

			char c = word.charAt(0)
			int df = indexSearcher.getIndexReader().docFreq(t)
			def dfFraction = ((double) df/maxDoc)

			if (
				df < 3		
			//	|| dfFraction > 0.3
			    || 
				stopSet.contains(t.text())
			//	|| t.text().contains("'")		
				|| t.text().length()<2
			//	|| !c.isLetter()
				//|| dfFraction < 0.005
			//	|| t.text().contains(".")
			)
				continue;

			long indexDf = indexReader.docFreq(t);
			int docCount = indexReader.numDocs()
			TFIDFSimilarity tfidfSim = new DefaultSimilarity()	//new ClassicSimilarity()  

			PostingsEnum docsEnum = termsEnum.postings(MultiFields.getTermDocsEnum(indexReader, IndexInfo.FIELD_CONTENTS, text ));
			double tfidfTotal=0

			if (docsEnum != null) {
				while (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
					double tfidf = tfidfSim.tf(docsEnum.freq()) * tfidfSim.idf(docCount, indexDf);
					tfidfTotal +=tfidf
				}
			}
			wordMap+= [(word) : tfidfTotal]
		}

		wordMap= wordMap.sort{a, b -> a.value <=> b.value}
		List wordList = wordMap.keySet().toList().take(MAX_WORDLIST_SIZE)

		println "tfidf map size: ${wordMap.size()}  wordlist size: ${wordList.size()}  wordlist: $wordList"

		return wordList.toArray();
	}

	/**
	 * Experimental : find fraction of documents single word query returns
	 */
	public String[] getFreqFractionWordList()
	throws IOException{

		final int numberOfClasses=3;

		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		println "Important words terms.size ${terms.size()}"

		BytesRef text;
		def wordMap = [:]
		def maxD = indexSearcher.getIndexReader().maxDoc()

		while((text = termsEnum.next()) != null) {

			def word = text.utf8ToString()
			final Term t = new Term(IndexInfo.FIELD_CONTENTS, word);

			if (word=="") continue

				char c = word.charAt(0)
			int df = indexSearcher.getIndexReader().docFreq(t)
			def dfFraction = ((double) df/maxD)

			if (
		//		dfFraction < 0.01 || dfFraction > 0.30
			//|| StopSet.stopSet.contains(t.text())
			//||
			 stopSet.contains(t.text())
//			||t.text().contains("'".toCharArray())
//			||t.text().contains(".".toCharArray())
			|| t.text().length()<2
			//||!c.isLetter())
			//|| stopSet.contains(t.text()))
			)
				continue;

			def x = (df - (maxD/numberOfClasses)).abs()
			wordMap += [(word):x ]
		}

		wordMap= wordMap.sort{a, b -> a.value <=> b.value}

		List wordList = wordMap.keySet().toList().take(MAX_WORDLIST_SIZE)
		println "fraction based wordMap:  $wordMap"
		println "map size: ${wordMap.size()}  List size is ${wordList.size()}  list is $wordList"

		return wordList.toArray();
	}


	public String[] getFreqWordList()
	throws IOException{

		final int numberOfClasses=3;
		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		println "Important words terms.size ${terms.size()}"

		BytesRef text;
		termsEnum = terms.iterator();

		def wordMap = [:]
		def maxD = indexSearcher.getIndexReader().maxDoc()

		while((text = termsEnum.next()) != null) {

			def word = text.utf8ToString()

			final Term t = new Term(IndexInfo.FIELD_CONTENTS, word);

			if ( stopSet.contains(t.text())
			||t.text().contains("'")
			||t.text().contains(".")
			|| t.text().length()<2
			//||!c.isLetter())
			//|| stopSet.contains(t.text()))
			)
				continue;

			int df = indexSearcher.getIndexReader().docFreq(t)
			long termFreq = indexReader.totalTermFreq(t)

			wordMap.put(word, df)
		}

		wordMap= wordMap.sort{a, b -> b.value <=> a.value}

		List wordList = wordMap.keySet().toList().take(MAX_WORDLIST_SIZE)
		println "frequency based wordMap:  $wordMap"
		println "map size: ${wordMap.size()}  List size is ${wordList.size()}  list is $wordList"

		return wordList.toArray();
	}
}