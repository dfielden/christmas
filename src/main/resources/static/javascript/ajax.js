const timeout = function (millis) {
    return new Promise(function (_, reject) {
        setTimeout(function () {
            reject(new Error(`Request took too long. Timeout after ${millis} milliseconds`));
        }, millis);
    });
};

export const showFormMessage = (message, success, formEl) => {
    const formMessage = formEl.querySelector('.form-msg');
    formMessage.textContent = message;
    formMessage.classList.remove('visibility-hidden');

    if (success) {
        formMessage.classList.remove('form-msg--error');
        formMessage.classList.add('form-msg--success');
    } else {
        formMessage.classList.remove('form-msg--success');
        formMessage.classList.add('form-msg--error');
    }
}

export const AJAX = async function(url, uploadData = undefined) {
    try {
        // POST if uploadData added to argument
        const fetchPromise = uploadData ?
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(uploadData)
            })

            // GET by default
            : fetch(url);

        const res = await Promise.race([fetchPromise, timeout(2000)]); // two promises will race - timeout returns reject after time argument - i.e. we can add a timeout for load failure
        const data = await res.json();

        if(!res.ok) throw new Error(`${data.message} (${res.status})`);
        return data;
    } catch(err) {
        // throw err to deal with at point where function is called
        throw err;
    }
};