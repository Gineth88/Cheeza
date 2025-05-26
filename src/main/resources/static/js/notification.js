const WS_ENABLED = true;               // Set to false to force HTTP polling only
const POLL_INTERVAL_MS = 10000;        // How often to poll (10s)
const NOTIF_API_URL = '/api/notifications';           // List endpoint
const NOTIF_MARK_READ_URL = '/api/notifications/mark-as-read'; // Mark as read endpoint

// ---- MAIN LOGIC ----
let stompClient = null;
let pollingTimer = null;
let webSocketConnected = false;

// Called when notification received (WebSocket or HTTP)
function handleNotification(notification) {
    addNotificationToDropdown(notification);
    updateNotificationBadge();
    showToastNotification(notification);
}

// ---- WEBSOCKET FUNCTIONS ----

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        webSocketConnected = true;
        if (pollingTimer) clearInterval(pollingTimer);
        console.log("WebSocket connected", frame);

        stompClient.subscribe('/user/queue/notifications', function(message) {
            const notif = JSON.parse(message.body);
            handleNotification(notif);
        });
    }, function(error) {
        webSocketConnected = false;
        console.error('WebSocket error', error);
        // Fallback to polling
        startPolling();
    });
}

// ---- HTTP POLLING FUNCTIONS ----

function startPolling() {
    if (pollingTimer) clearInterval(pollingTimer);
    pollingTimer = setInterval(fetchNotifications, POLL_INTERVAL_MS);
    // Fetch immediately as well
    fetchNotifications();
}

function fetchNotifications() {
    fetch(NOTIF_API_URL)
        .then(res => res.json())
        .then(data => {
            // data may be {error: ...} or a notification list
            if (Array.isArray(data)) {
                // Show unread notifications only
                data.filter(n => n.read === false).forEach(handleNotification);
            }
        });
}

// ---- UI HELPERS ----

function addNotificationToDropdown(notification) {
    const dropdown = document.getElementById('notificationDropdown');
    if (!dropdown) return;

    // Get list holder (skip header)
    let notificationList = dropdown.querySelector('.notification-header').nextElementSibling;
    // Remove "no notifications" if present
    if (notificationList && notificationList.classList.contains("text-center")) {
        notificationList.innerHTML = '';
    }
    // Avoid duplicates (based on ID/title/message/time)
    if (notificationList && notificationList.innerHTML.includes(notification.title)
        && notificationList.innerHTML.includes(notification.message)
    ) {
        return;
    }
    const div = document.createElement('div');
    div.className = 'notification-item unread';
    div.innerHTML = `
        <div class="notification-title">${notification.title}</div>
        <div class="notification-message">${notification.message}</div>
        <div class="notification-time">${formatDateTime(notification.createdAt)}</div>
    `;
    notificationList.prepend(div);
}

function updateNotificationBadge() {
    const badge = document.querySelector('.notification-badge');
    if (badge) {
        badge.style.display = 'block';
        // Update count based on .unread items in dropdown
        const unreadCount = document.querySelectorAll('.notification-item.unread').length;
        badge.textContent = unreadCount;
    }
}

function showToastNotification(notification) {
    // Simple toast example, replace as desired
    const toast = document.createElement('div');
    toast.className = 'position-fixed bottom-0 end-0 m-3';
    toast.innerHTML = `
        <div class="toast show" role="alert" style="min-width: 320px">
            <div class="toast-header">
                <strong class="me-auto">${notification.title}</strong>
                <small>${formatDateTime(notification.createdAt)}</small>
                <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">${notification.message}</div>
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString();
}

// ---- ON PAGE LOAD ----
document.addEventListener('DOMContentLoaded', function() {
    if (WS_ENABLED && typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        connectWebSocket();
    } else {
        startPolling();
    }

    // Notification dropdown open: mark all as read
    document.querySelector('.notification-icon')?.addEventListener('click', function() {
        fetch(NOTIF_MARK_READ_URL, {method: 'POST'});
        // visually mark as read the displayed notifications
        document.querySelectorAll('.notification-item.unread').forEach(el => el.classList.remove('unread'));
        updateNotificationBadge();
    });

    // Responsive: fallback if WebSocket disconnects later
    setInterval(() => {
        if (WS_ENABLED && !webSocketConnected) startPolling();
    }, 15000);
});

/* Expose for manual test if include from profile.html */
window.testManualNotification = function() {
    fetch('/api/test/send-notification', {method: 'POST'})
        .then(response => response.text())
        .then(console.log)
        .catch(console.error);
}
