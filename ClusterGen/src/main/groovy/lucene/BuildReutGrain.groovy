 package lucene

import groovy.io.FileType
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
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

class BuildReutGrain {
	// Create Lucene index in this directory
	def indexPath = 	"indexes/reut8"
	def maxN = 0

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
	// /C:\Users\Laurie\Dataset\webkb/
	///C:\Users\Laurie\Dataset\reut8/
	// /C:\Users\Laurie\Dataset\reuters-top10/
	 /C:\Users\Laurie\Dataset\reut90\training/
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
	def docsSet = [] as Set

	static main(args) {
		def i = new BuildReutGrain()
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

		//for webKB
		def catName = f.getCanonicalPath().drop(40).take(5)

		//println "catName $catName"

//C:\Users\Laurie\Dataset\reuters-top10\04_grain
//C:\Users\Laurie\Dataset\reut8\01_corn
//C:\Users\Laurie\Dataset\reut90\training\alum

		//def n = catsFreq.get((catName)) ?: 0
		//catsFreq.put((catName), n + 1)

	
		if (docsSet.add (f.name ) && maxN <200) {
			if (catName=="zztra"){
				def fname =  "C:\\Users\\Laurie\\Dataset\\r8\\trade\\"
				fname = fname + f.name + ".txt"

				println "adding fname"
				def dest = new File(   fname )
				dest << f.text
				maxN++

			}
		}else println "Not adding fnameXX ${f.name} catName $catName"
		//}
	}
}
