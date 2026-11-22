package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {

    private final List<Movie> movies;

    public MoviesStore() {
        this.movies = new ArrayList<>();
    }

    public Movie addMovie(String title, Integer year) {
        var id = !movies.isEmpty() ? movies.getLast().getId() + 1 : 0;
        var movie = new Movie(id, title, year);
        movies.add(movie);
        return movie;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public Optional<Movie> getMovieById(Integer id) {
        return movies.stream()
            .filter(m -> m.getId().equals(id))
            .findFirst();
    }

    public List<Movie> getMoviesByYear(Integer year) {
        return movies.stream()
            .filter(m -> m.getYear().equals(year))
            .toList();
    }

    public void removeMovieById(Integer id) {
        boolean removed = movies.removeIf(m -> m.getId().equals(id));
        if (!removed) throw new NoSuchElementException();
    }

    public void clear() {
        movies.clear();
    }
}