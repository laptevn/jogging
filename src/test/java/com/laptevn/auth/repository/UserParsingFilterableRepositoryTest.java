package com.laptevn.auth.repository;

import com.laptevn.exception.IntegrityException;
import com.laptevn.repository.filtering.WhereClauseBuilder;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import java.util.Optional;

public class UserParsingFilterableRepositoryTest {
    @Test(expected = IntegrityException.class)
    public void noPredicateBuild() {
        new UserParsingFilterableRepository(createEntityManager(createQuery(false)), createWhereClauseBuilder(null))
                .findAll(null, Optional.empty());
    }

    private static TypedQuery createQuery(boolean isPaging) {
        TypedQuery query = EasyMock.mock(TypedQuery.class);
        EasyMock.expect(query.getResultList()).andReturn(null);
        if (isPaging) {
            EasyMock.expect(query.setFirstResult(EasyMock.anyInt())).andReturn(query).once();
            EasyMock.expect(query.setMaxResults(EasyMock.anyInt())).andReturn(query).once();
        }
        EasyMock.replay(query);
        return query;
    }

    private static EntityManager createEntityManager(TypedQuery query) {
        CriteriaQuery criteriaQuery = EasyMock.mock(CriteriaQuery.class);
        EasyMock.expect(criteriaQuery.select(EasyMock.anyObject())).andReturn(criteriaQuery);
        EasyMock.expect(criteriaQuery.where(EasyMock.<Predicate>anyObject())).andReturn(null);
        EasyMock.expect(criteriaQuery.from(EasyMock.<Class>anyObject())).andReturn(null);

        CriteriaBuilder criteriaBuilder = EasyMock.mock(CriteriaBuilder.class);
        EasyMock.expect(criteriaBuilder.createQuery(EasyMock.anyObject())).andReturn(criteriaQuery);

        EntityManager entityManager = EasyMock.mock(EntityManager.class);
        EasyMock.expect(entityManager.getCriteriaBuilder()).andReturn(criteriaBuilder);
        EasyMock.expect(entityManager.createQuery(criteriaQuery)).andReturn(query);

        EasyMock.replay(criteriaQuery, criteriaBuilder, entityManager);
        return entityManager;
    }

    private static WhereClauseBuilder createWhereClauseBuilder(Predicate predicate) {
        WhereClauseBuilder whereClauseBuilder = EasyMock.mock(WhereClauseBuilder.class);
        EasyMock.expect(whereClauseBuilder.build(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString()))
                .andReturn(predicate);

        EasyMock.replay(whereClauseBuilder);
        return whereClauseBuilder;
    }

    @Test
    public void noPagination() {
        new UserParsingFilterableRepository(createEntityManager(createQuery(false)), createWhereClauseBuilder(EasyMock.mock(Predicate.class)))
                .findAll(null, Optional.empty());
    }

    @Test
    public void pagination() {
        TypedQuery query = createQuery(true);
        new UserParsingFilterableRepository(createEntityManager(query), createWhereClauseBuilder(EasyMock.mock(Predicate.class)))
                .findAll(null, Optional.of(PageRequest.of(1, 2)));
        EasyMock.verify(query);
    }
}