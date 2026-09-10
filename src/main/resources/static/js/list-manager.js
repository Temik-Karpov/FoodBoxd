document.addEventListener('DOMContentLoaded', () => {
    const searchBlock = document.querySelector('.search-block');
    const dishInput = document.getElementById('search-input');
    const restaurantInput = document.getElementById('restaurant-search-input');

    if (!dishInput && !restaurantInput) return;

    const listId = searchBlock
        ? searchBlock.dataset.listId
        : (dishInput ? dishInput.dataset.listId : null);
    const sortableList = document.getElementById('sortable-list');

    let selectedRestaurantId = null;

    // ===== Шаг 1: выбор ресторана (только для списков блюд и напитков) =====
    if (restaurantInput) {
        const restaurantResults = document.getElementById('restaurant-search-results');
        const restaurantWrap = document.getElementById('restaurant-search-wrap');
        const selectedBox = document.getElementById('selected-restaurant');
        const selectedName = selectedBox.querySelector('.selected-restaurant-name');
        const dishBlock = document.getElementById('dish-search-block');
        const storageKey = `list:${listId}:selectedRestaurant`;
        let restaurantTimeout;

        function applyRestaurant(id, name) {
            selectedRestaurantId = id;
            selectedName.textContent = name;
            restaurantResults.classList.remove('open');
            restaurantWrap.hidden = true;
            selectedBox.hidden = false;
            dishBlock.hidden = false;
        }

        function saveRestaurant(id, name) {
            try {
                sessionStorage.setItem(storageKey, JSON.stringify({ id: id, name: name }));
            } catch (e) {
                // хранилище может быть недоступно (приватный режим) — не критично
            }
        }

        function loadRestaurant() {
            try {
                const raw = sessionStorage.getItem(storageKey);
                return raw ? JSON.parse(raw) : null;
            } catch (e) {
                return null;
            }
        }

        function clearRestaurant() {
            try {
                sessionStorage.removeItem(storageKey);
            } catch (e) {
                // ignore
            }
        }

        // Восстанавливаем ресторан после перезагрузки страницы, чтобы не выбирать его заново
        const savedRestaurant = loadRestaurant();
        if (savedRestaurant && savedRestaurant.id != null) {
            applyRestaurant(savedRestaurant.id, savedRestaurant.name || '');
        }

        restaurantInput.addEventListener('input', function() {
            clearTimeout(restaurantTimeout);
            const query = this.value.trim();
            if (query.length < 2) {
                restaurantResults.innerHTML = '';
                restaurantResults.classList.remove('open');
                return;
            }
            restaurantTimeout = setTimeout(() => {
                fetch(`/lists/${listId}/restaurants?query=${encodeURIComponent(query)}`)
                    .then(r => r.json())
                    .then(data => {
                        restaurantResults.innerHTML = '';
                        if (data.length === 0) {
                            restaurantResults.innerHTML = '<div class="search-result-item">Ничего не найдено</div>';
                        } else {
                            data.forEach(item => {
                                const div = document.createElement('div');
                                div.className = 'search-result-item';
                                div.innerHTML = `<strong>${escapeHtml(item.name)}</strong>` +
                                    (item.address ? ` <span class="muted">${escapeHtml(item.address)}</span>` : '');
                                div.addEventListener('click', () => selectRestaurant(item));
                                restaurantResults.appendChild(div);
                            });
                        }
                        restaurantResults.classList.add('open');
                    })
                    .catch(err => console.error('Ошибка поиска ресторанов:', err));
            }, 300);
        });

        document.addEventListener('click', function(e) {
            if (!restaurantWrap.contains(e.target) && !restaurantResults.contains(e.target)) {
                restaurantResults.classList.remove('open');
            }
        });

        document.getElementById('change-restaurant-btn').addEventListener('click', function() {
            clearRestaurant();
            selectedRestaurantId = null;
            selectedBox.hidden = true;
            dishBlock.hidden = true;
            restaurantWrap.hidden = false;
            restaurantInput.value = '';
            if (dishInput) {
                dishInput.value = '';
                const results = document.getElementById('search-results');
                if (results) {
                    results.innerHTML = '';
                    results.classList.remove('open');
                }
            }
            restaurantInput.focus();
        });

        function selectRestaurant(item) {
            applyRestaurant(item.id, item.name);
            saveRestaurant(item.id, item.name);
            if (dishInput) dishInput.focus();
        }
    }

    // ===== Шаг 2: поиск блюд (или ресторанов — для списка заведений) =====
    if (dishInput) {
        const dishResults = document.getElementById('search-results');
        let dishTimeout;

        dishInput.addEventListener('input', function() {
            clearTimeout(dishTimeout);
            const query = this.value.trim();
            if (query.length < 2) {
                dishResults.innerHTML = '';
                dishResults.classList.remove('open');
                return;
            }
            dishTimeout = setTimeout(() => {
                let url = `/lists/${listId}/search?query=${encodeURIComponent(query)}`;
                if (restaurantInput && selectedRestaurantId != null) {
                    url += `&restaurantId=${selectedRestaurantId}`;
                }
                fetch(url)
                    .then(r => r.json())
                    .then(data => {
                        dishResults.innerHTML = '';
                        if (data.length === 0) {
                            dishResults.innerHTML = '<div class="search-result-item">Ничего не найдено</div>';
                        } else {
                            data.forEach(item => {
                                const div = document.createElement('div');
                                div.className = 'search-result-item';
                                if (item.type === 'RESTAURANT') {
                                    div.innerHTML = `<strong>${escapeHtml(item.name)}</strong> <span class="muted">${escapeHtml(item.address)}</span>`;
                                } else {
                                    div.innerHTML = `<strong>${escapeHtml(item.name)}</strong> <span class="muted">${escapeHtml(item.restaurantName)} — ${escapeHtml(item.price)} ₽</span>`;
                                }
                                div.addEventListener('click', function() {
                                    addToList(listId, item.id);
                                });
                                dishResults.appendChild(div);
                            });
                        }
                        dishResults.classList.add('open');
                    })
                    .catch(err => console.error('Ошибка поиска:', err));
            }, 300);
        });

        document.addEventListener('click', function(e) {
            if (!dishInput.contains(e.target) && dishResults && !dishResults.contains(e.target)) {
                dishResults.classList.remove('open');
            }
        });
    }

    function addToList(listId, itemId) {
        fetch(`/lists/${listId}/add?itemId=${itemId}`, { method: 'POST' })
            .then(r => {
                if (!r.ok) throw new Error('Ошибка сервера');
                return r.json();
            })
            .then(() => {
                location.reload();
            })
            .catch(err => {
                console.error('Ошибка добавления:', err);
                alert('Не удалось добавить элемент');
            });
    }

    // Удаление элемента
    document.querySelectorAll('.remove-item-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const lid = this.dataset.listId;
            const itemId = this.dataset.itemId;
            if (!confirm('Удалить элемент из списка?')) return;
            fetch(`/lists/${lid}/remove/${itemId}`, { method: 'DELETE' })
                .then(r => r.json())
                .then(() => location.reload())
                .catch(err => console.error('Ошибка удаления:', err));
        });
    });

    // Drag-and-drop
    if (sortableList) {
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/sortablejs@1.15.0/Sortable.min.js';
        script.onload = function() {
            new Sortable(sortableList, {
                handle: '.drag-handle',
                animation: 150,
                onEnd: function() {
                    const itemIds = [...sortableList.querySelectorAll('.sortable-item')]
                        .map(el => parseInt(el.dataset.itemId));
                    fetch(`/lists/${listId}/reorder`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(itemIds)
                    }).catch(err => console.error('Ошибка сортировки:', err));
                }
            });
        };
        document.head.appendChild(script);
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});
