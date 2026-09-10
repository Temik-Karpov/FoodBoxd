package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по email.
     * @param email адрес электронной почты
     * @return Optional с пользователем
     */
    Optional<User> findByEmail(String email);

    /**
     * Поиск пользователя по имени (username).
     * @param username уникальное имя пользователя
     * @return Optional с пользователем
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным email.
     * @param email адрес электронной почты
     * @return true, если такой email уже используется
     */
    boolean existsByEmail(String email);

    /**
     * Проверяет существование пользователя с указанным username.
     * @param username имя пользователя
     * @return true, если занято
     */
    boolean existsByUsername(String username);

    Optional<User> findByResetToken(String resetToken);

    /**
     * Возвращает всех пользователей, отсортированных по имени.
     * @return список пользователей
     */
    List<User> findAllByOrderByUsernameAsc();

    /**
     * Ищет пользователей по имени или городу без учёта регистра.
     * @param username часть имени пользователя
     * @param city часть названия города
     * @return найденные пользователи
     */
    List<User> findByUsernameContainingIgnoreCaseOrCityContainingIgnoreCaseOrderByUsernameAsc(String username, String city);
}
