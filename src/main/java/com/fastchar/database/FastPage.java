package com.fastchar.database;

import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastSqlInfo;

import java.util.List;

public class FastPage<T extends FastEntity<?>> {
    private transient FastSqlInfo sqlInfo;
    private int page;
    private int totalPage;
    private int totalRow;
    private int pageSize;
    private List<T> list;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalRow() {
        return totalRow;
    }

    public void setTotalRow(int totalRow) {
        this.totalRow = totalRow;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public FastSqlInfo getSqlInfo() {
        return sqlInfo;
    }

    public FastPage<T> setSqlInfo(FastSqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
        return this;
    }

    public void release() {
        if (this.list != null) {
            this.list.clear();
        }
        this.list = null;
    }

}
