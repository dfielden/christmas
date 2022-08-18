import {AJAX, showFormMessage} from "./ajax.js";
import {Item} from "./Item.js";

window.addEventListener('load', async (e) => {
    const listId = getListId();
    const list = await AJAX(`/api/mylist/${listId}`);
    const listSharedStatus = await AJAX(`/api/listsharedstatus/${listId}`);
    for (const id in list) {
        const html = generateRowHtml(list[id], id, listSharedStatus);
        appendItemRow(html);
    }

    const emails = await AJAX(`/api/emails/${listId}`);
    for (const emailAddress in emails) {
        appendEmailAddress(generateEmailHtml(emailAddress, emails[emailAddress]));
    }

    document.querySelector('.heading-1').textContent = await AJAX(`/api/title/${listId}`);

    // get custom contacts and add to select
    const customContacts = await getCustomContacts();
    customContacts.forEach(contact => {
        let newOption = new Option(contact, contact);
        document.querySelector('#input-user').add(newOption, undefined);
    })
});

const getCustomContacts = async () => {
    return await AJAX('/api/customcontacts');
}

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
        link: row.querySelector('.url').textContent,
        additionalInfo: row.querySelector('.additional-info').textContent,
    }
}

const removeRowFromDom = (row) => {
    row.remove();
}

const closeAllForms = () => {
    closeItemForm();
    closeEmailForm()
    closeSendEmailCheck();
    closeNewEmailForm();
}

document.querySelectorAll('.form__close').forEach((el) => el.addEventListener('click', () => {
    closeAllForms();
    })
);

document.querySelector('#link-new-email').addEventListener('click', () => {
    document.querySelector('body').classList.add('body-hidden');
    openNewEmailForm();
});



document.querySelector('#btn-add-email').addEventListener('click', async () => {
    await addNewContact();
});

document.querySelector('#new-email-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    await addNewContact();
})

const addNewContact = async () => {
    // get email address
    const emailAddress = document.querySelector('#input-new-email').value.trim();
    console.log(emailAddress);

    // add as new user, visible only to current user
    const user = await AJAX('/newuser', emailAddress);

    // close add new email form
    closeNewEmailForm();

    // reopen share with form
    openEmailForm();
    let newOption = new Option(emailAddress, emailAddress);
    document.querySelector('#input-user').add(newOption, undefined);
    document.querySelector('#input-user').value = emailAddress;

}

document.querySelector('.confirm-send-email__close').addEventListener('click', () => {
    closeSendEmailCheck();
});

document.querySelector('#btn-cancelSendEmail').addEventListener('click', () => {
    closeSendEmailCheck();
});

document.querySelector('#btn-confirmSendEmail').addEventListener('click', () => {
    sendEmails();
})

document.querySelector('#new-item').addEventListener('click', () => {
    document.querySelector('body').classList.add('body-hidden');
    window.scrollTo(0,0);
    openItemForm();
});

document.querySelector('#submit-item').addEventListener('click', () => {
    submitAddItemForm();
});

document.querySelector('#edit-item').addEventListener('click', () => {
    document.querySelector('body').classList.add('body-hidden');
    window.scrollTo(0,0);
    editItemForm();
});

document.querySelector('#share-list').addEventListener('click', () => {
    document.querySelector('body').classList.add('body-hidden');
    openEmailForm();
})


const closeItemForm = () => {
    document.querySelector('body').classList.remove('body-hidden');
    document.querySelector('.form-add-edit').reset();
    document.querySelector('.new-entry').classList.add('display-none');
    document.querySelector('#submit-item').classList.remove('display-none');
    document.querySelector('#edit-item').classList.add('display-none');
    document.querySelector('.header-add-edit').textContent = 'Add new entry';
    document.querySelector('.form-msg').textContent = '';

}

const openNewEmailForm = () => {
    closeAllForms();
    document.querySelector('.add-new-email').classList.remove('display-none');
}

const closeNewEmailForm = () => {
    document.querySelector('#new-email-form').reset();
    document.querySelector('.add-new-email').classList.add('display-none');
}

const openItemForm = () => {
    document.querySelector('body').classList.add('body-hidden');
    window.scrollTo(0,0);
    document.querySelector('.new-entry').classList.remove('display-none');
}

const closeEmailForm = () => {
    document.querySelector('#email-form').reset();
    document.querySelector('.share-with').classList.add('display-none');
}

const closeSendEmailCheck = () => {
    document.querySelector('#email-form').style.zIndex = "10";
    document.querySelector('.confirm-send-email').classList.add('display-none');
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
        showFormMessage("Please complete in the Item/product row of this form", false, document.querySelector('.form-add-edit'));
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
    const listSharedStatus = await AJAX(`/api/listsharedstatus/${listId}`);
    const html = generateRowHtml(item, itemId, listSharedStatus);
    appendItemRow(html);
    closeItemForm();
};

