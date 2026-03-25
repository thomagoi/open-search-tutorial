package org.acme.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import java.util.UUID;

@Entity
@Indexed
public class Book extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @FullTextField(analyzer = "name_analyzer")
    public String title;

    @FullTextField(analyzer = "name_analyzer")
    public String description;

    @GenericField
    public long price;

    @Enumerated(EnumType.STRING)
    @KeywordField
    public Genre genre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    @IndexedEmbedded(includePaths = {"name"})
    @JsonBackReference
    public Author author;
}
