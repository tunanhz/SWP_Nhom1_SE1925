let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/';

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('resetPasswordForm');
    let successMessage = document.getElementById('success-message');
    let errorMessage = document.getElementById('error-message');
    const emailInput = document.getElementById('email');
    const submitButton = document.querySelector('#resetPasswordForm button[type="submit"]');

    // Kiểm tra các phần tử DOM
    if (!form || !successMessage || !errorMessage || !emailInput || !submitButton) {
        console.error('One or more elements not found:', { form, successMessage, errorMessage, emailInput, submitButton });
        if (!successMessage) successMessage = { style: { display: 'none' }, textContent: '' };
        if (!errorMessage) errorMessage = { style: { display: 'none' }, textContent: '' };
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        console.log('Form submitted');
        successMessage.style.display = 'none';
        errorMessage.style.display = 'none';

        const email = emailInput.value;
        console.log('Email entered:', email);

        // Validate email format
        const emailPattern = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
        if (!emailPattern.test(email)) {
            errorMessage.textContent = 'Vui lòng nhập địa chỉ Gmail hợp lệ (ví dụ: chuhung@gmail.com)';
            errorMessage.style.display = 'block';
            return;
        }
        console.log('Email validated successfully');

        // Thay đổi nội dung nút khi gửi yêu cầu
        const originalButtonText = submitButton.innerHTML;
        submitButton.innerHTML = 'Đang kiểm tra...';
        submitButton.disabled = true;

        try {
            const response = await fetch(baseAPI + 'resetPasswordServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email }),
            });
            console.log('Response received:', response);
            let data;
            try {
                data = await response.json();
                console.log('Response data:', data);
            } catch (jsonError) {
                console.error('Failed to parse JSON:', jsonError);
                errorMessage.textContent = 'Phản hồi từ server không hợp lệ. Vui lòng thử lại.';
                errorMessage.style.display = 'block';
                throw new Error('JSON parse failed');
            }

            if (response.ok && data.success) {
                successMessage.textContent = data.message || 'Mã OTP đã được gửi đến email của bạn.';
                successMessage.style.display = 'block';
                form.reset();
                sessionStorage.setItem('resetEmail', email);
                setTimeout(() => {
                    window.location.href = 'verify-otp.html';
                }, 1000);
            } else {
                // Xử lý lỗi cụ thể
                console.log('Error condition triggered, data.error:', data.error);
                if (data.error === "Email not found") {
                    errorMessage.textContent = 'Email không tồn tại';
                } else {
                    errorMessage.textContent = data.error || 'Không thể xử lý yêu cầu đặt lại mật khẩu.';
                }
                errorMessage.style.display = 'block';
            }
        } catch (error) {
            console.error('Fetch or JSON error:', error);
            errorMessage.textContent = 'Đã xảy ra lỗi. Vui lòng thử lại sau.';
            errorMessage.style.display = 'block';
        } finally {
            // Khôi phục nút sau khi hoàn tất
            submitButton.innerHTML = originalButtonText;
            submitButton.disabled = false;
        }
    });
});