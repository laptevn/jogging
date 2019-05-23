package com.laptevn;

import com.laptevn.PaginationFactory;
import org.junit.Test;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaginationFactoryTest {
    @Test
    public void noPagination() {
        assertFalse(new PaginationFactory().createPagination(null, null).isPresent());
    }

    @Test
    public void pagination() {
        Optional<Pageable> pageable = new PaginationFactory().createPagination(5, 2);
        assertTrue("Pageable wasn't created", pageable.isPresent());
        assertEquals("Invalid page index", 4, pageable.get().getPageNumber());
        assertEquals("Invalid page size", 2, pageable.get().getPageSize());
    }

    @Test
    public void paginationNoPageIndex() {
        Optional<Pageable> pageable = new PaginationFactory().createPagination(null, 2);
        assertTrue("Pageable wasn't created", pageable.isPresent());
        assertEquals("Invalid page index", 0, pageable.get().getPageNumber());
        assertEquals("Invalid page size", 2, pageable.get().getPageSize());
    }

    @Test
    public void paginationNoPageSize() {
        Optional<Pageable> pageable = new PaginationFactory().createPagination(3, null);
        assertTrue("Pageable wasn't created", pageable.isPresent());
        assertEquals("Invalid page index", 2, pageable.get().getPageNumber());
        assertEquals("Invalid page size", 10, pageable.get().getPageSize());
    }
}