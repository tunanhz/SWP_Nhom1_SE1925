const profileForm = document.getElementById('updateProfileForm');
const profileImg = document.getElementById('profileImg');
const fullNameDisplay = document.getElementById('fullNameDisplay');
const imgInput = document.getElementById('profileImgInput');
const saveChangesBtn = document.getElementById('saveChangesBtn');
const saveIcon = document.getElementById('saveIcon');
const imgError = document.getElementById('img-error');
const formError = document.getElementById('form-error');
const formSuccess = document.getElementById('form-success');
let userImgElement = document.getElementById('userImg');
let profileImgElement = document.getElementById('profileImg');

if (account) {
    let imagePath = account?.img;
    if (userImgElement) {
        userImgElement.src = imagePath;
        userImgElement.alt = `Profile of ${account.username || 'User'}`;
    }
    if (profileImgElement) {
        profileImgElement.src = imagePath;
        profileImgElement.alt = `Profile of ${account.username || 'User'}`;
    }
}

// Hàm hiển thị lỗi
function showError(element, message) {
    element.innerHTML = `<p class="text-danger">${message}</p>`;
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
    const hasImg = imgInput.files && imgInput.files.length > 0;
    const isValid = hasImg;
    saveChangesBtn.disabled = !isValid;
}

// Lấy thông tin lễ tân hiện tại
fetch(`http://localhost:8080/SWP_back_war_exploded/api/patientProfile/?accountPatientId=${account.accountPatientId}`, {
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
        const { img } = data.data;
        profileImg.src = img || './assets/images/user/user-1.jpg';
        userImg.src = img || './assets/images/avatars/12.png';
        updateSubmitButton();
    })
    .catch(error => {
        console.error('Error loading profile:', error);
        showError(formError, `Error loading profile: ${error.message}`);
        saveChangesBtn.disabled = true;
    });

// Xử lý thay đổi ảnh và hiển thị lên profileImg
imgInput.addEventListener('change', function () {
    const file = imgInput.files[0];
    if (file) {
        const maxSize = 5 * 1024 * 1024; // 5MB
        const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
        if (!validTypes.includes(file.type)) {
            showError(imgError, `Only JPEG, PNG, or GIF images are accepted`);
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

profileForm.addEventListener('submit', function (event) {
    event.preventDefault();
    hideError(formError);
    hideSuccess();

    const imgFile = imgInput.files ? imgInput.files[0] : null;

    saveChangesBtn.disabled = true;
    loadingSpinner.style.display = 'inline-block';
    saveIcon.style.display = 'none';

    fetch(`http://localhost:8080/SWP_back_war_exploded/api/patientProfile?accountPatientId=${account.accountPatientId}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) throw new Error(`Cannot get receptionistId: ${response.status}`);
            return response.json();
        })
        .then(data => {
            if (!data.success) throw new Error(data.error || 'Cannot get receptionistId');

            // Upload ảnh nếu có, trả về imgUrl hoặc null
            const uploadPromise = imgFile ? uploadToCloudinary(imgFile) : Promise.resolve(null);
            return uploadPromise.then(uploadedImgUrl => {
                const dataToSend = { uploadedImgUrl };
                console.log('Sending POST data:', dataToSend);
                return fetch(`http://localhost:8080/SWP_back_war_exploded/api/patientProfile/?accountPatientId=${account.accountPatientId}`, {
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
                account.img = data.img;
                userImgElement.src = data.img;
                profileImgElement.src = data.img;
                imgInput.value = '';
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


document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('logoutLink').addEventListener('click', function (event) {
        event.preventDefault();
        localStorage.removeItem('account');
        window.location.href = '/frontend/login.html';
    });

    document.getElementById('logoutModalLink').addEventListener('click', function (event) {
        event.preventDefault();
        localStorage.removeItem('account');
        window.location.href = '/frontend/login.html';
    });
});