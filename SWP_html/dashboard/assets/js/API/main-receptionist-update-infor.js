
document.addEventListener('DOMContentLoaded', function () {
    // Profile Form Elements
    const profileForm = document.getElementById('updateProfileForm');
    const fullNameInput = document.getElementById('fullName');
    const phoneInput = document.getElementById('phone');
    const imgInput = document.getElementById('profileImgInput');
    const saveChangesBtn = document.getElementById('saveChangesBtn');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const saveIcon = document.getElementById('saveIcon');
    const fullNameError = document.getElementById('fullName-error');
    const phoneError = document.getElementById('phone-error');
    const imgError = document.getElementById('img-error');
    const formError = document.getElementById('form-error');
    const formSuccess = document.getElementById('form-success');
    const profileImg = document.getElementById('profileImg');
    const userImg = document.getElementById('userImg');
    const fullNameDisplay = document.getElementById('fullNameDisplay');

    // Password Form Elements
    const passwordForm = document.getElementById('changePasswordForm');
    const currentPasswordInput = document.getElementById('currentPassword');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    const passwordLoadingSpinner = document.getElementById('passwordLoadingSpinner');
    const passwordSaveIcon = document.getElementById('passwordSaveIcon');
    const currentPasswordError = document.getElementById('currentPassword-error');
    const newPasswordError = document.getElementById('newPassword-error');
    const confirmPasswordError = document.getElementById('confirmPassword-error');
    const passwordFormError = document.getElementById('password-form-error');
    const passwordFormSuccess = document.getElementById('password-form-success');

    // Kiểm tra các phần tử DOM cần thiết
    if (!saveChangesBtn || !loadingSpinner || !saveIcon || !changePasswordBtn || !passwordLoadingSpinner || !passwordSaveIcon) {
        console.error('DOM elements not found');
        return;
    }

    // Lấy thông tin tài khoản từ localStorage
    const account = JSON.parse(localStorage.getItem('account'));
    if (!account || !account.accountStaffId) {
        showError(formError, 'Please log in to update your profile');
        showError(passwordFormError, 'Please log in to change your password');
        saveChangesBtn.disabled = true;
        changePasswordBtn.disabled = true;
        return;
    }

    // Hàm hiển thị lỗi
    function showError(element, message) {
        element.textContent = message;
        element.style.display = 'block';
    }

    // Hàm ẩn lỗi
    function hideError(element) {
        element.textContent = '';
        element.style.display = 'none';
    }

    // Hàm hiển thị thông báo thành công
    function showSuccess(element, message) {
        element.textContent = message;
        element.style.display = 'block';
    }

    // Hàm ẩn thông báo thành công
    function hideSuccess(element) {
        element.textContent = '';
        element.style.display = 'none';
    }

    // Hàm kiểm tra và bật/tắt nút Submit cho profile form
    function updateProfileSubmitButton() {
        const fullName = fullNameInput.value.trim();
        const phone = phoneInput.value.trim();
        const hasImg = imgInput.files && imgInput.files.length > 0;
        const isValid = (fullName && fullName.match(/^[a-zA-Z\s\u00C0-\u1EF9]{2,100}$/)) ||
                        (phone && phone.match(/^0[0-9]{9}$/)) ||
                        hasImg;
        saveChangesBtn.disabled = !isValid;
        console.log('updateProfileSubmitButton - isValid:', isValid, 'fullName:', fullName, 'phone:', phone, 'hasImg:', hasImg);
    }

    // Hàm kiểm tra và bật/tắt nút Submit cho password form
    function updatePasswordSubmitButton() {
        const currentPassword = currentPasswordInput.value.trim();
        const newPassword = newPasswordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();
        const isValid = currentPassword && newPassword && confirmPassword &&
                        newPassword.length >= 8 && newPassword === confirmPassword;
        changePasswordBtn.disabled = !isValid;
        console.log('updatePasswordSubmitButton - isValid:', isValid, 'currentPassword:', !!currentPassword, 'newPassword:', newPassword, 'confirmPassword:', confirmPassword);
    }

    // Lấy thông tin lễ tân hiện tại
    fetch(`http://localhost:8080/SWP_back_war_exploded/api/receptionistProfile?accountStaffId=${account.accountStaffId}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) throw new Error(`Cannot load profile: ${response.status}`);
            return response.json();
        })
        .then(data => {
            console.log('GET response:', data);
            if (!data.success) {
                showError(formError, data.error || 'Cannot load profile');
                showError(passwordFormError, data.error || 'Cannot load profile');
                saveChangesBtn.disabled = true;
                changePasswordBtn.disabled = true;
                return;
            }
            const { fullName, phone, img } = data.data;
            fullNameInput.value = fullName || '';
            phoneInput.value = phone || '';
            profileImg.src = img || './assets/images/user/user-1.jpg';
            userImg.src = img || './assets/images/avatars/12.png';
            fullNameDisplay.textContent = fullName || 'Not specified';
            updateProfileSubmitButton();
        })
        .catch(error => {
            console.error('Error loading profile:', error);
            showError(formError, `Error loading profile: ${error.message}`);
            showError(passwordFormError, `Error loading profile: ${error.message}`);
            saveChangesBtn.disabled = true;
            changePasswordBtn.disabled = true;
        });

    // Sự kiện nhập liệu cho profile form
    fullNameInput.addEventListener('input', updateProfileSubmitButton);
    phoneInput.addEventListener('input', updateProfileSubmitButton);
    imgInput.addEventListener('change', function () {
        const file = imgInput.files[0];
        if (file) {
            const maxSize = 5 * 1024 * 1024; // 5MB
            const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!validTypes.includes(file.type)) {
                showError(imgError, 'Only JPEG, PNG, or GIF images are accepted');
                imgInput.value = '';
                updateProfileSubmitButton();
                return;
            }
            if (file.size > maxSize) {
                showError(imgError, 'Image size must not exceed 5MB');
                imgInput.value = '';
                updateProfileSubmitButton();
                return;
            }
            hideError(imgError);
            const reader = new FileReader();
            reader.onload = function (e) {
                profileImg.src = e.target.result;
            };
            reader.readAsDataURL(file);
            updateProfileSubmitButton();
        }
    });

    // Sự kiện nhập liệu cho password form
    currentPasswordInput.addEventListener('input', updatePasswordSubmitButton);
    newPasswordInput.addEventListener('input', updatePasswordSubmitButton);
    confirmPasswordInput.addEventListener('input', updatePasswordSubmitButton);

    // Hàm upload ảnh lên Cloudinary
    function uploadToCloudinary(file) {
        const url = 'https://api.cloudinary.com/v1_1/dnoyqme5b/image/upload';
        const data = new FormData();
        data.append('file', file);
        data.append('upload_preset', 'upload.avt');
        return fetch(url, { method: 'POST', body: data })
            .then(response => {
                if (!response.ok) throw new Error(`Cannot upload to Cloudinary: ${response.status}`);
                return response.json();
            })
            .then(data => {
                if (!data.secure_url) throw new Error('Secure URL not found in response');
                return data.secure_url;
            });
    }

    // Xử lý submit profile form
    profileForm.addEventListener('submit', function (event) {
        event.preventDefault();
        hideError(formError);
        hideSuccess(formSuccess);

        const fullName = fullNameInput.value.trim();
        const phone = phoneInput.value.trim();
        const imgFile = imgInput.files ? imgInput.files[0] : null;

        let hasError = false;
        if (fullName && !fullName.match(/^[a-zA-Z\s\u00C0-\u1EF9]{2,100}$/)) {
            showError(fullNameError, 'Full name must contain only letters and spaces, 2-100 characters');
            hasError = true;
        } else {
            hideError(fullNameError);
        }
        if (phone && !phone.match(/^0[0-9]{9}$/)) {
            showError(phoneError, 'Phone number must be 10 digits starting with 0');
            hasError = true;
        } else {
            hideError(phoneError);
        }
        if (hasError) return;

        saveChangesBtn.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        saveIcon.style.display = 'none';

        fetch(`http://localhost:8080/SWP_back_war_exploded/api/receptionistProfile?accountStaffId=${account.accountStaffId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(response => {
                if (!response.ok) throw new Error(`Cannot get receptionistId: ${response.status}`);
                return response.json();
            })
            .then(data => {
                if (!data.success) throw new Error(data.error || 'Cannot get receptionistId');
                const receptionistId = data.data.receptionistId;

                const uploadPromise = imgFile ? uploadToCloudinary(imgFile) : Promise.resolve(null);
                return uploadPromise.then(uploadedImgUrl => {
                    const dataToSend = { receptionistId };
                    if (fullName) dataToSend.fullName = fullName;
                    if (phone) dataToSend.phone = phone;
                    if (uploadedImgUrl) dataToSend.img = uploadedImgUrl;

                    console.log('Sending POST data:', dataToSend);

                    return fetch(`http://localhost:8080/SWP_back_war_exploded/api/receptionistProfile`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: new URLSearchParams(dataToSend).toString()
                    });
                });
            })
            .then(response => {
                if (!response.ok) throw new Error(`Error during update: ${response.status}`);
                return response.json();
            })
            .then(data => {
                loadingSpinner.style.display = 'none';
                saveIcon.style.display = 'inline-block';
                saveChangesBtn.disabled = false;
                if (data.success) {
                    showSuccess(formSuccess, 'Profile updated successfully!');
                    if (fullName) fullNameDisplay.textContent = fullName;
                    const updatedAccount = { ...account };
                    if (fullName) updatedAccount.fullName = fullName;
                    localStorage.setItem('account', JSON.stringify(updatedAccount));
                    if (data.data && data.data.img) {
                        userImg.src = data.data.img;
                    }
                } else {
                    showError(formError, data.error || 'Cannot update profile');
                }
            })
            .catch(error => {
                console.error('Error sending request:', error);
                loadingSpinner.style.display = 'none';
                saveIcon.style.display = 'inline-block';
                saveChangesBtn.disabled = false;
                showError(formError, `Error during update: ${error.message}`);
            });
    });

    // Xử lý submit password form
