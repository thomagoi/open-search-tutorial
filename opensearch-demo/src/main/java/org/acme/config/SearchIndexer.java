package org.acme.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.search.mapper.orm.Search;

@ApplicationScoped
public class SearchIndexer {

    @Inject
    EntityManager entityManager;

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        Search.session(entityManager).massIndexer().startAndWait();
    }
}
