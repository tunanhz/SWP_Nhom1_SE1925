let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/';

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('verifyOTPForm');
    let successMessage = document.getElementById('success-message');
    let errorMessage = document.getElementById('error-message');
    const emailInput = document.getElementById('email');

    // Kiểm tra các phần tử DOM
    if (!form || !successMessage || !errorMessage || !emailInput) {
        console.error('One or more elements not found:', { form, successMessage, errorMessage, emailInput });
        if (!successMessage) successMessage = { style: { display: 'none' }, textContent: '' };
        if (!errorMessage) errorMessage = { style: { display: 'none' }, textContent: '' };
    }

    const email = sessionStorage.getItem('resetEmail');
    if (email) {
        emailInput.value = email;
    } else {
        errorMessage.textContent = 'No email found. Please start the reset process again.';
        errorMessage.style.display = 'block';
        form.style.display = 'none';
        return;
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        console.log('Form submitted');
        successMessage.style.display = 'none';
        errorMessage.style.display = 'none';

        const otp = document.getElementById('otp').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        try {
            const response = await fetch(baseAPI + 'verifyOTPServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, otp, password, confirmPassword }),
            });
            console.log('Response received:', response);
            const data = await response.json();
            console.log('Response data:', data);

            if (response.ok && data.success) {
                successMessage.textContent = data.message || 'Your password has been updated.';
                successMessage.style.display = 'block';
                form.reset();
                sessionStorage.removeItem('resetEmail');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else {
                errorMessage.textContent = data.error || 'Failed to verify OTP.';
                errorMessage.style.display = 'block';
            }
        } catch (error) {
            console.error('Fetch error:', error);
            errorMessage.textContent = 'An error occurred. Please try again later.';
            errorMessage.style.display = 'block';
        }
    });
});