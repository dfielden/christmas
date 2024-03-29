import {AJAX} from "./ajax.js";
import {Item} from "./Item.js";

window.addEventListener('load', async (e) => {
    const listId = getListId();
    const list = await AJAX(`/api/sharedlist/${listId}`);
    for (const id in list) {
        const html = await generateRowHtml(list[id], id);
        appendItemRow(html);
    }

    document.querySelector('.heading-1').textContent = await AJAX(`/api/title/${listId}`);

});


const generateRowHtml = async (item, id) => {
    return `
      <div class="row row-item ${item.selected ? 'row-selected' : ''} ${await isSelectedByMe(item) ? 'selected-me' : ''}" data-itemid="${id}">
        <div class="cell product pseudo-product">${item.product}</div>
        <div class="cell price pseudo-price">${convertPriceIfZero(item)}</div>
        <div class="cell location pseudo-location">${item.location}</div>
        <div class="cell url pseudo-url link"><a href="${item.url}" target="_blank">${item.url}</a></div>
        <div class="cell additional-info pseudo-additional-info">${item.additionalInfo}</div>
        <div class="cell btn-cell">
            ${await generateIsSelectedHtml(item)}
        </div>
      </div>
    `;
};

const generateIsSelectedHtml = async (item) => {
    if (!item.selected) {
        return `<div class="btn btn--table btn--primary">Select item</div>`;
    }

    if (await isSelectedByMe(item)) {
        return `<div class="btn btn--table btn--danger">Unselect</div>`
    }

    const selectedByUserName = await AJAX(`/api/username/${item.selectedBy}`);
    return `Selected by ${selectedByUserName}`;

}

const isSelectedByMe = async (item) => {
    return item.selectedBy === await AJAX(`/api/myuserid`);
}

document.addEventListener('click', async (e) => {
    if (e.target.classList.contains('btn--primary')) {

        const row = e.target.closest('.row');
        const itemId = row.dataset.itemid;
        const alreadySelected = await AJAX(`/api/selectitem/${itemId}`);
        if (alreadySelected) {
            row.querySelector('.btn-cell').innerHTML = `<p class="paragraph danger">Error: Item already selected by another user.</p>`;
            return;
        } else {
            row.querySelector('.btn-cell').innerHTML = `<div class="btn btn--table btn--danger">Unselect</div>`;
            styleAsSelected(row);
            styleAsSelectedByMe(row);
            return;
        }
    }
    if (e.target.classList.contains('btn--danger')) {
        const row = e.target.closest('.row');
        const itemId = row.dataset.itemid;
        await AJAX(`/api/deselectitem/${itemId}`);
        row.querySelector('.btn-cell').innerHTML = `<div class="btn btn--table btn--primary">Select item</div>`;
        styleAsNotSelected(row);
    }
})

const styleAsSelected = (row) => {
    row.classList.add('row-selected');
}

const styleAsSelectedByMe = (row) => {
    row.classList.add('selected-me');
}

const styleAsNotSelected = (row) => {
    row.classList.remove('row-selected');
    row.classList.remove('selected-me');
}


const convertPriceIfZero = (item) => {
    if (item.price) {
        return `£${Number((item.price)).toFixed(2)}`;
    }
    return '';
}

const appendItemRow = (html) => {
    document.querySelector('.list-table').insertAdjacentHTML('beforeend', html);
};


const getListId = () => {
    const currentUrl = window.location.href;
    return parseInt(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
}



