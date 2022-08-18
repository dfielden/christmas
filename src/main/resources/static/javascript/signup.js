'use strict'
import {AJAX, showFormMessage} from "./ajax.js";
const SIGNUP_SUCCESS_VALUE = 'SIGNUP_SUCCESS'; // must match PSFS SIGNUP_SUCCESS_RESPONSE_VALUE in ChristmasListApplication.java
const LOGIN_SUCCESS_VALUE = 'LOGIN_SUCCESS'; // must match PSFS LOGIN_SUCCESS_RESPONSE_VALUE in ChristmasListApplication.java

document.querySelector('#signup').addEventListener('click', async (e) => {
    e.preventDefault();
    await submitForm();
});

document.querySelector('.form').addEventListener('submit', async () => {
    await submitForm();
})

const submitForm = async () => {
    // validation
    const username = document.querySelector('#input-username').value;
    const pw1 = document.querySelector('#input-password-1').value;
    const pw2 = document.querySelector('#input-password-2').value;

    if (pw1 !== pw2) {
        showFormMessage("Please ensure passwords match", false, document.querySelector('.form'));
        return;
    }

    if (!(username && pw1 && pw2)) {
        showFormMessage("Please ensure all fields are completed", false, document.querySelector('.form'));
        return;
    }

    const data = await AJAX('/signup', {
        password: pw1,
        username: username,
    });

    if (data === SIGNUP_SUCCESS_VALUE) {
        showFormMessage("Sign-up successful!", true, document.querySelector('.form'));
        setTimeout(() => {
            // if signup successful, login and redirect to home
            _login(username, pw1);
        }, 500)
    } else {
        showFormMessage(data, false, document.querySelector('.form'));
    }
}

const _login = async (username, pw) => {
    const data = await AJAX('/login', {
        username: username,
        password: pw,
        email: "",
    });

    if (data === LOGIN_SUCCESS_VALUE) {
        window.location.href = "/";
    } else {
        showFormMessage(data, false, document.querySelector('.form'));
    }
}