passwordForm.addEventListener('submit', function (event) {
    event.preventDefault();
    hideError(passwordFormError);
    hideSuccess(passwordFormSuccess);

    const currentPassword = currentPasswordInput.value.trim();
    const newPassword = newPasswordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();

    let hasError = false;
    if (!currentPassword) {
        showError(currentPasswordError, 'Current password is required');
        hasError = true;
    } else {
        hideError(currentPasswordError);
    }
    if (!newPassword || newPassword.length < 8) {
        showError(newPasswordError, 'New password must be at least 8 characters');
        hasError = true;
    } else {
        hideError(newPasswordError);
    }
    if (!confirmPassword || confirmPassword !== newPassword) {
        showError(confirmPasswordError, 'Confirm password must match new password');
        hasError = true;
    } else {
        hideError(confirmPasswordError);
    }
    if (hasError) return;

    changePasswordBtn.disabled = true;
    passwordLoadingSpinner.style.display = 'inline-block';
    passwordSaveIcon.style.display = 'none';

    const dataToSend = {
        accountStaffId: account.accountStaffId,
        currentPassword,
        newPassword,
        confirmPassword
    };

    console.log('Sending password change data:', dataToSend);

    fetch(`http://localhost:8080/SWP_back_war_exploded/api/receptionist/changePassword`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(dataToSend).toString()
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw { status: response.status, ...errorData };
                });
            }
            return response.json();
        })
        .then(data => {
            passwordLoadingSpinner.style.display = 'none';
            passwordSaveIcon.style.display = 'inline-block';
            changePasswordBtn.disabled = false;
            if (data.success) {
                showSuccess(passwordFormSuccess, 'Password changed successfully!');
                passwordForm.reset();
                updatePasswordSubmitButton();
            } else {
                // Hiển thị lỗi cụ thể dựa trên message từ server
                const errorMessage = data.message || 'Cannot change password';
                if (errorMessage.includes('Current password is incorrect')) {
                    showError(currentPasswordError, 'Error updating password: Current password is incorrect');
                } else if (errorMessage.includes('New password and confirm password must match')) {
                    showError(confirmPasswordError, 'Error updating password: New password and confirm password must match');
                } else if (errorMessage.includes('Password must be at least 8 characters')) {
                    showError(newPasswordError, 'Error updating password: Password must be at least 8 characters');
                } else {
                    showError(passwordFormError, `Error updating password: ${errorMessage}`);
                }
            }
        })
        .catch(error => {
            console.error('Error during password change:', error);
            passwordLoadingSpinner.style.display = 'none';
            passwordSaveIcon.style.display = 'inline-block';
            changePasswordBtn.disabled = false;
            if (error.status === 400) {
                showError(passwordFormError, 'Error updating password: Invalid input data');
            } else {
                showError(passwordFormError, `Error updating password: ${error.message}`);
            }
        });
});
});
