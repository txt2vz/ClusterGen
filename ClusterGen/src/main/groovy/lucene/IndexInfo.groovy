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
	public static final String FIELD_CATEGORY_NAME = "category_name";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";

    int NUMBER_OF_CLUSTERS =  3

	public IndexSearcher indexSearcher
	public IndexReader indexReader

	public String pathToIndex =

	//"indexes/Ohs3Bact02Dig06Resp08"
	//"indexes/20NG6GraphicsHockeyCryptSpaceChristianGuns"
    "indexes/20NG3SpaceHockeyChristian"
	//"indexes/reut5"
	//"indexes/classic4_500"
	//"indexes/webkb"
	//"indexes/reut8stem"
	//"indexes/reut8"
	//  "indexes/crisis4FireBombFloodShoot"
	

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