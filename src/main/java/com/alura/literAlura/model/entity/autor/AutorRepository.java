package com.alura.literAlura.model.entity.autor;

import com.alura.literAlura.model.entity.autor.Autor;
import com.alura.literAlura.model.entity.libro.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    List<Autor> findByFechaDeFallecimientoGreaterThan(Integer año);

    List<Autor> findByNombre(String nombre);
}
