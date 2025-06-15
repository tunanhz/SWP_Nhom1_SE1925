let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/loginServlet'; // Điều chỉnh context path

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('loginForm');

    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Ngăn chặn hành vi mặc định của form

        // Lấy giá trị từ form
        const username = form.querySelector('input[name="username"]').value;
        const password = form.querySelector('input[name="pwd"]').value;

        // Kiểm tra nếu trường rỗng
        if (!username || !password) {
            alert("Please fill in both username and password!");
            return;
        }

        // Chuẩn bị body dưới dạng JSON
        const requestBody = {
            username: username,
            password: password // Sử dụng "password" để khớp với servlet (có thể thay bằng "pwd" nếu cần)
        };

        // Gửi yêu cầu POST đến API với body JSON
        fetch(baseAPI, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // Sử dụng JSON
            },
            body: JSON.stringify(requestBody) // Chuyển đổi object thành JSON string
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.status + ' ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log('Response from server:', data); // Log để debug

                // Kiểm tra phản hồi
                if (data.success) {
                    const role = data.role.toLowerCase();
                    let redirectUrl;

                    // Xác định URL chuyển hướng dựa trên role
                    switch (role) {
                        case 'doctor':
                        case 'nurse':
                        case 'receptionist':
                            window.location.href = "./front-end/index.html";
                            break;
                        case 'patient':
                            window.location.href = '/SWP_html/dashboard/patient-dashboard.html';
                            break;
                        case 'pharmacist':
                            window.location.href = "./front-end/index.html";
                            break;
                        default:
                            redirectUrl = '/accessDenied';
                    }

                    //     case 'doctor':
                    //     case 'nurse':
                    //     case 'receptionist':
                    //         redirectUrl = '/staff';
                    //         break;
                    //     case 'patient':
                    //         redirectUrl = '/patient';
                    //         break;
                    //     case 'pharmacist':
                    //         redirectUrl = '/pharmacist';
                    //         break;
                    //     default:
                    //         redirectUrl = '/accessDenied';
                    // }

                    // // Chuyển hướng đến URL tương ứng
                    // window.location.href = baseAPI.substring(0, baseAPI.indexOf('/api')) + redirectUrl;
                } else {
                    alert(data.error || 'Login failed!');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred during login. Please try again! Status: ' + error.message);
            });
    });
});