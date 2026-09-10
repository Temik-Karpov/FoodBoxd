document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.star-rating').forEach(container => {
        const stars = container.querySelectorAll('.star');
        const ratingBlock = container.closest('.rating-block');
        const hiddenInput = ratingBlock.querySelector('.rating-score-input');

        const currentValue = parseFloat(hiddenInput.value) || 0;
        highlightStars(container, currentValue);

        stars.forEach(star => {
            star.addEventListener('click', function() {
                const value = parseFloat(this.dataset.value);
                hiddenInput.value = value;
                highlightStars(container, value);
                submitRating(ratingBlock);
            });

            star.addEventListener('mouseenter', function() {
                const value = parseFloat(this.dataset.value);
                highlightStars(container, value);
            });
        });

        container.addEventListener('mouseleave', function() {
            const value = parseFloat(hiddenInput.value) || 0;
            highlightStars(container, value);
        });
    });

    // Отдельная кнопка отправки отзыва (без обязательной оценки звёздами)
    document.querySelectorAll('.review-submit-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const ratingBlock = btn.closest('.rating-block');
            submitRating(ratingBlock, { viaButton: true });
        });
    });
});

function submitRating(ratingBlock, opts = {}) {
    const itemType = ratingBlock.dataset.itemType;
    const itemId = ratingBlock.dataset.itemId;
    const hiddenInput = ratingBlock.querySelector('.rating-score-input');
    const score = parseFloat(hiddenInput.value) || 0;
    const reviewInput = ratingBlock.querySelector('.rating-review-input');
    const review = reviewInput ? reviewInput.value.trim() : '';

    const avgEl = ratingBlock.querySelector('.avg-rating-value');
    const statusEl = ratingBlock.querySelector('.review-composer-status');
    const btn = ratingBlock.querySelector('.review-submit-btn');
    const prevAvg = avgEl ? avgEl.textContent : '-';

    // Оценка обязательна — без неё отзыв не отправляем
    if (score <= 0) {
        setStatus(statusEl, 'Поставьте оценку звёздами', 'error');
        return;
    }

    setStatus(statusEl, 'Отправка...', 'pending');
    if (btn) btn.disabled = true;

    const params = new URLSearchParams();
    params.append('score', score);
    if (review) params.append('review', review);

    fetch(`/rate/${itemType}/${itemId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(response => {
            if (response.status === 401) {
                window.location.href = '/login';
                throw new Error('redirect');
            }
            return response.json().then(data => ({ ok: response.ok, data }));
        })
        .then(({ ok, data }) => {
            if (!ok) throw new Error(data.error || 'Ошибка сервера');

            // Мгновенное обновление среднего рейтинга
            if (avgEl && data.averageRating != null) {
                avgEl.textContent = data.averageRating.toFixed(1);
            }

            // Мгновенное обновление «Ваша оценка»
            updateMyRating(ratingBlock, data.userScore);

            // Мгновенное обновление списка отзывов
            updateReviewList(data.review);

            // Поле очищается после сохранения
            if (reviewInput) reviewInput.value = '';

            if (opts.viaButton) {
                const message = review
                    ? 'Оценка и отзыв сохранены'
                    : 'Оценка сохранена';
                setStatus(statusEl, message, 'success');
                setTimeout(() => setStatus(statusEl, '', 'idle'), 3500);
            }
        })
        .catch(err => {
            if (err.message === 'redirect') return;
            console.error('Ошибка оценки:', err);
            setStatus(statusEl, 'Не удалось отправить. Попробуйте ещё раз.', 'error');
            if (avgEl) avgEl.textContent = prevAvg;
        })
        .finally(() => {
            if (btn) btn.disabled = false;
        });
}

function setStatus(el, message, state) {
    if (!el) return;
    el.textContent = message;
    el.classList.remove('success', 'error', 'pending', 'idle');
    if (message) el.classList.add(state);
}

/* ===== Живое обновление списка отзывов ===== */

function updateReviewList(reviewData) {
    const block = document.querySelector('.reviews-block');
    if (!block || !reviewData) return;

    const list = block.querySelector('.reviews-list');
    if (!list) return;

    const username = reviewData.username || '';
    const reviewText = (reviewData.review || '').trim();

    // Отзыв очищен (например, оценка без текста) — убираем элемент пользователя
    if (!reviewText) {
        const existing = list.querySelector('.review-item[data-username="' + CSS.escape(username) + '"]');
        if (existing) {
            existing.remove();
            updateReviewCount(block, -1);
            showReviewsEmpty(block, true);
        }
        return;
    }

    const html = buildReviewItemHtml(reviewData);
    const existing = list.querySelector('.review-item[data-username="' + CSS.escape(username) + '"]');

    if (existing) {
        // Пользователь уже оставлял отзыв — обновляем его в списке
        existing.outerHTML = html;
    } else {
        // Новый отзыв — вставляем в начало списка
        showReviewsEmpty(block, false);
        const node = createElementFromHtml(html);
        list.prepend(node);
        updateReviewCount(block, +1);
    }
}

function buildReviewItemHtml(data) {
    const esc = escapeHtml;
    const scoreBadge = data.score != null
        ? '<span class="review-score"><i class="fas fa-star"></i> <span>' + Number(data.score).toFixed(1).replace('.', ',') + '</span></span>'
        : '<span class="review-no-score">без оценки</span>';

    return '<div class="review-item" data-username="' + esc(data.username) + '">' +
        '<div class="review-header">' +
            '<img src="' + esc(data.avatarUrl) + '" alt="Аватар" class="review-avatar">' +
            '<div class="review-meta">' +
                '<strong>' + esc(data.username) + '</strong>' +
                scoreBadge +
                '<span class="review-date">' + esc(data.createdAt || '') + '</span>' +
            '</div>' +
        '</div>' +
        '<div class="review-body">' + esc(data.review) + '</div>' +
    '</div>';
}

function createElementFromHtml(html) {
    const wrapper = document.createElement('div');
    wrapper.innerHTML = html;
    return wrapper.firstElementChild;
}

function updateMyRating(ratingBlock, userScore) {
    const info = ratingBlock.querySelector('.rating-info');
    if (!info) return;

    let chip = ratingBlock.querySelector('.my-rating');
    if (userScore != null) {
        if (!chip) {
            chip = document.createElement('span');
            chip.className = 'my-rating';
            chip.innerHTML = '<i class="fas fa-star"></i> Ваша оценка: <span class="my-rating-value"></span>';
            info.appendChild(chip);
        }
        chip.querySelector('.my-rating-value').textContent =
            Number(userScore).toFixed(1).replace('.', ',');
    } else if (chip) {
        chip.remove();
    }
}

function updateReviewCount(block, delta) {
    const countEl = block.querySelector('.reviews-count');
    if (!countEl) return;
    const current = parseInt(countEl.textContent, 10) || 0;
    countEl.textContent = Math.max(0, current + delta);
}

function showReviewsEmpty(block, visible) {
    let empty = block.querySelector('.reviews-empty');
    if (visible && !empty) {
        empty = document.createElement('div');
        empty.className = 'empty-state reviews-empty';
        empty.innerHTML = '<i class="fas fa-comments"></i> Пока нет комментариев.';
        const list = block.querySelector('.reviews-list');
        if (list) list.after(empty);
    } else if (!visible && empty) {
        empty.remove();
    }
}

function escapeHtml(value) {
    return String(value == null ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function highlightStars(container, value) {
    const stars = container.querySelectorAll('.star');
    stars.forEach(star => {
        const starValue = parseFloat(star.dataset.value);
        star.classList.remove('full', 'half');
        if (starValue <= value) {
            star.classList.add('full');
        } else if (starValue - 0.5 <= value && starValue > value) {
            star.classList.add('half');
        }
    });
}
