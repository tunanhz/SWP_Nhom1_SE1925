// Configuration
const API_BASE_URL1 = "http://localhost:8080/SWP_back_war_exploded/api/patientProfile"; // Adjust for production
const CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/dnoyqme5b/image/upload";
const CLOUDINARY_PRESET = "upload.avt"; // Verify this preset in Cloudinary

// Profile Form Elements
const profileForm = document.getElementById("updateProfileForm");
const profileImg = document.getElementById("profileImg");
const imgInput = document.getElementById("profileImgInput");
const saveChangesBtn = document.getElementById("saveChangesBtn");
const saveIcon = document.getElementById("saveIcon");
const loadingSpinner = document.getElementById("loadingSpinner");
const imgError = document.getElementById("img-error");
const formError = document.getElementById("form-error");
const formSuccess = document.getElementById("form-success");
let userImgElement = document.getElementById("userImg");
let profileImgElement = document.getElementById("profileImg");

// Password Form Elements
const passwordForm = document.getElementById("changePasswordForm");
const changePasswordBtn = document.getElementById("changePasswordBtn");
const currentPasswordInput = document.getElementById("currentPassword");
const newPasswordInput = document.getElementById("newPassword");
const confirmPasswordInput = document.getElementById("confirmPassword");
const passwordError = document.getElementById("password-error");
const passwordSuccess = document.getElementById("password-success");

// Helper Functions
function showError(element, message) {
    element.innerHTML = `<p class="text-danger">${message}</p>`;
    element.style.display = "block";
}

function hideError(element) {
    element.textContent = "";
    element.style.display = "none";
}

function showSuccess(element, message) {
    element.innerHTML = `<p class="text-success">${message}</p>`;
    element.style.display = "block";
}

function hideSuccess(element) {
    element.textContent = "";
    element.style.display = "none";
}

// Profile Form: Enable/disable Submit button
function updateProfileSubmitButton() {
    const hasImg = imgInput.files && imgInput.files.length > 0;
    saveChangesBtn.disabled = !hasImg;
}

// Password Form: Real-time validation
function validatePasswordForm() {
    const currentPassword = currentPasswordInput.value.trim();
    const newPassword = newPasswordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();
    let isValid = true;

    hideError(passwordError);

    if (!currentPassword) {
        showError(passwordError, "Cần phải nhập mật khẩu hiện tại.");
        isValid = false;
    }

    if (newPassword.length < 8) {
        showError(passwordError, "Mật khẩu mới phải dài ít nhất 8 ký tự.");
        isValid = false;
    }

    if (newPassword !== confirmPassword) {
        showError(passwordError, "Mật khẩu không khớp.");
        isValid = false;
    }

    changePasswordBtn.disabled = !isValid;
}

// Attach real-time validation to password inputs
[currentPasswordInput, newPasswordInput, confirmPasswordInput].forEach((input) => {
    input.addEventListener("input", validatePasswordForm);
});

// Load initial profile data
if (account && account.accountPatientId) {
    fetch(`${API_BASE_URL1}?accountPatientId=${account.accountPatientId}`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
    })
        .then((response) => {
            if (!response.ok) throw new Error(`Không thể tải hồ sơ: ${response.status}`);
            return response.json();
        })
        .then((data) => {
            console.log("GET response:", data);
            if (!data.success) {
                showError(formError, data.message || "Không thể tải hồ sơ");
                saveChangesBtn.disabled = true;
                return;
            }
            const img = data.data.img || "./assets/images/user/user-1.jpg";
            if (userImgElement) {
                userImgElement.src = img;
                userImgElement.alt = `Hồ sơ của ${account.username || "User"}`;
            }
            if (profileImgElement) {
                profileImgElement.src = img;
                profileImgElement.alt = `Hồ sơ của ${account.username || "User"}`;
            }
            updateProfileSubmitButton();
        })
        .catch((error) => {
            console.error("Error loading profile:", error);
            showError(formError, `Lỗi khi tải hồ sơ: ${error.message}`);
            saveChangesBtn.disabled = true;
        });
}

