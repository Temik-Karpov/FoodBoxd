document.addEventListener('DOMContentLoaded', () => {
    const mapContainer = document.getElementById('yandex-map');
    if (!mapContainer) return;

    const address = mapContainer.dataset.address;
    const name = mapContainer.dataset.name;

    if (!address) return;

    ymaps.ready(() => {
        const geocoder = ymaps.geocode(address, { results: 1 });

        geocoder.then(result => {
            const firstGeoObject = result.geoObjects.get(0);
            if (!firstGeoObject) return;

            const coords = firstGeoObject.geometry.getCoordinates();
            const map = new ymaps.Map('yandex-map', {
                center: coords,
                zoom: 15,
                controls: ['zoomControl', 'fullscreenControl']
            });

            const placemark = new ymaps.Placemark(coords, {
                hintContent: name,
                balloonContent: `<strong>${name}</strong><br>${address}`
            }, {
                preset: 'islands#redIcon'
            });

            map.geoObjects.add(placemark);
            map.behaviors.disable('scrollZoom');
        }).catch(err => {
            console.error('Ошибка геокодирования:', err);
            mapContainer.innerHTML = '<p style="text-align:center;padding:20px;">Не удалось загрузить карту</p>';
        });
    });
});