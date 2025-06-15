document.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const doctorId = params.get('id');
  if (doctorId) {
    try {
      const response = await fetch(`http://localhost:8080/SWP_back_war_exploded/api/doctors/${doctorId}`);
      const doctorData = await response.json();
      
      document.querySelector('.doctor-name').innerHTML = doctorData.fullName
      document.getElementById('doctor-name1').textContent = doctorData.fullName || 'N/A';
      document.getElementById('doctor-img').src = doctorData.img || 'N/A';



      document.getElementById('doctor-department').textContent = doctorData.department || 'N/A';
      document.getElementById('doctor-email').textContent = doctorData.email || 'N/A';
      document.getElementById('doctor-eduLevel').textContent = doctorData.eduLevel || 'N/A';
      document.getElementById('doctor-email').innerHTML = `<a href="mailto:${doctorData.email}"> ${doctorData.email}</a>` || 'N/A';
      document.getElementById('doctor-phone').innerHTML = `<a href="tel:${doctorData.phone}">${doctorData.phone}</a>` || 'N/A';
      document.getElementById('doctor-location').textContent = doctorData.address || 'N/A';
      document.getElementById('doctor-age').textContent = doctorData.age || 'N/A';

    } catch (error) {
      console.error('Lỗi khi lấy dữ liệu bác sĩ:', error);
      document.getElementById('doctor-name').textContent = 'Không tìm thấy thông tin';
    }
  }
});