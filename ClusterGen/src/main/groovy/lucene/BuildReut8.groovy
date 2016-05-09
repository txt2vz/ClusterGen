  package lucene

import groovy.io.FileType
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.codecs.*
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

class BuildReut8 {
	// Create Lucene index in this directory
	def indexPath = 	"indexes/reut5"

	// Index files in this directory

	def docsPath =
	//"dataset/r8/r8"
	/C:\Users\Laurie\Dataset\r5/

	Path path = Paths.get(indexPath)
	Directory directory = FSDirectory.open(path)
	Analyzer analyzer = //new EnglishAnalyzer();
	new StandardAnalyzer();
	def catsFreq=[:]
	def docsSet = [] as Set

	static main(args) {
		def i = new BuildReut8()
		i.buildIndex()
	}

	def buildIndex() {
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		// Create a new index in the directory, removing any
		// previously indexed documents:
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(directory, iwc);

		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");
		def catNumber=0;

		new File(docsPath).eachDir {
			//	it.eachDir{
			it.eachFileRecurse(FileType.FILES) { file ->

				indexDocs(writer,file, catNumber)
			}
			//	}
			catNumber++;
		}

		Date end = new Date();
		println (end.getTime() - start.getTime() + " total milliseconds");
		println "Total docs: " + writer.maxDoc()


		IndexSearcher searcher = new IndexSearcher(writer.getReader());

		TotalHitCountCollector thcollector  = new TotalHitCountCollector();
		final TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY,	"gra"))
		searcher.search(catQ, thcollector);
		def categoryTotal = thcollector.getTotalHits();
		println "cateTotoal $categoryTotal"

		writer.close()
		println "End ***************************************************************"
	}

	//index the doc adding fields for path, category, test/train and contents
	def indexDocs(IndexWriter writer, File f, categoryNumber)
	throws IOException {

		def doc = new Document()

		Field pathField = new StringField(IndexInfo.FIELD_PATH, f.getPath(), Field.Store.YES);
		doc.add(pathField);

		//for classic3 dataset
		//def catName = f.getName().substring(0,4)

		def catName = f.getCanonicalPath().drop(49).take(3)
		//println "catName $catName"
		// /home/test/git/ClusterGA/ClusterGA/dataset/r8/r8/interest/0006418.txt
		//C:\Users\Laurie\Dataset\reuters-top10\04_grain

		def n = catsFreq.get((catName)) ?: 0
		//if (n< 10 || true){
		catsFreq.put((catName), n + 1)
		//if (docsSet.add (f.name ) && ! f.canonicalPath.contains("test")) {

		println "adding fname ${f.name} catname $catName name " + f.name +  "  path " + f.getCanonicalPath()

		Field catNameField = new StringField(IndexInfo.FIELD_CATEGORY_NAME, catName, Field.Store.YES);
		doc.add(catNameField)
		//f.getParentFile().getName(), Field.Store.YES);

		//doc.add(new TextField(IndexInfoStaticG.FIELD_CONTENTS, new BufferedReader(new InputStreamReader(fis, "UTF-8"))) );
		doc.add(new TextField(IndexInfo.FIELD_CONTENTS, f.text,  Field.Store.YES)) ;

		//	Field categoryField = new StringField(IndexInfo.FIELD_CATEGORY, categoryNumber.toString(), Field.Store.YES);
		Field categoryField = new StringField(IndexInfo.FIELD_CATEGORY, catName, Field.Store.YES);  //for classic3 name is same as category
		doc.add(categoryField)

		//set test train field
		String test_train
		if ( f.canonicalPath.contains("test")) test_train="test" else test_train="train";

		Field ttField = new StringField(IndexInfo.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
		doc.add(ttField)

		writer.addDocument(doc);
	}
}
