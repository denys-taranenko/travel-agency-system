async function initializeStripePayment(stripePublicKey, orderId) {
    const stripe = Stripe(stripePublicKey);
    const elements = stripe.elements();

    const card = elements.create('card', {
        style: {
            base: {
                fontFamily: '"Segoe UI", system-ui, sans-serif',
                fontSize: '16pxpx',
                color: '#32325d',
                '::placeholder': { color: '#aab7c4' }
            },
            invalid: { color: '#fa755a' }
        },
        hidePostalCode: true
    });

    card.mount('#card-element');

    const form = document.getElementById('payment-form');
    const submitBtn = document.getElementById('submit-btn');
    const errorDisplay = document.getElementById('card-errors');

    card.addEventListener('change', ({error}) => {
        errorDisplay.textContent = error?.message || '';
        submitBtn.disabled = !!error;
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        submitBtn.disabled = true;

        try {
            const { token, error } = await stripe.createToken(card);

            if (error) throw error;

            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'stripeToken';
            input.value = token.id;
            form.appendChild(input);

            form.submit();

        } catch (error) {
            errorDisplay.textContent = error.message;
            submitBtn.disabled = false;
        }
    });
}
