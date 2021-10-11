import {AJAX} from "./ajax.js";
import {Item} from "./Item.js";

window.addEventListener('load', async (e) => {
    const listId = getListId();
    const list = await AJAX(`/api/list/${listId}`);
    for (const id in list) {
        const html = generateRowHtml(list[id], id);
        appendItemRow(html);
    }

    const emails = JSON.parse(await AJAX(`/api/emails/${listId}`));
    for (let i = 0; i < emails.length; i++) {
        appendEmailAddress(generateEmailHtml(emails[i]));
    }

    document.querySelector('.heading-1').textContent = await AJAX(`/title/${listId}`);

});

document.querySelector('.list-table').addEventListener('click', async (e) => {
    if (e.target.classList.contains('btn--grey-light')) {
        const itemId = e.target.closest('.row').dataset.itemid;
        const rowInfo = getRowInfo(e.target.closest('.row'));
        openItemForm();
        populateItemForm(rowInfo);
        updateFormToEditItem(itemId);
    } else if (e.target.classList.contains('btn--danger')) {
        const itemId = e.target.closest('.row').dataset.itemid;
        await AJAX(`/deleteitem/${itemId}`);
        const row = e.target.closest('.row');
        removeRowFromDom(row);
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

const removeRowFromDom = (row) => {
    row.remove();
}

document.querySelectorAll('.form__close').forEach((el) => el.addEventListener('click', () => {
    closeItemForm();
    closeEmailForm()
    })
);

document.querySelector('#new-item').addEventListener('click', () => {
    openItemForm();
});

document.querySelector('#submit-item').addEventListener('click', () => {
    submitAddItemForm();
});

document.querySelector('#edit-item').addEventListener('click', () => {
    editItemForm();
});

document.querySelector('#share-list').addEventListener('click', () => {
    openEmailForm();
})


const closeItemForm = () => {
    document.querySelector('.form-add-edit').reset();
    document.querySelector('.new-entry').classList.add('display-none');
    document.querySelector('#submit-item').classList.remove('display-none');
    document.querySelector('#edit-item').classList.add('display-none');
    document.querySelector('.header-add-edit').textContent = 'Add new entry';
}

const openItemForm = () => {
    document.querySelector('.new-entry').classList.remove('display-none');
}

const closeEmailForm = () => {
    document.querySelector('#email-form').reset();
    document.querySelector('.share-with').classList.add('display-none');
}

const openEmailForm = () => {
    document.querySelector('.share-with').classList.remove('display-none');
}

const populateItemForm = (formParams) => {
    const form = document.querySelector('.form-add-edit');
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
    const itemId = await AJAX(`/add/${listId}`, item);
    const html = generateRowHtml(item, itemId);
    appendItemRow(html);
    closeItemForm();
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

    const rowToUpdate = document.querySelector(`[data-itemid="${itemId}"]`);
    updateRow(rowToUpdate, updatedItem);
    closeItemForm();
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

const appendItemRow = (html) => {
    document.querySelector('.list-table').insertAdjacentHTML('beforeend', html);
};


document.querySelector('.btn-form-single-row').addEventListener('click', async (e) => {
    e.preventDefault();

    // validate email
    const emailAddress = document.querySelector('#input-email').value.trim();
    if (!emailAddress) {
        return;
    }

    // append
    const html = generateEmailHtml(emailAddress);
    appendEmailAddress(html);

    // add to db
    await updateEmailsDB();

    // clear form
    document.querySelector('#email-form').reset();
});

const generateEmailHtml = (emailAddress) => {
    return `
        <div class="email-container">
            <div class="email-container--left">
                <i class="fas fa-user-alt"></i>
                <div class="email-address">${emailAddress}</div>   
            </div>
            <div class="email-container--right">
                <i class="fas fa-trash"></i>
            </div>
        </div>
    `;
}

const updateEmailsDB = async () => {
    const allEmailAddresses = getEmailAddresses();
    const listId = getListId();
    const url = `/email/${listId}`;
    await AJAX(url, JSON.stringify(allEmailAddresses));
}

const appendEmailAddress = (html) => {
    document.querySelector('.email-list').insertAdjacentHTML('beforeend', html);
};

const getEmailAddresses = () => {
    const emailArray = [];
    const emails = document.querySelectorAll('.email-address');
    emails.forEach((el) => {
        emailArray.push(el.textContent);
    })
    return emailArray;
}

const getListId = () => {
    const currentUrl = window.location.href;
    return parseInt(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
}

document.addEventListener('click', async (e) => {
    if (e.target.classList.contains('fa-trash')) {
        const container = e.target.closest('.email-container');
        container.remove();
        await updateEmailsDB();
    }
});

document.querySelector('#btn-shareList').addEventListener('click', async () => {
    sendEmails();
});

const sendEmails = async () => {
    const allEmailAddresses = getEmailAddresses();
    const listId = getListId();
    const url = `/sharelist/${listId}`;
    await AJAX(url, allEmailAddresses);
}