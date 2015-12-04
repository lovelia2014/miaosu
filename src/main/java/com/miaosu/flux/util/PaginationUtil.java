package com.miaosu.flux.util;

import org.springframework.data.domain.PageRequest;

/**
 * Utility class for handling pagination.
 *
 * <p>
 * Pagination uses the same principles as the <a href="https://developer.github.com/v3/#pagination">Github API</api>,
 * and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 * </p>
 */
public class PaginationUtil {

    public static final int DEFAULT_OFFSET = 0;

    public static final int DEFAULT_LIMIT = 20;

    public static final int MAX_LIMIT = 100;

    public static PageRequest generatePageRequest(Integer start, Integer size) {
        Integer page;

        if (start == null) {
            page = DEFAULT_OFFSET;
        } else {
            page = (start / size);
        }

        if (size == null || size > MAX_LIMIT) {
            size = DEFAULT_LIMIT;
        }

        return new PageRequest(page, size);
    }
}
