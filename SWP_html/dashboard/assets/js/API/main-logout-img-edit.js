let imagePath = account?.img;

if (account) {
    const userImgElement = document.getElementById('userImg');
    if (userImgElement) {
        userImgElement.src = imagePath;
        userImgElement.alt = `Profile of ${account.username || 'User'}`;
    }

    const profileImgElement = document.getElementById('profileImg');
    if (profileImgElement) {
        profileImgElement.src = imagePath;
        profileImgElement.alt = `Profile of ${account.username || 'User'}`;
    }
}

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