const editItemForm = async () => {
    if (!document.querySelector('#input-product').value) {
        showFormMessage("Please complete in the Item/product row of this form", false, document.querySelector('.form-add-edit'));
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

const generateRowHtml = (item, id, boolListShared) => {
    return `
      <div class="row row-item" data-itemid="${id}">
        <div class="cell product pseudo-product">${item.product}</div>
        <div class="cell price pseudo-price">${convertPriceIfZero(item)}</div>
        <div class="cell location pseudo-location">${item.location}</div>
        <div class="cell url link pseudo-url"><a href="${item.url}" target="_blank">${item.url}</a></div>
        <div class="cell additional-info pseudo-additional-info">${item.additionalInfo}</div>
        ${boolListShared ? '<div class="cell btn-cell"></div>' : '<div class="cell btn-cell"><div class="btn btn--table btn--grey-light">Edit</div><div class="btn btn--table btn--danger">Delete</div></div>'}
      </div>
    `;
};

const updateRow = (row, item) => {
    row.querySelector('.product').textContent = item.product;
    row.querySelector('.price').textContent = item.price;
    row.querySelector('.location').textContent = item.location;
    row.querySelector('.url').textContent = item.url;
    row.querySelector('.additional-info').textContent = item.additionalInfo;
};

const convertPriceIfZero = (item) => {
    if (item.price) {
        return `£${Number((item.price)).toFixed(2)}`;
    }
    return '';
}

const appendItemRow = (html) => {
    document.querySelector('.list-table').insertAdjacentHTML('beforeend', html);
};


document.querySelector('.btn-form-single-row').addEventListener('click', async (e) => {
    e.preventDefault();

    // validate username
    const user = document.querySelector('#input-user').value.trim();
    if (!user) {
        return;
    }

    const alreadyAddedContacts = getContacts();
    for (let i = 0; i < alreadyAddedContacts.length; i++) {
        if (user === alreadyAddedContacts[i]) {
            return;
        }
    }

    // append
    const html = generateEmailHtml(user, false );
    appendEmailAddress(html);

    // add to db
    const listId = getListId();
    const url = `/email/${listId}`;
    await AJAX(url, user);

    // clear form
    document.querySelector('#email-form').reset();
});

const generateEmailHtml = (username, boolEmailSent, responseToSubmit = false) => {
    return `
        <div class="email-container">
            <div class="email-container--left">
                <i class="fas fa-user-alt"></i>
                <div class="email-address">${username}</div>   
            </div>
            <div class="email-container--right ${boolEmailSent ? 'success' : responseToSubmit ? 'danger' : ''}">
                ${boolEmailSent ? "<i class=\"far fa-envelope\"></i>Email sent" : "<i class=\"far fa-envelope\"></i>Email not sent<i class=\"fas fa-trash\"></i>"}
            </div>
        </div>
    `;
}


const appendEmailAddress = (html) => {
    document.querySelector('.email-list').insertAdjacentHTML('beforeend', html);
};

const getContacts = () => {
    const contactArray = [];
    const contacts = document.querySelectorAll('.email-address');
    contacts.forEach((el) => {
        contactArray.push(el.textContent);
    })
    return contactArray;
}

const getListId = () => {
    const currentUrl = window.location.href;
    return parseInt(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
}

document.addEventListener('click', async (e) => {
    if (e.target.classList.contains('fa-trash')) {
        const container = e.target.closest('.email-container');
        const emailAddress = container.querySelector('.email-address').textContent;
        container.remove();

        const listId = getListId();
        const url = `/deleteemail/${listId}`;
        await AJAX(url, emailAddress);
    }
});

document.querySelector('#btn-shareList').addEventListener('click', async (e) => {
    e.preventDefault();
    document.querySelector('.confirm-send-email').classList.remove('display-none');
    document.querySelector('#email-form').style.zIndex = "4";
});



const sendEmails = async () => {
    closeSendEmailCheck();
    const allContacts = getContacts();
    const listId = getListId();
    const url = `/sharelist/${listId}`;
    await AJAX(url, allContacts);
    await repopulateEmailsAfterSend();
    clearEditBtns();
}

const repopulateEmailsAfterSend = async () => {
    document.querySelector('.email-list').innerHTML = "";
    const listId = getListId();
    const emails = await AJAX(`/api/emails/${listId}`);
    for (const emailAddress in emails) {
        appendEmailAddress(generateEmailHtml(emailAddress, emails[emailAddress], true));
    }
}

const clearEditBtns = () => {
    const btnContainers = document.querySelectorAll('.btn-cell');
    btnContainers.forEach(el => el.innerHTML = '');
}
