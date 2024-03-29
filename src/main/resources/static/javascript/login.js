'use strict'
import {AJAX, showFormMessage} from "./ajax.js";
const LOGIN_SUCCESS_VALUE = 'LOGIN_SUCCESS'; // must match PSFS LOGIN_SUCCESS_RESPONSE_VALUE in ChristmasListApplication.java

document.querySelector('#login').addEventListener('click', async (e) => {
    e.preventDefault();
    await submitForm();
});

document.querySelector('.form').addEventListener('submit', async () => {
    await submitForm();
})

const submitForm = async () => {
    // validation
    const username = document.querySelector('#input-user').value;
    const pw = document.querySelector('#input-password').value;

    const data = await AJAX('/login', {
        username: username,
        password: pw,
        email: ""
    });

    if (data === LOGIN_SUCCESS_VALUE) {
        showFormMessage("Login successful!", true, document.querySelector('.form'));
        setTimeout(() => {
            window.location.href = "/";
        }, 500)
    } else {
        showFormMessage(data, false, document.querySelector('.form'));
    }
}