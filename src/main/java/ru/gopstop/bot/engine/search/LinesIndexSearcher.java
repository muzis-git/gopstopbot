package ru.gopstop.bot.engine.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import ru.gopstop.bot.engine.entities.GopSong;
import ru.gopstop.bot.engine.search.preprocessing.BasicPreprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aam on 31.07.16.
 */
public class LinesIndexSearcher {

    private final IndexSearcher is;
//    private final CustomAnalyzer analyzer;

    private final Analyzer analyzer;

    private final static Logger LOGGER = LogManager.getLogger(LinesIndexSearcher.class);

    private final static int COUNT_RETURNED = 20;

    public LinesIndexSearcher(final Directory dir, final Analyzer analyzer) throws IOException {

        final IndexReader ir = DirectoryReader.open(dir);
        is = new IndexSearcher(ir);
        this.analyzer = analyzer;

    }

    public List<FoundGopSong> search(final String request) throws IOException {

        final BooleanQuery q = new BooleanQuery();
        final String processedRequest = BasicPreprocessor.postfix(request);

//        for (final String token : analyzer.handle(processedRequest)) {
//            q.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
//        }

        // тупой префиксный запрос

        for (int i = 5; i > 0; i--) {
            q.add(new PrefixQuery(
                            new Term("text", processedRequest.substring(0, Math.min(i, processedRequest.length())))),
                    BooleanClause.Occur.SHOULD);
        }
        final TopDocs docs = is.search(q, COUNT_RETURNED);

        final List<FoundGopSong> foundSongs = new ArrayList<>();

        LOGGER.info("REQUEST: [" + request + "] hits " + docs.totalHits);

        for (int i = 0; i < Math.min(COUNT_RETURNED, docs.totalHits); i++) {
            final Document doc = is.doc(docs.scoreDocs[i].doc);
            LOGGER.info(docs.scoreDocs[i].score + "\t| " + doc.get("fulltext") + " | " + doc.get("text"));
            foundSongs.add(
                    new FoundGopSong(
                            new GopSong(
                                    doc.get("title"),
                                    doc.get("author"), null),
                            doc.get("fulltext"),
                            docs.scoreDocs[i].score));

        }
        return foundSongs;
    }
}
