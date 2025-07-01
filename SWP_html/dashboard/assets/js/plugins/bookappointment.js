"use strict"
const accountString = localStorage.getItem("account");
const account = JSON.parse(accountString);
const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${account.accountPatientId}`;
    
async function callAPI () {
    const response = await fetch(baseAPI, {
      method: "GET",
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
          errorData.error || `HTTP error! Status: ${response.status}`
      );
    }

    const data = await response.json();
    const allAppointment = data.allAppointment;
    return allAppointment;
}

var bookingcalendar
if (document.querySelectorAll('#bookingcalendar').length) {
  document.addEventListener('DOMContentLoaded', async function () {

    const allAppointment = await callAPI();
    
    // Transform API data to match the desired event format
    const formattedEvents = allAppointment.map(appointment => ({
      title: appointment.note || 'Appointment',
      start: moment(`${appointment.appointmentDate} ${appointment.appointmentTime}`, 'DD/MM/YYYY HH:mm:ss').toISOString(),
      backgroundColor: 'rgba(var(--bs-primary-rgb), 0.1)',
      textColor: 'var(--bs-primary)',
      borderColor: 'var(--bs-primary)',
      extendedProps: {
        drname: appointment.doctorName || 'Unknown Doctor',
        starttime: appointment.appointmentTime || '00:00:00',
        endtime: appointment.appointmentTime ? moment(`${appointment.appointmentDate} ${appointment.appointmentTime}`, 'DD/MM/YYYY HH:mm:ss').add(30, 'minutes').format('HH:mm:ss') : '00:30:00',
        desc: appointment.note || 'No description',
        checked: appointment.appointmentStatus === 'Confirmed'
      }
    }));
    
    let calendarEl = document.getElementById('bookingcalendar');
     bookingcalendar = new FullCalendar.Calendar(calendarEl, {
      selectable: true,
      plugins: ["timeGrid", "dayGrid", "list", "interaction"],
      timeZone: "UTC",
      defaultView: "dayGridMonth",
      contentHeight: "auto",
      eventLimit: true,
      dayMaxEvents: 4,
      header: {
          left: "prev,next today",
          center: "title",
          right: "dayGridMonth,timeGridWeek,timeGridDay"
      },
      dateClick: function (info) {
          $('#schedule-start-date').val(info.dateStr)
          $('#schedule-end-date').val(info.dateStr)
          $('#date-event').modal('show')
      },
      eventClick: function (info) {
        $('#edit-appointment').modal('show');

        const editelem = document.getElementById('edit-appointment'); 
        editelem.querySelector('#title-drop-1').value = info.event.title
        editelem.querySelector('#dr-name-1').value = info.event.extendedProps.drname
        editelem.querySelector('[name="date"]').value= info.event.start
        editelem.querySelector('[name="end_date"]').value= info.event.end || info.event.start
        editelem.querySelector('[name="start_time"]').value= info.event.extendedProps.starttime|| 'enter time'
        editelem.querySelector('[name="end_time"]').value= info.event.extendedProps.endtime|| 'enter time'
        editelem.querySelector('[name="description"]').value= info.event.extendedProps.desc|| 'provide description'
        editelem.querySelector('#editconfirm').checked = info.event.extendedProps.checked
        editelem.querySelector('[name="service"]').value = "";
     },
     eventRender:function(info){

        var eventElement = info.el;
        var drname = info.event.extendedProps.drname;

        let $eventTime = document.createElement('div')
        $eventTime.classList.add("fc-content");
        
        let $time = document.createElement('span');
        $time.classList.add("fc-time", "text-uppercase"); 
        $time.append(eventElement.querySelector(".fc-time").textContent);
        $eventTime.append($time);

        let $evetDetail = document.createElement('span')
        $evetDetail.classList.add("fc-detail")

        // Create the h6 tag
        let $title = document.createElement('h6');
        $title.classList.add("fc-title");
        $title.append(eventElement.querySelector(".fc-title").textContent);

        // Append the h6 tag to $evetDetail
        $evetDetail.append($title);

        // Create an element to hold the extra text
        var eventExtraProsDrName = document.createElement('div');
        eventExtraProsDrName.classList.add('dr-name', 'text-body');
        eventExtraProsDrName.textContent = drname;
        
        $evetDetail.append(eventExtraProsDrName)

        eventElement.replaceChildren($eventTime,$evetDetail);

     },
      events: formattedEvents
  });
  bookingcalendar.render();
  });
  
}