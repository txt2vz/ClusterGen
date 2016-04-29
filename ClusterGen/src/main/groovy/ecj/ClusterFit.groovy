package ecj;

//import org.apache.lucene.search.Query
import ec.simple.SimpleFitness
import lucene.IndexInfo

import java.io.FileWriter
import java.util.Formatter;

import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.TotalHitCountCollector

/**
 * Store cluster information
 * 
 * @author Laurie 
 */

public class ClusterFit extends SimpleFitness {

	def queryMap = [:]
	def positiveScore=0 as float
	def negativeScore=0 as float
	def posHits=0
	def negHits=0
	def duplicateCount=0;
	def noHitsCount=0;
	def coreClusterPenalty=0
	def treePenalty=0;
	def graphPenalty=0
	def scoreOnly=0 as float
	def scoreOrig =0 as float
	def totalHits=0
	def fraction = 0 as float
	def baseFitness = 0 as float
	def missedDocs =0
	def emptyPen =0
	Formatter bestResultsOut

	IndexSearcher searcher = IndexInfo.instance.indexSearcher;

	public String queryShort (){
		def s=""
		queryMap.keySet().eachWithIndex {q, index ->
			if (index>0) s+='\n';
			s +=  "Cluster " + index + ": " + queryMap.get(q) + " " + q.toString(IndexInfo.FIELD_CONTENTS)
		}
		return s
	}

	public int getTotalHits(){

		int hitsPerPage=5000
		def docsReturned =[] as Set

		queryMap.keySet().each {q ->

			TopDocs topDocs = searcher.search(q, hitsPerPage)
			ScoreDoc[] hits = topDocs.scoreDocs;
			hits.each {h -> docsReturned << h.doc }
		}
		return docsReturned.size()
	}

	public void queryTest (int job){
		def s=""
		int hitsPerPage=5000
		FileWriter resOut = new FileWriter("results/clusterPurity.txt", true)
		resOut <<"Job $job **************************************************************** \n "

		def f1list = []
		queryMap.keySet().eachWithIndex {q, index ->

			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			def qString = q.toString(IndexInfo.FIELD_CONTENTS)

			println "***********************************************************************************"
			println "Searching for:  $qString  Found ${hits.length} hits:"
			resOut << "Cluster $index searching for:  $qString  Found ${hits.length} hits:" + '\n'

			def catsFreq=[:]
			hits.eachWithIndex{ h, i ->
				int docId = h.doc;
				def scr = h.score
				Document d = searcher.doc(docId);
				def cat = d.get(IndexInfo.FIELD_CATEGORY_NAME)

				def n = catsFreq.get((cat)) ?: 0
				catsFreq.put((cat), n + 1)
				if (i <5){
					def res = "$i path " + d.get(IndexInfo.FIELD_PATH)	+ " cat number $cat catName: " + d.get(IndexInfo.FIELD_CATEGORY_NAME)
					println res
					resOut << res + '\n'
				}
			}
			println "Cluster: $index catsFreq: $catsFreq for query: $qString "
			//def catMax = catsFreq.max { a, b ->  a.value <=> b.value  }.value
			def catMax = catsFreq?.max{it?.value} ?:0
			println "catsFreq $catsFreq"
			println " cats max: $catMax "
			def purity = (hits.size()==0) ? 0 : (1 / hits.size())  * catMax.value
			println "purity:  $purity"

			if (catMax !=0){
				TotalHitCountCollector thcollector  = new TotalHitCountCollector();
				final TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY_NAME,
						catMax.key));
				searcher.search(catQ, thcollector);
				def categoryTotal = thcollector.getTotalHits();
				s = "categoryTotal is $categoryTotal for catQ $catQ \n"
				println s
				resOut << s

				def recall = catMax.value / categoryTotal;
				def precision = catMax.value / hits.size()
				def f1 = (2 * precision * recall) / (precision + recall);
				f1list << f1
				def out = "f1: $f1 recall: $recall precision: $precision"
				println out
				resOut << out + "\n"
				resOut << "Purity: $purity Job: $job \n"
			}
		}
		def averagef1 = f1list.sum()/f1list.size()

		def o = "f1list: $f1list averagef1: :$averagef1"
		println o
		resOut << o + "\n"

		resOut << "PosHits: $posHits NegHits: $negHits PosScore: $positiveScore NegScore: $negativeScore Fitness: ${fitness()} \n "
		resOut << "TotalHits: " + getTotalHits() + " Total Docs: " + IndexInfo.instance.indexReader.maxDoc() + "\n"
		resOut << "************************************************ \n \n"

		resOut.flush()
		resOut.close()
	}

	public void saveFinalResults(int job){
		boolean appnd = job!=1
		FileWriter f = new FileWriter("results/resultsCluster.csv", appnd)
		Formatter resultsOut = new Formatter(f);
		if (!appnd){
			final String fileHead = "job, fitness, posHits, negHits, posScore, negScore, query" + '\n';
			resultsOut.format("%s", fileHead)
		}
		resultsOut.format(
				"%s, %.3f, %d, %d, %.3f, %.3f, %s",
				job,
				fitness(),
				posHits,
				negHits,
				positiveScore as float,
				negativeScore as float,
				queryForCSV(job) );

		resultsOut.flush();
		resultsOut.close()
	}

	public String queryForCSV (int job){
		def s="Job: $job "
		queryMap.keySet().eachWithIndex {q, index ->
			s += "Cluster " + index + ": " + queryMap.get(q) + " " + q.toString(IndexInfo.FIELD_CONTENTS) + " ## "
		}
		return s + '\n'
	}

	public String fitnessToStringForHumans() {
		//def origFit = this.fitness() -400
		return  "Cluster Fitness: " + this.fitness() + " scoreOrig: $scoreOrig";
	}

	public String toString(int gen) {
		return "Gen: " + gen + " Cluster Fitness: " + this.fitness
		+ " qMap: " + queryMap;
	}
}
