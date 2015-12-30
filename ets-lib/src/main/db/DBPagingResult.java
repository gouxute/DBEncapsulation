package com.etrans.lib.db;

import java.sql.ResultSet;

/**
 * Created by maotz on 2015-04-25.
 * Data search page's division result
 */
public class DBPagingResult {
    public final int rowsCount;
    public final int pageCount;
    public final int pageIndex;
    public final ResultSet result;

    DBPagingResult(int _rows_count, int _page_count, int _page_index, ResultSet _result){
        rowsCount = _rows_count;
        pageCount = _page_count;
        pageIndex = _page_index;
        result = _result;
    }
}
