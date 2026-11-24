package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {

    private final Map<Integer, Movie> movies;

    public MoviesStore() {
        this.movies = new HashMap<>();
    }

    public Movie addMovie(String title, Integer year) {
        var movie = new Movie(title, year);
        movies.put(movie.getId(), movie);
        return movie;
    }

    public List<Movie> getMovies() {
        return movies.values().stream().toList();
    }

    public Optional<Movie> getMovieById(Integer id) {
        return Optional.ofNullable(movies.get(id));
    }

    public List<Movie> getMoviesByYear(Integer year) {
        return movies.values().stream()
            .filter(m -> m.getYear().equals(year))
            .toList();
    }

    public void removeMovieById(Integer id) {
        if (!movies.containsKey(id)) {
            throw new NoSuchElementException();
        }
        movies.remove(id);
    }

    public void clear() {
        movies.clear();
    }
}