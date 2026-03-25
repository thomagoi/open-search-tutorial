package org.acme.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.hibernate.search.engine.backend.types.Sortable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Indexed
public class Author extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @FullTextField(analyzer = "name_analyzer")
    @KeywordField(name = "name_sort", sortable = Sortable.YES)
    public String name;

    public LocalDate birthdate;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @IndexedEmbedded(includePaths = {"title", "description", "genre"})
    @JsonManagedReference
    public List<Book> books = new ArrayList<>();
}
