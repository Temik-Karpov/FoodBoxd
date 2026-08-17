package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.UserListItem;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с элементами пользовательских списков.
 */
public interface UserListItemRepository extends JpaRepository<UserListItem, Long> {

    /**
     * Находит все элементы списка, упорядоченные по полю сортировки.
     * @param userListId ID списка
     * @return отсортированный список элементов
     */
    List<UserListItem> findByUserListIdOrderBySortOrderAsc(Long userListId);

    /**
     * Удаляет элемент списка, если он ссылается на указанный объект.
     * Используется при удалении ресторана или блюда из системы.
     * @param itemType тип объекта
     * @param itemId ID объекта
     */
    void deleteByItemTypeAndItemId(ItemType itemType, Long itemId);
}