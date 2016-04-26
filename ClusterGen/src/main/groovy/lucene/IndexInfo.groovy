package lucene

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

import query.*

/**
 * Singleton class to store index information.
 * Set the path to the lucene index here
 */
@Singleton
class IndexInfo {

	// Lucene field names
	public static final String FIELD_CATEGORY = "category";
	public static final String FIELD_CATEGORY_NAME = "category_name";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";

    int NUMBER_OF_CLUSTERS = 3 

	public IndexSearcher indexSearcher
	public IndexReader indexReader

	private String pathToIndex =
	//"indexes/classic4_500"
	//"indexes/webkb"
	//"indexes/reut8stem"
	//"indexes/reut8"
	'indexes/20NG3SpaceHockeyChristian'
	//"C:\\Users\\Laurie\\Java\\indexes2\\20NG3TestSpaceHockeyChristian"
//	"C:\\Users\\laurie\\Java\\indexes2\\classic3"
	//"C:\\Users\\laurie\\Java\\indexes2\\webkb"
	
//	/C:\Users\laurie\Java\indexes2\classic4_200/ 
	//"C:\\Users\\laurie\\Java\\indexes2\\classic4"
		//"C:\\Users\\laurie\\Java\\indexes2\\20NG4GunCryptChristianHockeyP"
	//private final static pathToIndex =
	//  "C:\\Users\\laurie\\Java\\indexes2\\crawl7"
	//	"C:\\Users\\laurie\\Java\\indexes2\\20NG3"
	//	"/home/test/indexes2/20NG4HockeySpaceChristianGuns"
	//	"/home/test/indexes2/20NG4HockeySpaceChristianGunsNoStem"
	//	"/home/test/indexes2/20bydate/"
	//	"C:\\Users\\laurie\\Java\\indexes2\\20NG5macForsaleCryptMideast"
	//"C:\\Users\\laurie\\Java\\indexes2\\bbc"
	//	"C:\\Users\\laurie\\Java\\indexes2\\Ohs3BactDigestResp"
	//	"C:\\Users\\laurie\\Java\\indexes2\\20NG3MedHockeyGraphicsTest"

	public void setIndex(){

		Path path = Paths.get(pathToIndex)
		Directory directory = FSDirectory?.open(path)

		indexReader = DirectoryReader?.open(directory)
		indexSearcher = new IndexSearcher(indexReader);

		TermQuery trainQ = new TermQuery(new Term(
				IndexInfo.FIELD_TEST_TRAIN, "train"));

		TermQuery testQ = new TermQuery(new Term(
				IndexInfo.FIELD_TEST_TRAIN, "test"));
	}

}