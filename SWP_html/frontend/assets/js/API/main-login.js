let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/loginServlet'; // Điều chỉnh context path

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('loginForm');

    form.addEventListener('submit', function (event) {
        event.preventDefault(); 

        const identifier = form.querySelector('input[name="identifier"]').value;
        const password = form.querySelector('input[name="pwd"]').value;

        if (!identifier || !password) {
            alert("Vui lòng điền đầy đủ tên người dùng/email và mật khẩu!");
            return;
        }

        const requestBody = {
            identifier: identifier,
            password: password 
        };

        fetch(baseAPI, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' 
            },
            body: JSON.stringify(requestBody) 
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Phản hồi của mạng không ổn ' + response.status + ' ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log('Response from server:', data); 
                localStorage.setItem('account', JSON.stringify(data.account));

                if (data.success) {
                    const role = data.role.toLowerCase();
                    let redirectUrl;

                    switch (role) {
                        case 'doctor':
                            window.location.href = "/dashboard/dashboadtest.html";
                            break;
                        case 'nurse':
                            window.location.href = "/dashboard/patient-dashboard.html";
                            break;
                        case 'receptionist':
                            window.location.href = "/dashboard/receptionist-dashboard.html";
                            break;
                        case 'patient':
                            window.location.href = '/dashboard/patient-dashboard.html';
                            break;
                        case 'pharmacist':
                            window.location.href = "/dashboard/pharmacist-dashboard.html";
                            break;
                         case 'adminbusiness':
                            window.location.href = "/dashboard/index.html";
                            break;
                         case 'adminsys':
                            window.location.href = "/dashboard/adminSystem_dashborad.html";
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
                    alert(data.error || 'Đăng nhập thất bại!');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Đã xảy ra lỗi trong quá trình đăng nhập. Vui lòng thử lại! Trạng thái:' + error.message);
            });
    });
});