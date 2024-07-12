package com.alura.literAlura.principal;

import com.alura.literAlura.model.dto.Datos;
import com.alura.literAlura.model.dto.DatosLibro;
import com.alura.literAlura.model.entity.autor.Autor;
import com.alura.literAlura.model.entity.libro.Libro;
import com.alura.literAlura.model.entity.autor.AutorRepository;
import com.alura.literAlura.model.entity.libro.LibroRepository;
import com.alura.literAlura.service.ConsumoApi;
import com.alura.literAlura.service.conversor.Conversor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL = "https://gutendex.com/books/";
    private ConsumoApi consumoApi = new ConsumoApi();
    private Conversor conversor = new Conversor();
    private Integer opcion = 10;
    private Scanner scanner = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    private void leerLibro(Libro libro) {
        System.out.printf("""
                        ----- LIBRO -----
                        Titulo: %s
                        Autor: %s
                        Idioma: %s
                        Numero de descargas: %d
                        -------------------- \n
                        """,
                libro.getTitulo(),
                libro.getAutor().getNombre(),
                libro.getIdioma(),
                libro.getNumeroDeDescargas());
    }

    private void buscarLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        String nombreLibro = scanner.nextLine();
        String json = consumoApi.obtenerLibros(URL + "?search=" + nombreLibro.replace(" ", "+"));
        if (json == null || json.isEmpty()) {
            System.out.println("No se recibió ninguna respuesta de la API.");
            return;
        }
        List<DatosLibro> libros = conversor.obtenerDatos(json, Datos.class).resultados();
        Optional<DatosLibro> libroOptional = libros.stream()
                .filter(l -> l.titulo().toLowerCase().contains(nombreLibro.toLowerCase()))
                .findFirst();
        if (libroOptional.isPresent()) {
            var libro = new Libro(libroOptional.get());
            libroRepository.save(libro);
            leerLibro(libro);
        } else {
            System.out.println("El libro no ha podido ser encontrado");
        }
    }

    private void listarLibros() {
        List<Libro> libros = libroRepository.findAll();
        libros.stream().forEach(this::leerLibro);
    }

    private void leerAutor(Autor autor) {
        System.out.printf("""
                        Autor: %s
                        Fecha de nacimiento: %s
                        Fecha de fallecimiento: %s
                        """,
                autor.getNombre(),
                autor.getFechaDeNacimiento(),
                autor.getFechaDeFallecimiento());

        var libros = autor.getLibros().stream()
                .map(a -> a.getTitulo())
                .collect(Collectors.toList());
        System.out.println("Libros: " + libros + "\n");
    }

    private void listarAutores() {
        List<Autor> autores = autorRepository.findAll();
        autores.stream().forEach(this::leerAutor);
    }

    private void listarAutoresPorAño() {
        System.out.println("Ingresa el año vivo de autor(es) que desea buscar");
        Integer año = scanner.nextInt();
        List<Autor> autores = autorRepository.findByFechaDeFallecimientoGreaterThan(año);
        autores.stream().forEach(this::leerAutor);
    }

    private void listarLibrosPorIdioma() {
        System.out.println("""
                Ingrese el idioma para buscar los libros
                es - español
                en - ingles
                fr - frances
                pt - portugues
                """);
        String idioma = scanner.next();
        List<Libro> libros = libroRepository.findByIdioma(idioma);
        libros.stream().forEach(this::leerLibro);
    }

    private void generarEstadisticasDelNumeroDeDescargas() {
        var libros = libroRepository.findAll();
        DoubleSummaryStatistics doubleSummaryStatistics = new DoubleSummaryStatistics();
        for (Libro libro : libros) doubleSummaryStatistics.accept(libro.getNumeroDeDescargas());
        System.out.println("Conteo del numero de descargas - " + doubleSummaryStatistics.getCount());
        System.out.println("Numero de descargas minimo - " + doubleSummaryStatistics.getMin());
        System.out.println("Numero de descargas maximo - " + doubleSummaryStatistics.getMax());
        System.out.println("Suma del numero de descargas - " + doubleSummaryStatistics.getSum());
        System.out.println("Promedio del numero de descargas - " + doubleSummaryStatistics.getAverage() + "\n");
    }

    private void listarTop10Libros() {
        libroRepository.buscarTop10Libros().stream().forEach(this::leerLibro);
    }

    private void buscarAutorPorNombre() {
        System.out.println("Ingresa un nombre para buscar al autor");
        String nombre = scanner.nextLine();
        autorRepository.findByNombre(nombre).stream().forEach(this::leerAutor);
    }

    public void mostrarMenu() {
        while (opcion != 9) {
            System.out.println("""
                    Elija la opcion a traves de su numero:
                    1- buscar libro por titulo
                    2- listar libros registrados
                    3- listar autores registrados
                    4- listar autores vivos en un determinado año
                    5- listar libros por idioma
                    6- generar estadisticas del numero de descargas
                    7- listar el top 10 de libros mas descargados
                    8- buscar autor por nombre
                    9- salir
                    """);
            opcion = scanner.nextInt();
            scanner.nextLine();  // Añadido para consumir la nueva línea después de nextInt()
            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    listarLibros();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    listarAutoresPorAño();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    generarEstadisticasDelNumeroDeDescargas();
                    break;
                case 7:
                    listarTop10Libros();
                    break;
                case 8:
                    buscarAutorPorNombre();
                    break;
                case 9:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente nuevamente.");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        LibroRepository libroRepository = new LibroRepository() {
            /**
             * @param sort
             * @return
             */
            @Override
            public List<Libro> findAll(Sort sort) {
                return List.of();
            }

            /**
             * @param pageable
             * @return
             */
            @Override
            public Page<Libro> findAll(Pageable pageable) {
                return null;
            }

            /**
             * @param entity
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> S save(S entity) {
                return null;
            }

            /**
             * @param entities
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> List<S> saveAll(Iterable<S> entities) {
                return List.of();
            }

            /**
             * @param aLong
             * @return
             */
            @Override
            public Optional<Libro> findById(Long aLong) {
                return Optional.empty();
            }

            /**
             * @param aLong
             * @return
             */
            @Override
            public boolean existsById(Long aLong) {
                return false;
            }

            /**
             * @return
             */
            @Override
            public List<Libro> findAll() {
                return List.of();
            }

            /**
             * @param longs
             * @return
             */
            @Override
            public List<Libro> findAllById(Iterable<Long> longs) {
                return List.of();
            }

            /**
             * @return
             */
            @Override
            public long count() {
                return 0;
            }

            /**
             * @param aLong
             */
            @Override
            public void deleteById(Long aLong) {

            }

            /**
             * @param entity
             */
            @Override
            public void delete(Libro entity) {

            }

            /**
             * @param longs
             */
            @Override
            public void deleteAllById(Iterable<? extends Long> longs) {

            }

            /**
             * @param entities
             */
            @Override
            public void deleteAll(Iterable<? extends Libro> entities) {

            }

            /**
             *
             */
            @Override
            public void deleteAll() {

            }

            /**
             *
             */
            @Override
            public void flush() {

            }

            /**
             * @param entity
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> S saveAndFlush(S entity) {
                return null;
            }

            /**
             * @param entities
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> List<S> saveAllAndFlush(Iterable<S> entities) {
                return List.of();
            }

            /**
             * @param entities
             */
            @Override
            public void deleteAllInBatch(Iterable<Libro> entities) {

            }

            /**
             * @param longs
             */
            @Override
            public void deleteAllByIdInBatch(Iterable<Long> longs) {

            }

            /**
             *
             */
            @Override
            public void deleteAllInBatch() {

            }

            /**
             * @param aLong
             * @deprecated
             */
            @Override
            public Libro getOne(Long aLong) {
                return null;
            }

            /**
             * @param aLong
             * @deprecated
             */
            @Override
            public Libro getById(Long aLong) {
                return null;
            }

            /**
             * @param aLong
             * @return
             */
            @Override
            public Libro getReferenceById(Long aLong) {
                return null;
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> List<S> findAll(Example<S> example) {
                return List.of();
            }

            /**
             * @param example
             * @param sort
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> List<S> findAll(Example<S> example, Sort sort) {
                return List.of();
            }

            /**
             * @param example
             * @param pageable
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> long count(Example<S> example) {
                return 0;
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Libro> boolean exists(Example<S> example) {
                return false;
            }

            /**
             * @param example
             * @param queryFunction
             * @param <S>
             * @param <R>
             * @return
             */
            @Override
            public <S extends Libro, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return null;
            }

            /**
             * @param idioma
             * @return
             */
            @Override
            public List<Libro> findByIdioma(String idioma) {
                return List.of();
            }

            /**
             * @return
             */
            @Override
            public List<Libro> buscarTop10Libros() {
                return List.of();
            }
        };
        AutorRepository autorRepository = new AutorRepository() {
            /**
             * @param sort
             * @return
             */
            @Override
            public List<Autor> findAll(Sort sort) {
                return List.of();
            }

            /**
             * @param pageable
             * @return
             */
            @Override
            public Page<Autor> findAll(Pageable pageable) {
                return null;
            }

            /**
             * @param entity
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> S save(S entity) {
                return null;
            }

            /**
             * @param entities
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> List<S> saveAll(Iterable<S> entities) {
                return List.of();
            }

            /**
             * @param aLong
             * @return
             */
            @Override
            public Optional<Autor> findById(Long aLong) {
                return Optional.empty();
            }

            /**
             * @param aLong
             * @return
             */
            @Override
            public boolean existsById(Long aLong) {
                return false;
            }

            /**
             * @return
             */
            @Override
            public List<Autor> findAll() {
                return List.of();
            }

            /**
             * @param longs
             * @return
             */
            @Override
            public List<Autor> findAllById(Iterable<Long> longs) {
                return List.of();
            }

            /**
             * @return
             */
            @Override
            public long count() {
                return 0;
            }

            /**
             * @param aLong
             */
            @Override
            public void deleteById(Long aLong) {

            }

            /**
             * @param entity
             */
            @Override
            public void delete(Autor entity) {

            }

            /**
             * @param longs
             */
            @Override
            public void deleteAllById(Iterable<? extends Long> longs) {

            }

            /**
             * @param entities
             */
            @Override
            public void deleteAll(Iterable<? extends Autor> entities) {

            }

            /**
             *
             */
            @Override
            public void deleteAll() {

            }

            /**
             *
             */
            @Override
            public void flush() {

            }

            /**
             * @param entity
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> S saveAndFlush(S entity) {
                return null;
            }

            /**
             * @param entities
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> List<S> saveAllAndFlush(Iterable<S> entities) {
                return List.of();
            }

            /**
             * @param entities
             */
            @Override
            public void deleteAllInBatch(Iterable<Autor> entities) {

            }

            /**
             * @param longs
             */
            @Override
            public void deleteAllByIdInBatch(Iterable<Long> longs) {

            }

            /**
             *
             */
            @Override
            public void deleteAllInBatch() {

            }

            /**
             * @param aLong
             * @deprecated
             */
            @Override
            public Autor getOne(Long aLong) {
                return null;
            }

            /**
             * @param aLong
             * @deprecated
             */
            @Override
            public Autor getById(Long aLong) {
                return null;
            }

            /**
             * @param aLong
             * @return
             */
            @Override
            public Autor getReferenceById(Long aLong) {
                return null;
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> List<S> findAll(Example<S> example) {
                return List.of();
            }

            /**
             * @param example
             * @param sort
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> List<S> findAll(Example<S> example, Sort sort) {
                return List.of();
            }

            /**
             * @param example
             * @param pageable
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> long count(Example<S> example) {
                return 0;
            }

            /**
             * @param example
             * @param <S>
             * @return
             */
            @Override
            public <S extends Autor> boolean exists(Example<S> example) {
                return false;
            }

            /**
             * @param example
             * @param queryFunction
             * @param <S>
             * @param <R>
             * @return
             */
            @Override
            public <S extends Autor, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return null;
            }

            /**
             * @param año
             * @return
             */
            @Override
            public List<Autor> findByFechaDeFallecimientoGreaterThan(Integer año) {
                return List.of();
            }

            /**
             * @param nombre
             * @return
             */
            @Override
            public List<Autor> findByNombre(String nombre) {
                return List.of();
            }
        };
        Principal principal = new Principal(libroRepository, autorRepository);
        principal.mostrarMenu();
    }
}
