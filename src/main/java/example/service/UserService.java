package example.service;

import example.dto.UserCreateDTO;
import example.dto.UserResponseDTO;
import example.entity.User;
import example.enums.Role;
import example.exception.AccessDeniedException;
import example.exception.IncorrectDataException;
import example.exception.ObjectAlreadyExistException;
import example.exception.ObjectNotFoundException;
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
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * ДОБАВИТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ
     * <p>
     * Данный метод используется как для регистрации новых пользователей, так и для добавления пользователей
     * администраторами. Перед сохранением производится проверка условий:
     * <ul>
     *     <li>Пользователь с таким именем не должен существовать.</li>
     *     <li>Пользователь с таким ID не должен существовать.</li>
     *     <li>Дата рождения должна соответствовать формату "YYYY-MM-DD".</li>
     * </ul>
     * Если все условия соблюдены, пароль пользователя хешируется, и он сохраняется в базе данных.
     * </p>
     *
     * @param userCreateDTO DTO с данными нового пользователя.
     * @return UserResponseDTO с данными добавленного пользователя.
     * @throws ObjectAlreadyExistException если пользователь с таким именем или ID уже существует.
     * @throws IncorrectDataException если введена некорректная дата рождения.
     */
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
        validateDateForAddUser(user.getBirthDate().toString());
    }

    private void validateDateForAddUser(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            String msg = "Дата %s некорректна. Введите дату в формате YYYY-MM-DD.".formatted(date);
            log.error(msg);
            throw new IncorrectDataException(msg);
        }
    }

    /**
     * ВЕРНУТЬ ПОЛЬЗОВАТЕЛЯ ПО ID
     * <p>
     * Данный метод используется для получения данных о пользователе.
     * Если пользователь с указанным ID не найден, выбрасывается исключение.
     * </p>
     *
     * @param id уникальный идентификатор пользователя.
     * @return DTO с данными найденного пользователя.
     * @throws ObjectNotFoundException если пользователь с данным ID не существует.
     */
    public UserResponseDTO getUserById(long id) {
        return userMapper.toUserResponseDTO(userRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Пользователь с ID %s не существует!".formatted(id);
                    log.error(msg);
                    return new ObjectNotFoundException(msg);
                }));
    }

    /**
     * ВЕРНУТЬ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
     * <p>
     * Данный метод используется для получения информации обо всех зарегистрированных пользователях.
     * Добавлю сюда сортировку по различным критериям.
     * </p>
     *
     * @return список DTO с данными всех пользователей.
     */
    public List<UserResponseDTO> getAllUsersById() {
        return userMapper.toListUserResponseDTO(userRepository.findAll());
    }

    /**
     * РЕДАКТИРОВАТЬ ИНФОРМАЦИЮ
     * <p>
     * Данный метод используется для обновления личных данных пользователя.
     * </p>
     *
     * @param updatedUserDTO DTO с обновленными данными пользователя.
     * @param logUsername    текущий логин пользователя, совершившего запрос.
     * @return DTO с обновленными данными пользователя.
     * @throws ObjectNotFoundException если пользователь не найден.
     * @throws ObjectAlreadyExistException если новый логин уже используется другим пользователем.
     */
    public UserResponseDTO updateUser(UserCreateDTO updatedUserDTO, String logUsername) {
        User user = userRepository.findByUsername(logUsername)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователя с именем %s не существует!"
                        .formatted(logUsername)));

        User updatedUser = userMapper.fromUserCreateDTO(updatedUserDTO);

        if (!updatedUser.getUsername().equals(logUsername)
                && userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
            String msg = "Пользователь с именем %s уже существует!".formatted(updatedUser.getUsername());
            log.error(msg);
            throw new ObjectAlreadyExistException(msg);
        }

        // Обновление разделов пользователя:
        user.setUsername(updatedUser.getUsername());
        user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setBio(updatedUser.getBio());

        userRepository.save(user);
        log.info("Информация о пользователе %s обновлена.".formatted(user.getUsername()));

        return userMapper.toUserResponseDTO(user);
    }

    /**
     * УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ ПО ID
     * <p>
     * Данный метод позволяет удалить пользователя из базы данных.
     * Удаление возможно только в случае, если пользователь удаляет свой собственный аккаунт
     * или обладает правами администратора.
     * </p>
     *
     * @param id идентификатор пользователя, которого требуется удалить.
     * @param logUsername текущий логин пользователя, совершившего запрос.
     * @throws ObjectNotFoundException если пользователь с данным ID не найден.
     * @throws AccessDeniedException если пользователь не является владельцем аккаунта и не имеет роли ADMIN.
     */
    public void deleteUserById(long id, String logUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Пользователь с ID %s не существует!".formatted(id);
                    log.error(msg);
                    return new ObjectNotFoundException(msg);
                });

        User requester = userRepository.findByUsername(logUsername)
                .orElseThrow(() -> {
                    String msg = "Пользователь, совершающий запрос, не найден: %s".formatted(logUsername);
                    log.error(msg);
                    return new ObjectNotFoundException(msg);
                });

        // Проверка прав: пользователь может удалить себя или быть администратором
        if (!requester.getUsername().equals(user.getUsername()) && requester.getRole() != Role.ADMIN) {
            String msg = "Пользователь %s не имеет прав для удаления аккаунта %s!"
                    .formatted(logUsername, user.getUsername());
            log.error(msg);
            throw new AccessDeniedException(msg);
        }

        userRepository.delete(user);
        log.info("Пользователь {} удалён из базы данных!", user.getUsername());
    }

    /**
     * ДОБАВИТЬ В ДАЛЬНЕЙШЕМ:
     * - ПОИСК ПОЛЬЗОВАТЕЛЕЙ ПО ЛОГИНУ / ИМЕНИ - ФАМИЛИИ
     */
}