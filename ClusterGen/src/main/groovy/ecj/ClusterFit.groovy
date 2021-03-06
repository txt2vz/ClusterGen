package ecj;

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

	def averageF1

	IndexSearcher searcher = IndexInfo.instance.indexSearcher;

	private String queryShort (){
		def s=""
		queryMap.keySet().eachWithIndex {q, index ->
			if (index>0) s+='\n';
			s +=  "Cluster " + index + ": " + queryMap.get(q) + " " + q.toString(IndexInfo.FIELD_CONTENTS)
		}
		return s
	}

	private int getTotalHits(){

		int hitsPerPage=Integer.MAX_VALUE

		def docsReturned =	queryMap.keySet().inject([] as Set) {docSet, q ->

			TopDocs topDocs = searcher.search(q, hitsPerPage)
			ScoreDoc[] hits = topDocs.scoreDocs;
			hits.each {h -> docSet << h.doc				 }
			docSet
		}
		return docsReturned.size()
	}

	public void queryStats (int job, int gen, int popSize){
		def messageOut=""
		int hitsPerPage=10000
		FileWriter resOut = new FileWriter("results/clusterResultsF1.txt", true)
		resOut <<"  ***** Job: $job Gen: $gen PopSize: $popSize Noclusters:" + IndexInfo.instance.NUMBER_OF_CLUSTERS + " pathToIndex: " + IndexInfo.instance.pathToIndex + " **************************************************************** \n "

		def f1list = []
		queryMap.keySet().eachWithIndex {q, index ->

			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			def qString = q.toString(IndexInfo.FIELD_CONTENTS)

			println "***********************************************************************************"
			messageOut = "Cluster $index searching for:  $qString  Found ${hits.length} hits:" + '\n'
			println messageOut
			resOut << messageOut

			//map of categories (ground truth) and their frequencies
			def catsFreq=[:]
			hits.eachWithIndex{ h, i ->
				int docId = h.doc;
				def scr = h.score
				Document d = searcher.doc(docId);
				def cat = d.get(IndexInfo.FIELD_CATEGORY_NAME)
				def n = catsFreq.get((cat)) ?: 0
				catsFreq.put((cat), n + 1)

				if (i <5){
					messageOut = "$i path " + d.get(IndexInfo.FIELD_PATH)	+ " cat number $cat catName: " + d.get(IndexInfo.FIELD_CATEGORY_NAME)
					println messageOut
					resOut << messageOut + '\n'
				}
			}
			println "Gen: $gen Cluster: $index catsFreq: $catsFreq for query: $qString "

			//find the category with maximimum returned docs for this query
			def catMax = catsFreq?.max{it?.value} ?:0

			println "catsFreq: $catsFreq cats max: $catMax "

			//purity measure - check this is correct?
			def purity = (hits.size()==0) ? 0 : (1 / hits.size())  * catMax.value
			println "purity:  $purity"

			if (catMax !=0){
				TotalHitCountCollector thcollector  = new TotalHitCountCollector();
				final TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY_NAME,
						catMax.key));
				searcher.search(catQ, thcollector);
				def categoryTotal = thcollector.getTotalHits();
				messageOut = "categoryTotal is $categoryTotal for catQ $catQ \n"
				println messageOut
				resOut << messageOut

				def recall = catMax.value / categoryTotal;
				def precision = catMax.value / hits.size()
				def f1 = (2 * precision * recall) / (precision + recall);
				f1list << f1
				messageOut = "f1: $f1 recall: $recall precision: $precision"
				println messageOut
				resOut << messageOut + "\n"
				resOut << "Purity: $purity Job: $job \n"
			}
		}
		averageF1 = f1list.sum()/f1list.size()

		messageOut = "f1list: $f1list averagef1: :$averageF1"
		println messageOut
		resOut << messageOut + "\n"

		resOut << "PosHits: $posHits NegHits: $negHits PosScore: $positiveScore NegScore: $negativeScore Fitness: ${fitness()} \n"
		resOut << "TotalHits: " + getTotalHits() + " Total Docs: " + IndexInfo.instance.indexReader.maxDoc() + "\n"
		resOut << "************************************************ \n \n"

		resOut.flush()
		resOut.close()

		boolean appnd = true //job!=1
		FileWriter f = new FileWriter("results/resultsCluster.csv", appnd)
		Formatter csvOut = new Formatter(f);
		if (!appnd){
			final String fileHead = "gen, job, popSize, fitness, averageF1, query" + '\n';
			csvOut.format("%s", fileHead)
		}
		csvOut.format(
				"%s, %s, %s, %.3f, %.3f, %s",
				gen,
				job,
				popSize,
				fitness(),
				averageF1,
				queryForCSV(job) );

		csvOut.flush();
		csvOut.close()
	}

	private String queryForCSV (int job){
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