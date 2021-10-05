'use strict'
import {AJAX, showFormMessage} from "./ajax.js";
const LOGIN_SUCCESS_VALUE = 'LOGIN_SUCCESS'; // must match PSFS LOGIN_SUCCESS_RESPONSE_VALUE in ChristmasListApplication.java

document.querySelector('#login').addEventListener('click', async (e) => {
    e.preventDefault();

    // validation
    const email = document.querySelector('#input-email').value;
    const pw = document.querySelector('#input-password').value;

    const data = await AJAX('/login', {
        email: email,
        password: pw,
    });

    if (data === LOGIN_SUCCESS_VALUE) {
        showFormMessage("Login successful!", true, document.querySelector('.form'));
        setTimeout(() => {
            window.location.href = "/";
        }, 500)
    } else {
        showFormMessage(data, false, document.querySelector('.form'));
    }
});