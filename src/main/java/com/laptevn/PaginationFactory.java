package com.laptevn;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaginationFactory {
    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public Optional<Pageable> createPagination(Integer pageIndex, Integer pageSize) {
        if (pageIndex == null && pageSize == null) {
            return Optional.empty();
        }

        int index = pageIndex == null ? DEFAULT_PAGE_INDEX : pageIndex;
        int zeroBasedIndex = index - 1;

        int size = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        return Optional.of(PageRequest.of(zeroBasedIndex, size));
    }
}