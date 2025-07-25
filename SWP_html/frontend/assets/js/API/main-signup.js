let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api';

let isUsernameValid = false;
let isEmailValid = false;
let isPasswordConfirmed = false;

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signupForm');
    const usernameInput = form.querySelector('input[name="username"]');
    const emailInput = form.querySelector('input[name="email"]');
    const passwordInput = form.querySelector('input[name="password"]');
    const confirmPasswordInput = document.getElementById('confirm-password');
    const imgInput = form.querySelector('input[name="img"]');
    const submitButton = document.getElementById('submitButton');
    const usernameError = document.getElementById('username-error');
    const usernameValid = document.getElementById('username-valid');
    const emailError = document.getElementById('email-error');
    const emailValid = document.getElementById('email-valid');
    const confirmPasswordError = document.getElementById('confirm-password-error'); 
    const confirmPasswordValid = document.getElementById('confirm-password-valid');

    function showError(element, message) {
        element.textContent = message;
        element.style.display = 'block';
    }

    function hideError(element) {
        element.textContent = '';
        element.style.display = 'none';
    }

    function showValid(element, message) {
        element.textContent = message;
        element.style.display = 'block';
    }

    function hideValid(element) {
        element.textContent = '';
        element.style.display = 'none';
    }

    function isFormComplete() {
        return (
            usernameInput.value.trim() &&
            emailInput.value.trim() &&
            passwordInput.value.trim() &&
            confirmPasswordInput.value.trim() &&
            imgInput.files.length > 0
        );
    }

    function updateSubmitButton() {
        const isValid = isUsernameValid && isEmailValid && isPasswordConfirmed && isFormComplete();
        submitButton.disabled = !isValid;
        console.log('Submit button state:', { isUsernameValid, isEmailValid, isPasswordConfirmed, isFormComplete: isFormComplete(), disabled: !isValid });
    }

    function checkDuplicate(type, value, errorElement, validElement, validFlag) {
        if (!value.trim()) {
            hideError(errorElement);
            hideValid(validElement);
            if (type === 'Username') isUsernameValid = false;
            else if (type === 'Email') isEmailValid = false;
            updateSubmitButton();
            return;
        }

        showError(errorElement, 'Đang kiểm tra...');
        hideValid(validElement);

        fetch(`${baseAPI}/check${type}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ value })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network error: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log(`${type} check result:`, data);
                if (data.exists) {
                   showError(errorElement, type === 'Username' ? 'Tên người dùng đã tồn tại' : 'Email đã tồn tại');
                    hideValid(validElement);
                    if (type === 'Username') isUsernameValid = false;
                    else if (type === 'Email') isEmailValid = false;
                } else {
                    hideError(errorElement);
                    showValid(validElement, type === 'Username' ? 'Tên người dùng có thể sử dụng' : 'Email có thể sử dụng');
                    if (type === 'Username') isUsernameValid = true;
                    else if (type === 'Email') isEmailValid = true;
                }
                updateSubmitButton();
            })
            .catch(error => {
                showError(errorElement, `Error checking ${type.toLowerCase()}: ${error.message}`);
                hideValid(validElement);
                if (type === 'Username') isUsernameValid = false;
                else if (type === 'Email') isEmailValid = false;
                updateSubmitButton();
            });
    }

    function checkPasswordConfirmation() {
        const password = passwordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();
        if (!password || !confirmPassword) {
            hideError(confirmPasswordError);
            hideValid(confirmPasswordValid);
            isPasswordConfirmed = false;
        } else if (password !== confirmPassword) {
            showError(confirmPasswordError, 'Mật khẩu không khớp');
            hideValid(confirmPasswordValid);
            isPasswordConfirmed = false;
        } else {
            hideError(confirmPasswordError);
            showValid(confirmPasswordValid, 'Mật khẩu khớp nhau');
            isPasswordConfirmed = true;
        }
        updateSubmitButton();
    }

    // limit call api
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    usernameInput.addEventListener('input', debounce(function () {
        const value = usernameInput.value.trim();
        checkDuplicate('Username', value, usernameError, usernameValid, 'isUsernameValid');
    }, 500));

    emailInput.addEventListener('input', debounce(function () {
        const value = emailInput.value.trim();
        checkDuplicate('Email', value, emailError, emailValid, 'isEmailValid');
    }, 500));

    confirmPasswordInput.addEventListener('input', checkPasswordConfirmation);
    passwordInput.addEventListener('input', checkPasswordConfirmation);

    [usernameInput, emailInput].forEach(input => {
        input.addEventListener('blur', function () {
            const value = input.value.trim();
            if (value) {
                const type = input.name === 'username' ? 'Username' : 'Email';
                const errorElement = input.name === 'username' ? usernameError : emailError;
                const validElement = input.name === 'username' ? usernameValid : emailValid;
                checkDuplicate(type, value, errorElement, validElement);
            } else {
                const errorElement = input.name === 'username' ? usernameError : emailError;
                const validElement = input.name === 'username' ? usernameValid : emailValid;
                hideError(errorElement);
                hideValid(validElement);
                if (input.name === 'username') isUsernameValid = false;
                else if (input.name === 'email') isEmailValid = false;
                updateSubmitButton();
            }
        });
    });

    form.addEventListener('input', function () {
        updateSubmitButton();
    });

    function uploadToCloudinary(file) {
        const url = 'https://api.cloudinary.com/v1_1/dnoyqme5b/image/upload';
        const data = new FormData();
        data.append('file', file);
        data.append('upload_preset', 'upload.avt');

        console.log('Uploading file to Cloudinary:', file ? file.name : 'No file');
        return fetch(url, {
            method: 'POST',
            body: data
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to upload to Cloudinary: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Cloudinary response:', data);
                if (data.secure_url) {
                    return data.secure_url;
                } else {
                    throw new Error('No secure_url in Cloudinary response');
                }
            })
            .catch(error => {
                console.error('Error uploading to Cloudinary:', error);
                throw error;
            });
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const email = emailInput.value.trim();
        const imgFile = imgInput.files[0];
        const confirmPassword = confirmPasswordInput.value.trim();

        console.log('Form data before upload:', {
            username: username || 'null',
            email: email || 'null',
            imgFile: imgFile ? imgFile.name : 'null'
        });

        if (!username || !password || !confirmPassword || !email || !imgFile) {
            showError(usernameError, 'Vui lòng điền đầy đủ các trường bắt buộc');
            return;
        }

        if (!isUsernameValid || !isEmailValid || !isPasswordConfirmed) {
            showError(usernameError, 'Vui lòng khắc phục lỗi tên người dùng, email hoặc mật khẩu');
            return;
        }

        submitButton.disabled = true;
        submitButton.innerHTML = 'Đang đăng ký...';

        uploadToCloudinary(imgFile)
            .then(imageUrl => {
                console.log('Image URL obtained:', imageUrl);
                submitFormWithImageUrl(imageUrl);
            })
            .catch(error => {
                console.error('Upload error:', error);
                showError(usernameError, 'Tải ảnh lên thất bại: ' + error.message);
                submitButton.disabled = false;
                submitButton.innerHTML = '<span class="iq-btn-text-holder">register</span><span class="iq-btn-icon-holder">...</span>';
            });
    });

    function submitFormWithImageUrl(imageUrl) {
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const email = emailInput.value.trim();

        const data = {
            username: username,
            password: password,
            email: email,
            img: imageUrl
        };

        console.log('Sending to server with URLSearchParams:', data);

        fetch(`${baseAPI}/signupPatient`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(data).toString()
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Response from server:', data);
                submitButton.disabled = false;
                submitButton.innerHTML = '<span class="iq-btn-text-holder">register</span><span class="iq-btn-icon-holder">...</span>';
                if (data.success) {
                    alert('Đăng ký thành công! Đang chuyển hướng đến trang đăng nhập...');
                    window.location.href = '/frontend/login.html';
                } else {
                    showError(usernameError, data.error || 'Đăng ký thất bại!');
                }
            })
            .catch(error => {
                console.error('Fetch error:', error);
                showError(usernameError, 'Registration error: ' + error.message);
                submitButton.disabled = false;
                submitButton.innerHTML = '<span class="iq-btn-text-holder">register</span><span class="iq-btn-icon-holder">...</span>';
            });
    }
});