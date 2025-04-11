package group4.opensource_server.user.domain;

import group4.opensource_server.user.dto.UserUpdateRequestDto;
import group4.opensource_server.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(String email, UserUpdateRequestDto userUpdateRequestDto) {
        User user=userRepository.findByEmail(email).orElseThrow( ()-> new UserNotFoundException("없는 유저입니다.") );
        return userRepository.save(user.update(userUpdateRequestDto));
    }

    @Transactional
    public User deleteUser(String email) {
        User user=userRepository.findByEmail(email).orElseThrow( ()-> new UserNotFoundException("없는 유저입니다.") );
        userRepository.delete(user);
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
