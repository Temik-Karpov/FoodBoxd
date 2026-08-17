document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-input');
    if (!searchInput) return;

    const searchResults = document.getElementById('search-results');
    const listId = searchInput.dataset.listId;
    const sortableList = document.getElementById('sortable-list');

    let searchTimeout;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        const query = this.value.trim();
        if (query.length < 2) {
            searchResults.innerHTML = '';
            searchResults.classList.remove('open');
            return;
        }
        searchTimeout = setTimeout(() => {
            fetch(`/lists/${listId}/search?query=${encodeURIComponent(query)}`)
                .then(r => r.json())
                .then(data => {
                    searchResults.innerHTML = '';
                    if (data.length === 0) {
                        searchResults.innerHTML = '<div class="search-result-item">Ничего не найдено</div>';
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
                            searchResults.appendChild(div);
                        });
                    }
                    searchResults.classList.add('open');
                })
                .catch(err => console.error('Ошибка поиска:', err));
        }, 300);
    });

    document.addEventListener('click', function(e) {
        if (searchInput && !searchInput.contains(e.target) && searchResults && !searchResults.contains(e.target)) {
            searchResults.classList.remove('open');
        }
    });

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