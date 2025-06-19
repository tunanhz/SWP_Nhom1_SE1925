let baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/signupPatient';

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signupForm');
    const imgInput = form.querySelector('input[name="img"]');

    // Function to upload image to Cloudinary
    function uploadToCloudinary(file) {
        const url = 'https://api.cloudinary.com/v1_1/dnoyqme5b/image/upload'; // Replace with your cloud name
        const data = new FormData();
        data.append('file', file);
        data.append('upload_preset', 'upload.avt'); // Replace with your upload preset

        console.log('Uploading file to Cloudinary:', file ? file.name : 'No file');
        return fetch(url, {
            method: 'POST',
            body: data
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to upload to Cloudinary: ' + response.status + ' - ' + response.statusText);
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

        const username = form.querySelector('input[name="username"]').value.trim();
        const password = form.querySelector('input[name="password"]').value.trim();
        const email = form.querySelector('input[name="email"]').value.trim();
        const imgFile = imgInput.files[0];

        console.log('Form data before upload:', {
            username: username || 'null',
            email: email || 'null',
            imgFile: imgFile ? imgFile.name : 'null'
        });

        if (!username || !password || !email || !imgFile) {
            alert("Please fill in all required fields and select an image!");
            return;
        }

        uploadToCloudinary(imgFile)
            .then(imageUrl => {
                console.log('Image URL obtained:', imageUrl);
                submitFormWithImageUrl(imageUrl);
            })
            .catch(error => {
                console.error('Upload error:', error);
                alert('Failed to upload image to Cloudinary. Please try again! Error: ' + error.message);
            });
    });

    function submitFormWithImageUrl(imageUrl) {
        const form = document.getElementById('signupForm');
        const username = form.querySelector('input[name="username"]').value.trim();
        const password = form.querySelector('input[name="password"]').value.trim();
        const email = form.querySelector('input[name="email"]').value.trim();

        const data = {
            username: username,
            password: password,
            email: email,
            img: imageUrl
        };

        console.log('Sending to server with URLSearchParams:', data);

        fetch(baseAPI, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams(data).toString()
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.status + ' - ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log('Response from server:', data);
                if (data.success) {
                    alert('Registration successful! Redirecting to login...');
                    window.location.href = '/frontend/login.html';
                } else {
                    alert(data.error || 'Registration failed!');
                }
            })
            .catch(error => {
                console.error('Fetch error:', error);
                alert('An error occurred during registration. Please try again! Status: ' + error.message);
            });
    }
});