package Custom_IR.IndexPhase;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;


import java.util.Set;

public class CustomAnalyzer extends Analyzer {

    private final CharArraySet stopwords;

    public CustomAnalyzer(Set<String> stopwords) {
        this.stopwords = new CharArraySet(stopwords, true);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();

        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
        tokenStream = new StopFilter(tokenStream, stopwords);
        tokenStream = new KStemFilter(tokenStream);

        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
