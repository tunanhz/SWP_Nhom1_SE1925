const accountString = localStorage.getItem("account");
const account = JSON.parse(accountString);
const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${account.accountPatientId}`;

async function callAPI() {
    const response = await fetch(baseAPI, {
        method: "GET",
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
    }

    const data = await response.json();
    console.log("API Response:", data.allAppointment);
    return data.allAppointment;
}

var bookingcalendar;
if (document.querySelectorAll("#bookingcalendar").length) {
    document.addEventListener("DOMContentLoaded", async function () {
        const allAppointment = await callAPI();

        const formattedEvents = allAppointment.map((appointment) => {
            const startDate = moment()
                .startOf("day")
                .add(parseInt(appointment.daysUntilAppointment), "days")
                .format("YYYY-MM-DD");
            const startDateTime = `${startDate}T${appointment.appointmentTime}`;

            console.log(`Event: ${appointment.note}, Start: ${startDateTime}`);

            return {
                id: appointment.appointmentId,
                title: appointment.note || "Appointment",
                start: startDateTime,
                backgroundColor: "rgba(var(--bs-primary-rgb), 0.1)",
                textColor: "var(--bs-primary)",
                borderColor: "var(--bs-primary)",
                extendedProps: {
                    drname: appointment.doctorName || "Unknown Doctor",
                    patientName: appointment.patient.fullName || "Unknown Patient",
                    starttime: appointment.appointmentTime || "00:00:00",
                    endtime: moment(startDateTime).add(30, "minutes").format("HH:mm:ss"),
                    desc: appointment.note || "No description",
                    status: appointment.appointmentStatus || "Pending",
                },
            };
        });

        let calendarEl = document.getElementById("bookingcalendar");
        bookingcalendar = new FullCalendar.Calendar(calendarEl, {
            selectable: true,
            plugins: ["timeGrid", "dayGrid", "list", "interaction"],
            timeZone: "Asia/Ho_Chi_Minh",
            defaultView: "timeGridDay",
            contentHeight: "auto",
            eventLimit: true,
            dayMaxEvents: 4,
            header: {
                left: "prev,next today",
                center: "title",
                right: "dayGridMonth,timeGridWeek,timeGridDay",
            },
            dateClick: function (info) {
                $("#schedule-start-date").val(info.dateStr);
                $("#schedule-end-date").val(info.dateStr);
                $("#date-event").modal("show");
            },
            eventClick: function (info) {
                $("#edit-appointment").modal("show");

                const editelem = document.getElementById("edit-appointment");
                editelem.querySelector('[name="id"]').value = info.event.id || "";
                editelem.querySelector("#title-drop-1").value = info.event.title;
                editelem.querySelector("#dr-name-1").value = info.event.extendedProps.drname;
                editelem.querySelector("#pt-name-1").value = info.event.extendedProps.patientName;
                editelem.querySelector('[name="date"]').value = moment(info.event.start).format("YYYY-MM-DD");
                editelem.querySelector('[name="time"]').value = info.event.extendedProps.starttime; 
                editelem.querySelector('[name="status"]').value = info.event.extendedProps.status;
            },
            eventRender: function (info) {
                var eventElement = info.el;
                var drname = info.event.extendedProps.drname;
                var startTime = info.event.extendedProps.starttime; 

                let $eventTime = document.createElement("div");
                $eventTime.classList.add("fc-content");

                let $time = document.createElement("span");
                $time.classList.add("fc-time", "text-uppercase");
                $time.append(moment(startTime, "HH:mm:ss").format("HH:mm"));
                $eventTime.append($time);

                let $evetDetail = document.createElement("span");
                $evetDetail.classList.add("fc-detail");

                let $title = document.createElement("h6");
                $title.classList.add("fc-title");
                $title.append(info.event.title);
                $evetDetail.append($title);

                var eventExtraProsDrName = document.createElement("div");
                eventExtraProsDrName.classList.add("dr-name", "text-body");
                eventExtraProsDrName.textContent = drname;
                $evetDetail.append(eventExtraProsDrName);

                eventElement.replaceChildren($eventTime, $evetDetail);
            },
            events: formattedEvents,
        });
        bookingcalendar.render();
    });
}