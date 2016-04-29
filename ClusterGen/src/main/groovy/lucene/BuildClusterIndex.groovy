 package lucene

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
//import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.codecs.*

class BuildClusterIndex {
	// Create Lucene index in this directory
	def indexPath = "indexes/Ohs3Bact02Dig06Resp08"	
	//"indexes/20NG5macForsaleHockeyCryptMideast"
	//"indexes/20NG3SpaceHockeyChristian"

	//	/C:\Users\laurie\Java\indexes2\classic4_500/
	//"C:\\Users\\laurie\\Java\\indexes2\\webkb"
	//"C:\\Users\\laurie\\Java\\indexes2\\20NG3TestSpaceHockeyChristian"
	//"C:\\Users\\laurie\\Java\\indexes2\\20NG4GunCryptChristianHockeyP"
	//'/home/test/indexes/20NG3SpaceHockeyChristian'
	//'/home/test/indexes2/20NG4HockeySpaceChristianGunsNoStem'
	//"C:\\Users\\laurie\\Java\\indexes2\\20NG3MedHockeyGraphicsTest"
	//"C:\\Users\\laurie\\Java\\indexes2\\bbc2"

	// Index files in this directory
	//	def docsPath = "C:\\Users\\Laurie\\Dataset\\20NG3Test"
	def docsPath =
//  	/C:\Users\Laurie\Dataset\webkb/
	
	// /C:\Users\Laurie\Dataset\20NG3TestSpaceHockeyChristian/
	/C:\Users\Laurie\Dataset\Ohs3BactDigestResp/
	//  /C:\Users\Laurie\Dataset\20NG5macForsaleCryptMideast/
//	/C:\Users\Laurie\Dataset\20NG4GunCryptChristianHockey/
	//C:\Users\Laurie\Dataset\classic/
	//"C:\\Users\\Laurie\\Dataset\\20NG3TestSpaceHockeyChristian"
	//"C:\\Users\\Laurie\\Dataset\\20NG4GunCryptChristianHockey"
	//'/home/test/datasets/20NG3SpaceHockeyChristian'
	//'/home/test/dataset/20NG4HockeySpaceChristianGuns/'
	//"C:\\Users\\Laurie\\Dataset\\bbc"
	//"C:\\Users\\Laurie\\Dataset\\20bydate"

	Path path = Paths.get(indexPath)
	Directory directory = FSDirectory.open(path)
	Analyzer analyzer = //new EnglishAnalyzer();
	new StandardAnalyzer();
	def catsFreq=[:]

	static main(args) {
		def i = new BuildClusterIndex()
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
			//it.eachDir{
			it.eachFileRecurse {
				if (!it.hidden && it.exists() && it.canRead()  && !it.directory
				//	&& !it.name.contains("cacm")
				)
				{
					indexDocs(writer,it, catNumber)
				}
			}
			//}
			catNumber++;
		}

		Date end = new Date();
		println (end.getTime() - start.getTime() + " total milliseconds");
		println "Total docs: " + writer.maxDoc()
		writer.close()
		println "End ***************************************************************"
	}

	//index the doc adding fields for path, category, test/train and contents
	def indexDocs(IndexWriter writer, File f, categoryNumber)
	throws IOException {

		def doc = new Document()
//C:\Users\Laurie\Dataset\Ohs3BactDigestResp\C08

		Field pathField = new StringField(IndexInfo.FIELD_PATH, f.getPath(), Field.Store.YES);
		doc.add(pathField);

		//for classic3 dataset
		//def catName = f.getName().substring(0,4)
//C:\Users\Laurie\Dataset\20NG3TestSpaceHockeyChristian\rec.sport.hockey\53715
		//for webKB
		def catName = f.getCanonicalPath().drop(44).take(8).replaceAll("[0-9]","")
	

		def n = catsFreq.get((catName)) ?: 0
		if (n< 500 || true){
			catsFreq.put((catName), n + 1)	

			Field catNameField = new StringField(IndexInfo.FIELD_CATEGORY_NAME, catName, Field.Store.YES);
			doc.add(catNameField)
			//f.getParentFile().getName(), Field.Store.YES);

			//doc.add(new TextField(IndexInfoStaticG.FIELD_CONTENTS, new BufferedReader(new InputStreamReader(fis, "UTF-8"))) );
			doc.add(new TextField(IndexInfo.FIELD_CONTENTS, f.text,  Field.Store.YES)) ;

			//	Field categoryField = new StringField(IndexInfo.FIELD_CATEGORY, categoryNumber.toString(), Field.Store.YES);
			//Field categoryField = new StringField(IndexInfo.FIELD_CATEGORY, catName, Field.Store.YES);  //for classic3 name is same as category
			//doc.add(categoryField)

			//set test train field
			String test_train
			if ( f.canonicalPath.contains("test")) test_train="test" else test_train="train";

			Field ttField = new StringField(IndexInfo.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
			doc.add(ttField)

			writer.addDocument(doc);
		}
	}
}
