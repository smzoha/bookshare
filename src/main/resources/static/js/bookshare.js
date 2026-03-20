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

function initSearchBar(id) {
    $(id).autocomplete({
        source: function (request, response) {
            $.ajax({
                method: 'GET',
                url: '/book/search',
                data: {
                    query: request.term
                },
                success: function (result) {
                    response(result);
                },
                error: function () {
                    console.log('Something went wrong fetching search data.');
                }
            });
        },
        minLength: 3,
        create: function () {
            $(this).data('ui-autocomplete')._renderItem = function (ul, item) {
                let imageUrl = item.imageId ? `/image/${item.imageId}` : '/img/book-placeholder.png';

                let $li = $('<li class="ui-menu-item" style="cursor:pointer;">')
                    .on('click', function () {
                        window.location.href = `/book/${item.id}`;
                    });

                $('<div class="d-flex align-items-center gap-3 px-3 py-2 rounded-3 mx-1 my-1">')
                    .html(`<img src="${imageUrl}" alt="${item.title}"
                                class="rounded-2 border object-fit-cover flex-shrink-0"
                                style="width:44px; height:60px;">

                            <div class="flex-grow-1 overflow-hidden">
                                <span class="fw-medium d-block text-truncate">${item.title}</span>
                                <span class="text-muted small d-block text-truncate">${item.authors}</span>
                            </div>`)
                    .appendTo($li);

                return $li.appendTo(ul);
            };
        }
    });
}
