function bindTogglePasswordEventListener(btnId, fieldId) {
    $(btnId).on('mousedown touchstart', function () {
        $(fieldId).attr('type', 'text');
    }).on('mouseup mouseleave touchend', function () {
        $(fieldId).attr('type', 'password');
    });
}

function imageModalLoad(e, button) {
    e.preventDefault();

    let imageUrl = $(button).data('load-url');

    $('#image-modal').modal('show')
        .find('.modal-body')
        .html(`<img src="${imageUrl}" class="img-fluid" alt="Image"/>`);
}

function showLoader() {
    let loaderContainer = $('<div>', {id: 'loader'});
    let loader = $('<div>', {
        class: 'spinner-grow text-bookshare',
        role: 'status'
    });

    loaderContainer.append(loader);
    $('body').append(loaderContainer);
}

function removeLoader() {
    $('#loader').remove();
}
