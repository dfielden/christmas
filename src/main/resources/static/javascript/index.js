'use strict';

import {AJAX, showFormMessage} from "./ajax.js";

window.addEventListener('load', async (e) => {
    const lists = await AJAX(`/api/mylists`);

    if (lists === {}) {
        appendMyLists(`<p>Create a list of gift ideas :)</p>`);
    }

    for (const id in lists) {
        const html = generateListInfoHtml(id, lists[id]);
        appendMyLists(html);
    }

    document.querySelectorAll('.list-own').forEach(el => el.addEventListener('click', (e) => {
        const id = e.target.closest('.list-own').dataset.listid;
        window.location.href = `mylist/${id}`;
    }))

});

const generateListInfoHtml = (id, info) => {
    return `
        <div class="list-container list-own" data-listid="${id}">
            <div class="list-container--header">
                <h4 class="heading-4">${info.title}</h4>
            </div>
            <div class="list-created-on">Created: ${createDateText(new Date(info.timeCreated))}</div>
        </div>
    `;
}

const appendMyLists = (html) => {
    document.querySelector('.my-lists').insertAdjacentHTML('beforeend', html);
}


document.querySelectorAll('.form__close').forEach((el) => el.addEventListener('click', () => {
        closeForm();
    })
);

document.querySelector('.link--right').addEventListener('click', () => {
    document.querySelector('.new-list').classList.remove('display-none');
});

document.querySelector('#create-list').addEventListener('click', async () => {
    const title = document.querySelector('#input-title').value;
    if (!title) {
        showFormMessage("Please ensure all fields are completed", false, document.querySelector('.form'));
        return;
    }

    const listId = await AJAX('list/new', title);
    window.location.href = `/mylist/${listId}`;
});

const closeForm = () => {
    document.querySelector('.form').reset();
    document.querySelector('.new-list').classList.add('display-none');
}

const createDateText = (date) => {
    if (isToday(date)) {
        return `today`;
    }
    return `${date.getDay() < 10 ?'0':''}${date.getDay()} ${date.toLocaleString('default', {month: 'short'})} ${date.getFullYear()}`;
}

const isToday = (date) => {
    const today = new Date();
    return date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear();
}