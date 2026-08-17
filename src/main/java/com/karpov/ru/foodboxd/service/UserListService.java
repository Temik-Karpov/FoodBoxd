package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.model.entity.UserList;
import com.karpov.ru.foodboxd.model.entity.UserListItem;
import com.karpov.ru.foodboxd.model.enums.ItemType;

import java.util.List;

/**
 * Сервис для управления пользовательскими списками (подборками).
 */
public interface UserListService {

    /**
     * Создаёт новый список.
     * @param ownerId идентификатор владельца
     * @param name название списка
     * @param description описание
     * @param isPublic признак публичности
     * @param itemType тип элементов списка (RESTAURANT или MENU_ITEM)
     * @return созданный список
     */
    UserList createList(Long ownerId, String name, String description, boolean isPublic, ItemType itemType);

    /**
     * Добавляет элемент в список.
     * @param listId идентификатор списка
     * @param itemType тип элемента
     * @param itemId идентификатор элемента
     * @param sortOrder порядок сортировки
     * @return добавленный элемент
     */
    UserListItem addItem(Long listId, ItemType itemType, Long itemId, Integer sortOrder);

    /**
     * Удаляет элемент из списка.
     * @param listId идентификатор списка
     * @param itemId идентификатор элемента списка (UserListItem)
     */
    void removeItem(Long listId, Long itemId);

    /**
     * Возвращает публичные списки пользователя.
     * @param ownerId идентификатор пользователя
     * @return список публичных списков
     */
    List<UserList> getPublicLists(Long ownerId);

    /**
     * Возвращает все списки пользователя.
     * @param ownerId идентификатор пользователя
     * @return все списки
     */
    List<UserList> getAllLists(Long ownerId);

    /**
     * Возвращает список по идентификатору с элементами.
     * @param listId идентификатор списка
     * @return список
     */
    UserList getListById(Long listId);

    /**
     * Обновляет порядок элементов в списке на основе переданного массива идентификаторов элементов.
     * @param listId идентификатор списка
     * @param itemIds упорядоченный список идентификаторов UserListItem
     */
    void reorderItems(Long listId, List<Long> itemIds);
}