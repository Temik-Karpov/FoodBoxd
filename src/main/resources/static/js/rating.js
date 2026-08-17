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

    function submitRating(ratingBlock) {
        const itemType = ratingBlock.dataset.itemType;
        const itemId = ratingBlock.dataset.itemId;
        const score = ratingBlock.querySelector('.rating-score-input').value;
        const reviewInput = ratingBlock.querySelector('.rating-review-input');
        const review = reviewInput ? reviewInput.value : '';

        if (!score || score === '0') return;

        const avgEl = ratingBlock.querySelector('.avg-rating-value');
        const prevAvg = avgEl ? avgEl.textContent : '-';

        if (avgEl) avgEl.textContent = '...';

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
                    return;
                }
                if (!response.ok) throw new Error('Ошибка сервера');
                return response.json();
            })
            .then(data => {
                if (data && avgEl && data.averageRating != null) {
                    avgEl.textContent = data.averageRating.toFixed(1);
                }
            })
            .catch(err => {
                console.error('Ошибка оценки:', err);
                if (avgEl) avgEl.textContent = prevAvg;
            });
    }
});

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