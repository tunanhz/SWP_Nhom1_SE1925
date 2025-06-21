let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api';

// Biến trạng thái để theo dõi tính hợp lệ
let isUsernameValid = false;
let isEmailValid = false;

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signupForm');
    const usernameInput = form.querySelector('input[name="username"]');
    const emailInput = form.querySelector('input[name="email"]');
    const passwordInput = form.querySelector('input[name="password"]');
    const imgInput = form.querySelector('input[name="img"]');
    const submitButton = document.getElementById('submitButton');
    const usernameError = document.getElementById('username-error');
    const usernameValid = document.getElementById('username-valid');
    const emailError = document.getElementById('email-error');
    const emailValid = document.getElementById('email-valid');

    // Hàm hiển thị thông báo lỗi
    function showError(element, message) {
        element.textContent = message;
        element.style.display = 'block';
    }

    // Hàm ẩn thông báo lỗi
    function hideError(element) {
        element.textContent = '';
        element.style.display = 'none';
    }

    // Hàm hiển thị trạng thái hợp lệ
    function showValid(element, message) {
        element.textContent = message;
        element.style.display = 'block';
    }

    // Hàm ẩn trạng thái hợp lệ
    function hideValid(element) {
        element.textContent = '';
        element.style.display = 'none';
    }

    // Hàm kiểm tra form có đầy đủ dữ liệu không
    function isFormComplete() {
        return (
            usernameInput.value.trim() &&
            emailInput.value.trim() &&
            passwordInput.value.trim() &&
            imgInput.files.length > 0
        );
    }

    // Hàm cập nhật trạng thái nút submit
    function updateSubmitButton() {
        const isValid = isUsernameValid && isEmailValid && isFormComplete();
        submitButton.disabled = !isValid;
        console.log('Submit button state:', { isUsernameValid, isEmailValid, isFormComplete: isFormComplete(), disabled: !isValid });
    }

    // Hàm kiểm tra trùng lặp
    function checkDuplicate(type, value, errorElement, validElement, validFlag) {
        if (!value.trim()) {
            hideError(errorElement);
            hideValid(validElement);
            if (type === 'Username') isUsernameValid = false;
            else if (type === 'Email') isEmailValid = false;
            updateSubmitButton();
            return;
        }

        showError(errorElement, 'Checking...');
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
                    showError(errorElement, `${type} already exists`);
                    hideValid(validElement);
                    if (type === 'Username') isUsernameValid = false;
                    else if (type === 'Email') isEmailValid = false;
                } else {
                    hideError(errorElement);
                    showValid(validElement, `${type} is available`);
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

    // Hàm debounce để giới hạn tần suất gọi API
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

    // Kiểm tra username khi nhập
    usernameInput.addEventListener('input', debounce(function () {
        const value = usernameInput.value.trim();
        checkDuplicate('Username', value, usernameError, usernameValid, 'isUsernameValid');
    }, 500));

    // Kiểm tra email khi nhập
    emailInput.addEventListener('input', debounce(function () {
        const value = emailInput.value.trim();
        checkDuplicate('Email', value, emailError, emailValid, 'isEmailValid');
    }, 500));

    // Kiểm tra lại khi rời khỏi input (blur)
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

    // Kiểm tra thủ công khi form hoàn tất
    form.addEventListener('input', function () {
        updateSubmitButton();
    });

    // Hàm upload ảnh lên Cloudinary
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

    // Xử lý submit form
    form.addEventListener('submit', function (event) {
        event.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const email = emailInput.value.trim();
        const imgFile = imgInput.files[0];

        console.log('Form data before upload:', {
            username: username || 'null',
            email: email || 'null',
            imgFile: imgFile ? imgFile.name : 'null'
        });

        if (!username || !password || !email || !imgFile) {
            showError(usernameError, 'Please fill in all required fields');
            return;
        }

        if (!isUsernameValid || !isEmailValid) {
            showError(usernameError, 'Please resolve username or email errors');
            return;
        }

        submitButton.disabled = true;
        submitButton.innerHTML = 'Registering...';

        uploadToCloudinary(imgFile)
            .then(imageUrl => {
                console.log('Image URL obtained:', imageUrl);
                submitFormWithImageUrl(imageUrl);
            })
            .catch(error => {
                console.error('Upload error:', error);
                showError(usernameError, 'Failed to upload image: ' + error.message);
                submitButton.disabled = false;
                submitButton.innerHTML = '<span class="iq-btn-text-holder">register</span><span class="iq-btn-icon-holder">...</span>';
            });
    });

    // Gửi form với URL ảnh
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
                    alert('Registration successful! Redirecting to login...');
                    window.location.href = '/frontend/login.html';
                } else {
                    showError(usernameError, data.error || 'Registration failed!');
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