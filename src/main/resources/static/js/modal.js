document.addEventListener('DOMContentLoaded', function() {
    document.addEventListener('click', function(e) {
        const button = e.target.closest('[data-modal-type]');
        if (button) {
            const orderId = button.getAttribute('data-order-id');
            const modalType = button.getAttribute('data-modal-type');
            toggleModal(orderId, modalType);
        }

        if (e.target.closest('[data-dismiss="modal"]')) {
            const modal = e.target.closest('.custom-modal');
            if (modal) {
                modal.classList.remove('active');
                setTimeout(() => modal.classList.add('modal-hidden'), 300);
            }
        }
    });

    document.querySelectorAll('.custom-modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('active');
                setTimeout(() => this.classList.add('modal-hidden'), 300);
            }
        });
    });
});

function toggleModal(orderId, type) {
    const modalMap = {
        cancel: 'cancelOrderModal-',
        pay: 'payOrderModal-',
        delete: 'deleteOrderModal-'
    };
    
    const modalId = modalMap[type] + orderId;
    const modal = document.getElementById(modalId);
    
    if (modal) {
        modal.classList.remove('modal-hidden');
        setTimeout(() => modal.classList.add('active'), 10);
    }
}
