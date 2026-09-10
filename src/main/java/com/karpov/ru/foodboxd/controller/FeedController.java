package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.dto.FeedItemDto;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.service.FollowService;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * Контроллер персональной ленты подписок.
 */
@Controller
@RequiredArgsConstructor
public class FeedController {

    private static final int FEED_LIMIT = 50;

    private final UserService userService;
    private final FollowService followService;
    private final RatingService ratingService;

    /**
     * Отображает последние оценки и комментарии подписанных пользователей.
     * @param principal текущий пользователь
     * @param model модель представления
     * @return шаблон feed/index
     */
    @GetMapping("/feed")
    public String feed(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();

        List<User> following = followService.getFollowing(user.getId());
        List<Long> followingIds = following.stream().map(User::getId).toList();
        List<FeedItemDto> feedItems = ratingService.getFeed(followingIds, FEED_LIMIT);

        model.addAttribute("following", following);
        model.addAttribute("feedItems", feedItems);
        return "feed/index";
    }
}
