'use strict'
import {AJAX, showFormMessage} from "./ajax.js";

document.querySelector('#resetpw').addEventListener('click', async (e) => {
    e.preventDefault();
    await submitForm();
});

document.querySelector('.form').addEventListener('submit', async () => {
    await submitForm();
})

const submitForm = async () => {
    // validation
    const email = document.querySelector('#input-email').value;

    const data = await AJAX('/resetpw', {
        email: email
    });
    showFormMessage(data, true, document.querySelector('.form'));
}