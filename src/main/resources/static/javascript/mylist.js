import {AJAX} from "./ajax.js";
import {Item} from "./Item.js";

document.querySelectorAll('.form__close').forEach((el) => el.addEventListener('click', () => {
    closeForm();
    })
);

document.querySelector('#new-item').addEventListener('click', () => {
    openForm();
});

document.querySelector('#submit-item').addEventListener('click', () => {
    submitAddItemForm();
});


const closeForm = () => {
    document.querySelector('.form').reset();
    document.querySelector('.new-entry').classList.add('display-none');
}

const openForm = () => {
    document.querySelector('.new-entry').classList.remove('display-none');
}

const submitAddItemForm = async () => {
    if (!document.querySelector('#input-product').value) {
        return;
    }

    const item = new Item(
        document.querySelector('#input-product').value,
        document.querySelector('#input-price').value,
        document.querySelector('#input-location').value,
        document.querySelector('#input-url').value,
        document.querySelector('#input-info').value
    );

    const data = await AJAX('/add/2', item);
    const html = generateRowHtml(data);
    appendRow(html);
    closeForm();
};

const generateRowHtml = (item) => {
    return `
      <div class="row">
        <div class="cell product">${item.product}</div>
        <div class="cell price">${convertPriceIfZero(item)}</div>
        <div class="cell location">${item.location}</div>
        <div class="cell link">${item.url}</div>
        <div class="cell additional-info">${item.additionalInfo}</div>
      </div>
    `;
};

const convertPriceIfZero = (item) => {
    if (item.price) {
        return `£${item.price}`;
    }
    return '';
}

const appendRow = (html) => {
    document.querySelector('.list-table').insertAdjacentHTML('beforeend', html);
};