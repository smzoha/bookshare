function bindTogglePasswordEventListener(btnId, fieldId) {
    $(btnId).on('mousedown touchstart', function () {
        $(fieldId).attr('type', 'text');
    }).on('mouseup mouseleave touchend', function () {
        $(fieldId).attr('type', 'password');
    });
}