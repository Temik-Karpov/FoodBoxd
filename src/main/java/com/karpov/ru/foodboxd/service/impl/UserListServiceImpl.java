package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.entity.UserList;
import com.karpov.ru.foodboxd.model.entity.UserListItem;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.repository.UserListRepository;
import com.karpov.ru.foodboxd.repository.UserListItemRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import com.karpov.ru.foodboxd.service.UserListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса пользовательских списков.
 */
@Service
@RequiredArgsConstructor
public class UserListServiceImpl implements UserListService {

    private final UserListRepository userListRepository;
    private final UserListItemRepository userListItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserList createList(Long ownerId, String name, String description, boolean isPublic, ItemType itemType) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + ownerId + " не найден"));

        UserList list = UserList.builder()
                .owner(owner)
                .name(name)
                .description(description)
                .isPublic(isPublic)
                .itemType(itemType)
                .build();

        return userListRepository.save(list);
    }

    @Override
    @Transactional
    public UserListItem addItem(Long listId, ItemType itemType, Long itemId, Integer sortOrder) {
        UserList list = userListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Список с ID " + listId + " не найден"));

        UserListItem item = UserListItem.builder()
                .userList(list)
                .itemType(itemType)
                .itemId(itemId)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .build();

        return userListItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeItem(Long listId, Long itemId) {
        UserListItem item = userListItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Элемент списка с ID " + itemId + " не найден"));
        if (!item.getUserList().getId().equals(listId)) {
            throw new IllegalArgumentException("Элемент не принадлежит указанному списку");
        }
        userListItemRepository.delete(item);
    }

    @Override
    public List<UserList> getPublicLists(Long ownerId) {
        return userListRepository.findByOwnerIdAndIsPublicTrue(ownerId);
    }

    @Override
    public List<UserList> getAllLists(Long ownerId) {
        return userListRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public long countPublicLists(Long ownerId) {
        return userListRepository.countByOwnerIdAndIsPublicTrue(ownerId);
    }

    @Override
    public UserList getListById(Long id) {
        return userListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Список с ID " + id + " не найден"));
    }

    @Override
    @Transactional
    public void reorderItems(Long listId, List<Long> itemIds) {
        List<UserListItem> items = userListItemRepository.findByUserListIdOrderBySortOrderAsc(listId);
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            int finalI = i;
            items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setSortOrder(finalI);
                        userListItemRepository.save(item);
                    });
        }
    }
}