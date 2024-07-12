package com.alura.literAlura.model.entity.libro;

import com.alura.literAlura.model.dto.DatosAutor;
import com.alura.literAlura.model.dto.DatosLibro;
import com.alura.literAlura.model.entity.autor.Autor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@Entity
@Table(name = "libros")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Autor autor;
    private String idioma;
    @Column(name = "numero_de_descargas")
    private Integer numeroDeDescargas;

    public Libro(DatosLibro libro) {
        this.titulo = libro.titulo();
        Optional<DatosAutor> autor = libro.autores().stream()
                .findFirst();
        if (autor.isPresent()) {
            this.autor = new Autor(autor.get());
        } else {
            System.out.println("No se ha podido encontrar el autor");
        }
        this.idioma = libro.idiomas().get(0);
        this.numeroDeDescargas = libro.numeroDeDescargas();
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

}
