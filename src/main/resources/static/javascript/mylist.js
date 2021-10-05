import {AJAX} from "./ajax.js";
import {Item} from "./Item.js";

window.addEventListener('load', async (e) => {
    const currentUrl = window.location.href;
    const listId = parseInt(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
    const list = await AJAX(`/api/list/${listId}`);
    for (const id in list) {
        const html = generateRowHtml(list[id], id);
        appendRow(html);
    }

    document.querySelector('.heading-1').textContent = await AJAX(`/title/${listId}`);
    console.log(list);
});

document.querySelector('.list-table').addEventListener('click', (e) => {
    if (e.target.classList.contains('btn--grey-light')) {
        const itemId = e.target.closest('.row').dataset.itemid;
        const rowInfo = getRowInfo(e.target.closest('.row'));
        openForm();
        populateForm(rowInfo);
        updateFormToEditItem(itemId);
    }
})

const getRowInfo = (row) => {
    return {
        product: row.querySelector('.product').textContent,
        price: row.querySelector('.price').textContent,
        location: row.querySelector('.location').textContent,
        link: row.querySelector('.link').textContent,
        additionalInfo: row.querySelector('.additional-info').textContent,
    }
}

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

document.querySelector('#edit-item').addEventListener('click', () => {
    editItemForm();
})


const closeForm = () => {
    document.querySelector('.form').reset();
    document.querySelector('.new-entry').classList.add('display-none');
    document.querySelector('#submit-item').classList.remove('display-none');
    document.querySelector('#edit-item').classList.add('display-none');
    document.querySelector('.header-add-edit').textContent = 'Add new entry';
}

const openForm = () => {
    document.querySelector('.new-entry').classList.remove('display-none');
}

const populateForm = (formParams) => {
    console.log(formParams);
    const form = document.querySelector('.form');
    form.querySelector('#input-product').value = formParams.product;
    form.querySelector('#input-price').value = parseFloat(formParams.price.substring(1));
    form.querySelector('#input-location').value = formParams.location;
    form.querySelector('#input-url').value = formParams.link;
    form.querySelector('#input-info').value = formParams.additionalInfo;
}

const updateFormToEditItem = (itemId) => {
    document.querySelector('#submit-item').classList.add('display-none');
    document.querySelector('#edit-item').classList.remove('display-none');
    document.querySelector('.header-add-edit').textContent = 'Edit item';
    document.querySelector('#id').value  = itemId;

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

    const currentUrl = window.location.href;
    const listId = parseInt(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
    item.id = await AJAX(`/add/${listId}`, item);
    const html = generateRowHtml(item);
    appendRow(html);
    closeForm();
};

const editItemForm = async () => {
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

    const itemId = document.querySelector('#id').value;
    const updatedItem = await AJAX(`/edit/${itemId}`, item);
    console.log(updatedItem);

    const rowToUpdate = document.querySelector(`[data-itemid="${itemId}"]`);
    console.log(rowToUpdate);
    updateRow(rowToUpdate, updatedItem);
    closeForm();
};

const generateRowHtml = (item, id) => {
    return `
      <div class="row" data-itemid="${id}">
        <div class="cell product">${item.product}</div>
        <div class="cell price">${convertPriceIfZero(item)}</div>
        <div class="cell location">${item.location}</div>
        <div class="cell link">${item.url}</div>
        <div class="cell additional-info">${item.additionalInfo}</div>
        <div class="cell btn-cell">
            <div class="btn btn--table btn--grey-light">Edit</div>
            <div class="btn btn--table btn--danger">Delete</div>
        </div>
      </div>
    `;
};

const updateRow = (row, item) => {
    row.querySelector('.product').textContent = item.product;
    row.querySelector('.price').textContent = item.price;
    row.querySelector('.location').textContent = item.location;
    row.querySelector('.link').textContent = item.url;
    row.querySelector('.additional-info').textContent = item.additionalInfo;
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