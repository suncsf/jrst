package com.braisefish.jrst.i.page;

import lombok.Data;

import java.util.Collections;
import java.util.List;
/**
 * @author 32365
 */
@Data
public class PageResult<T> {
    protected List<T> records;
    protected int total;
    protected int size;
    protected int pages;
    protected int current;

    public static <T> PageResult<T> of(List<T> list, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(pageNum);
        result.setSize(pageSize);
        result.setPages((int) Math.ceil((double)list.size()/pageSize));
        result.setRecords(paginateBySubList(list, pageNum, pageSize));
        result.setTotal(list.size());
        return result;
    }
    public static <T> List<T> paginateBySubList(List<T> list, int pageNum, int pageSize) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, total);
        return list.subList(fromIndex, toIndex);
    }

}
