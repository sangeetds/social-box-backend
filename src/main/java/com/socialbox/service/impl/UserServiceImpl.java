package com.socialbox.service.impl;

import com.socialbox.dto.UserDTO;
import com.socialbox.dto.UserMovieDTO;
import com.socialbox.model.Movie;
import com.socialbox.model.User;
import com.socialbox.repository.UserRepository;
import com.socialbox.service.MovieService;
import com.socialbox.service.UserService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final MovieService movieService;

  @Autowired
  public UserServiceImpl(UserRepository userRepository, MovieService movieService) {
    this.userRepository = userRepository;
    this.movieService = movieService;
  }

  @Override
  public List<User> getAllUsers() {
    return this.userRepository.findAll();
  }

  @Override
  public User getUserById(String id) {
    Optional<User> userOptional = this.userRepository.findById(id);
    userOptional.ifPresent(user -> log.info("Found user: {}", user));
    if (!userOptional.isPresent()) {
      log.error("User not found with id: {}", id);
    }

    return userOptional.orElse(null);
  }

  @Override
  public User loginUser(UserDTO user) {

    Optional<User> userOptional = this.userRepository.findById(user.getId());

    return userOptional.orElseGet(
        () -> this.userRepository.save(
            User.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .groups(new ArrayList<>())
                .owningGroup(new ArrayList<>())
                .photoURL(user.getPhotoURL())
                .personalMovieList(new ArrayList<>())
                .sharedMovieList(new ArrayList<>())
                .build()));
  }

  @Override
  public User saveUser(User user) {
    return this.userRepository.save(user);
  }

  @Override
  public List<UserMovieDTO> getMovies(String id) {
    Optional<User> userOptional = this.userRepository.findById(id);
    User currentUser = userOptional.orElse(null);

    if (currentUser == null) {
      log.error("User not found.");
      return null;
    }

    List<Movie> movieList =
        this.movieService.getMoviesByIds(currentUser.getPersonalMovieList()
            .stream()
            .map(userRatings -> userRatings.getMovie().getId())
            .collect(Collectors.toList()));
    List<UserMovieDTO> movieDTOS = new ArrayList<>();

    for (Movie movie : movieList) {
      UserMovieDTO movieDTO =
          UserMovieDTO.builder()
              .userId(id)
              .id(movie.getId())
              .name(movie.getName())
              .photoURL(movie.getPhotoURL())
              .rating(movie.getRating())
              .userRating(5) // Todo: Change User Ratings
              .votes(movie.getVotes())
              .build();

      movieDTOS.add(movieDTO);
    }

    return movieDTOS;
  }
}
