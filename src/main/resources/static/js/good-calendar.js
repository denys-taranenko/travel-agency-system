document.addEventListener('DOMContentLoaded', function() {
    var currentLocale = getCookie('LOCALE') || 'en';

    var localeConfig = {
        'uk': 'uk',
        'en': 'en',
    };

    flatpickr('#arrivalDate', {
        dateFormat: 'Y-m-d',
        minDate: 'today',
        locale: localeConfig[currentLocale] || 'en'
    });

    flatpickr('#evictionDate', {
        dateFormat: 'Y-m-d',
        minDate: 'today',
        locale: localeConfig[currentLocale] || 'en'
    });
});

function getCookie(name) {
    var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
}
