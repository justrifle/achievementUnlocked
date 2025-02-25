package example.service;

import example.dto.UserCreateDTO;
import example.dto.UserResponseDTO;
import example.entity.User;
import example.exception.IncorrectDataException;
import example.exception.ObjectAlreadyExistException;
import example.mapper.UserMapper;
import example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserResponseDTO addUser (UserCreateDTO userCreateDTO) {
        User user = userMapper.fromUserCreateDTO(userCreateDTO);
        checkConditionalsForAddUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        log.info("Пользователь %s добавлен.".formatted(user.getUsername()));
        return userMapper.toUserResponseDTO(user);
    }

    public void checkConditionalsForAddUser(User user){
        if (userRepository.findByUsername(user.getUsername()).isPresent()){
            String msg = "Пользователь с именем %s уже существует!".formatted(user.getUsername());
            log.error(msg);
            throw new ObjectAlreadyExistException(msg);
        } else if (userRepository.existsById(user.getId())) {
            String msg = "Пользователь с ID %s уже существует!".formatted(user.getId());
            log.error(msg);
            throw new ObjectAlreadyExistException(msg);
        }
        validateDate(user.getBirthDate().toString());
    }

    private void validateDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            String msg = "Дата %s некорректна. Введите дату в формате YYYY-MM-DD.".formatted(date);
            log.error(msg);
            throw new IncorrectDataException(msg);
        }
    }

}