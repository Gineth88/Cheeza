let stompClient = null;
let subscription = null;

function connectToOrderUpdates(orderId) {
    const socket = new SockJS('/ws-order-tracking');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        subscription = stompClient.subscribe(
            `/topic/order-status/${orderId}`, 
            function(message) {
                const update = JSON.parse(message.body);
                updateOrderUI(update);
            }
        );
    }, function(error) {
        console.error('WebSocket Error:', error);
        setTimeout(() => connectToOrderUpdates(orderId), 5000);
    });
}

function updateOrderUI(update) {
    // Update status text
    document.getElementById('order-status').textContent = update.status;
    document.getElementById('last-updated').textContent = 
        new Date(update.lastUpdated).toLocaleString();
    document.getElementById('est-delivery').textContent = 
        new Date(update.estimatedDelivery).toLocaleString();
    
    // Update progress
    updateProgressUI(update.status);
    
    // Update milestone indicators
    document.querySelectorAll('.status-milestones span').forEach(span => {
        const statusOrdinal = Object.values(OrderStatus).indexOf(update.status);
        const spanOrdinal = Object.values(OrderStatus).indexOf(span.textContent);
        span.classList.toggle('completed', spanOrdinal <= statusOrdinal);
    });
}

function updateProgressUI(status) {
    const statusOrder = Object.values(OrderStatus);
    const currentIndex = statusOrder.indexOf(status);
    const progressPercent = (currentIndex / (statusOrder.length - 1)) * 100;
    document.getElementById('order-progress').style.width = `${progressPercent}%`;
}

// Disconnect when leaving page
window.addEventListener('beforeunload', () => {
    if (subscription) subscription.unsubscribe();
    if (stompClient) stompClient.disconnect();
});

// Mock OrderStatus enum for frontend
const OrderStatus = {
    RECEIVED: 'RECEIVED',
    PREPARING: 'PREPARING',
    BAKING: 'BAKING',
    QUALITY_CHECK: 'QUALITY_CHECK',
    OUT_FOR_DELIVERY: 'OUT_FOR_DELIVERY',
    DELIVERED: 'DELIVERED',
    CANCELLED: 'CANCELLED'
};