// Profile image change handler
imgInput.addEventListener("change", function () {
    const file = imgInput.files[0];
    if (file) {
        const maxSize = 5 * 1024 * 1024; // 5MB
        const validTypes = ["image/jpeg", "image/png", "image/gif"];
        if (!validTypes.includes(file.type)) {
            showError(imgError, "Chỉ chấp nhận hình ảnh JPEG, PNG hoặc GIF");
            imgInput.value = "";
            updateProfileSubmitButton();
            return;
        }
        if (file.size > maxSize) {
            showError(imgError, "Kích thước hình ảnh không được vượt quá 5MB");
            imgInput.value = "";
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

// Upload image to Cloudinary
async function uploadToCloudinary(file) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", CLOUDINARY_PRESET);
    try {
        const response = await fetch(CLOUDINARY_URL, {
            method: "POST",
            body: formData,
        });
        if (!response.ok) throw new Error(`Không thể tải lên Cloudinary: ${response.status}`);
        const data = await response.json();
        if (!data.secure_url) throw new Error("Không tìm thấy URL bảo mật trong phản hồi");
        return data.secure_url;
    } catch (error) {
        throw new Error(`Tải lên Cloudinary không thành công: ${error.message}`);
    }
}

// Profile form submission
profileForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    hideError(formError);
    hideSuccess(formSuccess);

    if (!account || !account.accountPatientId) {
        showError(formError, "Không tìm thấy phiên người dùng. Vui lòng đăng nhập.");
        return;
    }

    const imgFile = imgInput.files ? imgInput.files[0] : null;
    saveChangesBtn.disabled = true;
    loadingSpinner.style.display = "inline-block";
    saveIcon.style.display = "none";

    try {
        const uploadedImgUrl = imgFile ? await uploadToCloudinary(imgFile) : null;
        const dataToSend = {
            accountPatientId: account.accountPatientId,
            uploadedImgUrl: uploadedImgUrl || "",
        };

        const response = await fetch(API_BASE_URL1, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams(dataToSend).toString(),
        });

        const data = await response.json();
        loadingSpinner.style.display = "none";
        saveIcon.style.display = "inline-block";
        saveChangesBtn.disabled = false;

        if (data.success) {
            showSuccess(formSuccess, data.message || "Hồ sơ đã được cập nhật thành công!");
            account.img = data.img;
            if (userImgElement) userImgElement.src = data.img;
            if (profileImgElement) profileImgElement.src = data.img;
            imgInput.value = "";
            updateProfileSubmitButton();
        } else {
            showError(formError, data.message || "Không thể cập nhật hồ sơ");
        }
    } catch (error) {
        console.error("Error updating profile:", error);
        loadingSpinner.style.display = "none";
        saveIcon.style.display = "inline-block";
        saveChangesBtn.disabled = false;
        showError(formError, `Error: ${error.message}`);
    }
});

// Password form submission
passwordForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    hideError(passwordError);
    hideSuccess(passwordSuccess);

    if (!account || !account.accountPatientId) {
        showError(passwordError, "Không tìm thấy phiên người dùng. Vui lòng đăng nhập.");
        return;
    }

    const currentPassword = currentPasswordInput.value.trim();
    const newPassword = newPasswordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();


    if (!currentPassword || !newPassword || !confirmPassword) {
        showError(passwordError, "Tất cả các trường đều bắt buộc.");
        return;
    }
    if (newPassword.length < 8) {
        showError(passwordError, "Mật khẩu mới phải dài ít nhất 8 ký tự.");
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showError(passwordError, "Mật khẩu không khớp.");
        return;
    }

    changePasswordBtn.disabled = true;
    changePasswordBtn.querySelector(".icon").style.display = "none";
    changePasswordBtn.querySelector(".text").textContent = "Changing...";

    try {
        const dataToSend = {
            accountPatientId: account.accountPatientId,
            currentPassword,
            newPassword,
            confirmPassword,
        };

        const response = await fetch(`${API_BASE_URL1}/changePassword`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams(dataToSend).toString(),
        });

        const data = await response.json();
        changePasswordBtn.disabled = false;
        changePasswordBtn.querySelector(".icon").style.display = "inline-block";
        changePasswordBtn.querySelector(".text").textContent = "Change Password";

        if (data.success) {
            showSuccess(passwordSuccess, data.message || "Đã thay đổi mật khẩu thành công!");
            passwordForm.reset();
            changePasswordBtn.disabled = true;
        } else {
            showError(passwordError, data.message || "Không thể thay đổi mật khẩu");
        }
    } catch (error) {
        console.error("Error changing password:", error);
        changePasswordBtn.disabled = false;
        changePasswordBtn.querySelector(".icon").style.display = "inline-block";
        changePasswordBtn.querySelector(".text").textContent = "Change Password";
        showError(passwordError, `Error: ${error.message}`);
    }
});


document.addEventListener("DOMContentLoaded", () => {
    const logoutLink = document.getElementById("logoutLink");
    const logoutModalLink = document.getElementById("logoutModalLink");
    const logoutHandler = async (event) => {
        event.preventDefault();
        try {
            await fetch("/api/logout", { method: "POST" }); 
            localStorage.removeItem("account");
            window.location.href = "/frontend/login.html";
        } catch (error) {
            console.error("Logout failed:", error);
            localStorage.removeItem("account");
            window.location.href = "/frontend/login.html";
        }
    };
    if (logoutLink) logoutLink.addEventListener("click", logoutHandler);
    if (logoutModalLink) logoutModalLink.addEventListener("click", logoutHandler);
});