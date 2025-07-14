document.addEventListener('DOMContentLoaded', function () {
    // Lấy các phần tử DOM cho cập nhật hồ sơ
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

    // Lấy các phần tử DOM cho thay đổi mật khẩu
    const passwordForm = document.getElementById('changePasswordForm');
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    const currentPasswordInput = document.getElementById('currentPassword');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordError = document.getElementById('password-error');

    // Kiểm tra các phần tử DOM cần thiết
    if (!saveChangesBtn || !loadingSpinner || !saveIcon || !userImg || !changePasswordBtn) {
        console.error('DOM elements not found:', { saveChangesBtn, loadingSpinner, saveIcon, userImg, changePasswordBtn });
        return;
    }

    // Lấy thông tin tài khoản từ localStorage
    const account = JSON.parse(localStorage.getItem('account'));
    if (!account || !account.accountStaffId) {
        showError(formError, 'Please log in to update your profile');
        saveChangesBtn.disabled = true;
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
    function showSuccess(message) {
        formSuccess.textContent = message;
        formSuccess.style.display = 'block';
    }

    // Hàm ẩn thông báo thành công
    function hideSuccess() {
        formSuccess.textContent = '';
        formSuccess.style.display = 'none';
    }

    // Hàm kiểm tra và bật/tắt nút Submit cho cập nhật hồ sơ
    function updateSubmitButton() {
        const fullName = fullNameInput.value.trim();
        const phone = phoneInput.value.trim();
        const hasImg = imgInput.files && imgInput.files.length > 0;
        const isValid = (fullName && fullName.match(/^[a-zA-Z\s\u00C0-\u1EF9]{2,100}$/)) ||
                        (phone && phone.match(/^0[0-9]{9}$/)) ||
                        hasImg;
        saveChangesBtn.disabled = !isValid;
        console.log('updateSubmitButton - isValid:', isValid, 'fullName:', fullName, 'phone:', phone, 'hasImg:', hasImg);
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
                saveChangesBtn.disabled = true;
                return;
            }
            const { fullName, phone, img } = data.data;
            fullNameInput.value = fullName || '';
            phoneInput.value = phone || '';
            profileImg.src = img || './assets/images/user/user-1.jpg';
            userImg.src = img || './assets/images/avatars/12.png';
            fullNameDisplay.textContent = fullName || 'Not specified';
            updateSubmitButton();
        })
        .catch(error => {
            console.error('Error loading profile:', error);
            showError(formError, `Error loading profile: ${error.message}`);
            saveChangesBtn.disabled = true;
        });

    // Sự kiện nhập liệu cho fullName và phone
    fullNameInput.addEventListener('input', updateSubmitButton);
    phoneInput.addEventListener('input', updateSubmitButton);

    // Xử lý thay đổi ảnh và hiển thị lên profileImg
    imgInput.addEventListener('change', function () {
        const file = imgInput.files[0];
        if (file) {
            const maxSize = 5 * 1024 * 1024; // 5MB
            const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!validTypes.includes(file.type)) {
                showError(imgError, 'Only JPEG, PNG, or GIF images are accepted');
                imgInput.value = '';
                updateSubmitButton();
                return;
            }
            if (file.size > maxSize) {
                showError(imgError, 'Image size must not exceed 5MB');
                imgInput.value = '';
                updateSubmitButton();
                return;
            }
            hideError(imgError);
            const reader = new FileReader();
            reader.onload = function (e) {
                profileImg.src = e.target.result;
            };
            reader.readAsDataURL(file);
            updateSubmitButton();
        }
    });

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

    // Xử lý submit form (cập nhật hồ sơ)
    profileForm.addEventListener('submit', function (event) {
        event.preventDefault();
        hideError(formError);
        hideSuccess();

        const fullName = fullNameInput.value.trim();
        const phone = phoneInput.value.trim();
        const imgFile = imgInput.files ? imgInput.files[0] : null;

        // Kiểm tra tính hợp lệ của các trường được nhập
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

        // Lấy receptionistId từ GET request
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

                // Upload ảnh nếu có, trả về imgUrl hoặc null
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
                    showSuccess('Profile updated successfully!');
                    if (fullName) fullNameDisplay.textContent = fullName;
                    const updatedAccount = { ...account };
                    if (fullName) updatedAccount.fullName = fullName;
                    localStorage.setItem('account', JSON.stringify(updatedAccount));
                    // Cập nhật userImg với URL ảnh mới từ server
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

    // Xử lý thay đổi mật khẩu
    changePasswordBtn.addEventListener('click', function (event) {
        event.preventDefault(); // Ngăn chặn hành vi mặc định nếu có
        // Kích hoạt tab thay đổi mật khẩu
        const passwordTab = new bootstrap.Tab(document.querySelector('#setting-content-2-tab'));
        passwordTab.show();
        // Đặt focus vào input đầu tiên để người dùng dễ nhập
        currentPasswordInput.focus();
    });

    passwordForm.addEventListener('submit', function (event) {
        event.preventDefault();
        hideError(passwordError);
        hideSuccess();

        const currentPassword = currentPasswordInput.value.trim();
        const newPassword = newPasswordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        let hasError = false;
        if (!currentPassword || !newPassword || !confirmPassword) {
            showError(passwordError, 'All password fields are required');
            hasError = true;
        } else if (newPassword.length < 8) {
            showError(passwordError, 'Password must be at least 8 characters');
            hasError = true;
        } else if (newPassword !== confirmPassword) {
            showError(passwordError, 'New password and confirm password must match');
            hasError = true;
        }
        if (hasError) return;

        saveChangesBtn.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        saveIcon.style.display = 'none';

        const dataToSend = {
            accountStaffId: account.accountStaffId,
            currentPassword: currentPassword,
            newPassword: newPassword
        };

        fetch(`http://localhost:8080/SWP_back_war_exploded/api/receptionist/changePassword`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(dataToSend).toString()
        })
            .then(response => {
                if (!response.ok) throw new Error(`Error during password change: ${response.status}`);
                return response.json();
            })
            .then(data => {
                loadingSpinner.style.display = 'none';
                saveIcon.style.display = 'inline-block';
                saveChangesBtn.disabled = false;
                if (data.success) {
                    showSuccess('Password changed successfully!');
                    passwordForm.reset(); // Xóa form sau khi thành công
                } else {
                    showError(passwordError, data.message || 'Cannot change password');
                }
            })
            .catch(error => {
                console.error('Error sending password change request:', error);
                loadingSpinner.style.display = 'none';
                saveIcon.style.display = 'inline-block';
                saveChangesBtn.disabled = false;
                showError(passwordError, `Error during password change: ${error.message}`);
            });
    });

    // Xử lý logout
    document.getElementById('logoutModalLink').addEventListener('click', function (event) {
        event.preventDefault();
        localStorage.removeItem('account');
        window.location.href = '/frontend/login.html';
    });
});