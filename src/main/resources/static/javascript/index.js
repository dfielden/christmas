'use strict';

import {AJAX, showFormMessage} from "./ajax.js";

window.addEventListener('load', async (e) => {
    const myLists = await AJAX(`/api/mylists`);

    if (isEmptyObject(myLists)) {
        appendMyLists(`<p>Create a list of gift ideas :)</p>`);
    }

    for (const id in myLists) {
        const html = generateListInfoHtml(id, myLists[id]);
        appendMyLists(html);
    }

    const sharedWithMe = await AJAX(`/api/sharedlists`);
    if (isEmptyObject(sharedWithMe)) {
        appendSharedWithMe(`<p>Nobody has shared any lists with you yet...</p>`);
    }

    for (const id in sharedWithMe) {
        const html = await generateListInfoSharedHtml(id, sharedWithMe[id]);
        appendSharedWithMe(html);
    }

    document.querySelectorAll('.list-own').forEach(el => el.addEventListener('click', (e) => {
        const id = e.target.closest('.list-own').dataset.listid;
        window.location.href = `mylist/${id}`;
    }));

    document.querySelectorAll('.list-shared').forEach(el => el.addEventListener('click', (e) => {
        const listId = e.target.closest('.list-shared').dataset.listid;
        window.location.href = `/sharedlist/${listId}`;
    }));

    document.querySelectorAll('.fa-trash').forEach(el => el.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = e.target.closest('.list-own').dataset.listid;
        openDeleteModal(id);
    }));

    const userName = await AJAX('/api/ownusername');
    document.querySelector('.heading-1').textContent = `Welcome, ${userName}`;

});



const generateListInfoHtml = (id, info) => {
    return `
        <div class="list-container list-own" data-listid="${id}">
            <div class="list-own-left">
                <div class="list-container--header">
                    <h4 class="heading-4">${info.title}</h4>
                </div>
                <div class="list-created-on">Created: ${createDateText(new Date(info.timeCreated))}</div>
            </div>
            <div class="list-own-right">
                <i class="fas fa-trash"></i>
            </div>
        </div>
    `;
}

const generateListInfoSharedHtml = async (listId, info) => {
    const createdBy = await getCreatorUserName(info.creatorId);

    return `
        <div class="list-container flex-col list-shared" data-listid="${listId}">
            <div class="list-own-left">
                <div class="list-container--header">
                    <h4 class="heading-4">${info.title}</h4>
                </div>
                <div class="list-created-on">Created by: ${createdBy}, ${createDateText(new Date(info.timeCreated))}</div>
            </div>
            <div class="list-own-right">
            </div>
        </div>
    `;
}

const appendMyLists = (html) => {
    document.querySelector('.my-lists').insertAdjacentHTML('beforeend', html);
}

const appendSharedWithMe = (html) => {
    document.querySelector('.all-shared-lists').insertAdjacentHTML('beforeend', html);
}

document.querySelectorAll('.form__close').forEach((el) => el.addEventListener('click', () => {
        closeForm();
    })
);

document.querySelector('#create-new-list').addEventListener('click', () => {
    document.querySelector('.new-list').classList.remove('display-none');
});

document.querySelector('#btn-cancelDelete').addEventListener('click', () => {
    closeForm();
});

document.querySelector('#btn-deleteList').addEventListener('click', async (e) => {
    const listId = document.querySelector('#id').value;
    await AJAX(`/deletelist/${listId}`);
    const elToRemove = document.querySelector(`.list-own[data-listid="${listId}"`);
    elToRemove.remove();
    closeForm();
});

document.querySelector('#create-list').addEventListener('click', async () => {
    await submitCreateListForm();
});

document.querySelector('.form').addEventListener('submit', async (e) => {
    e.preventDefault();
    await submitCreateListForm();
})

const submitCreateListForm = async () => {
    const title = document.querySelector('#input-title').value;
    if (!title) {
        showFormMessage("Please ensure all fields are completed", false, document.querySelector('.form'));
        return;
    }

    const listId = await AJAX('list/new', title);
    window.location.href = `/mylist/${listId}`;
}

const getCreatorUserName = async (userId) => {
    return await AJAX(`/api/username/${userId}`);
}

document.querySelectorAll('.list-shared').forEach((el) => el.addEventListener('click', (e) => {
    const listId = e.target.dataset.listid;
    window.location.href = `/sharedlist/${listId}`;
}));

const closeForm = () => {
    document.querySelector('.form').reset();
    document.querySelector('.new-list').classList.add('display-none');
    document.querySelector('.confirm-delete').classList.add('display-none');
    document.querySelector('#id').value = -1;
}

const openDeleteModal = (listId) => {
    document.querySelector('.confirm-delete').classList.remove('display-none');
    document.querySelector('#id').value = listId;
}

const createDateText = (date) => {
    if (isToday(date)) {
        return `today`;
    }
    return `${date.getDate() < 10 ?'0':''}${date.getDate()} ${date.toLocaleString('default', {month: 'short'})} ${date.getFullYear()}`;
}

const isToday = (date) => {
    const today = new Date();
    return date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear();
}

const isEmptyObject = (obj) => {
    return Object.keys(obj).length === 0;
}