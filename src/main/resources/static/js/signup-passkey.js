function base64UrlToUint8Array(base64url) {
    const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    const pad = '='.repeat((4 - (base64.length % 4)) % 4);
    const binary = atob(base64 + pad);
    const bytes = new Uint8Array(binary.length);

    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }

    return bytes;
}

function uint8ArrayToBase64Url(bytes) {
    let binary = '';
    const len = bytes.byteLength;

    for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
    }

    return btoa(binary)
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=+$/g, '');
}

function arrayBufferToBase64Url(buffer) {
    return uint8ArrayToBase64Url(new Uint8Array(buffer));
}

function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    return { token, header };
}

function setMessage(message, isError = false) {
    const el = document.getElementById('passkey-message');
    if (el) {
        el.textContent = message;
        el.style.color = isError ? 'red' : 'green';
    }
}

async function registerPasskey() {
    try {
        if (!window.PublicKeyCredential) {
            setMessage('This browser does not support passkeys/WebAuthn.', true);
            return;
        }

        const labelInput = document.getElementById('passkeyLabel');
                const passkeyLabel = labelInput && labelInput.value.trim()
                    ? labelInput.value.trim()
                    : 'My device';

        const { token, header } = getCsrf();

        // 1) Fetch registration options from Spring Security.
        const optionsResponse = await fetch('/webauthn/register/options', {
            method: 'POST',
            headers: {
                [header]: token
            }
        });

        if (!optionsResponse.ok) {
            throw new Error('Failed to retrieve registration options.');
        }

        const publicKeyOptions = await optionsResponse.json();

        // 2) Decode the binary fields.
        publicKeyOptions.challenge = base64UrlToUint8Array(publicKeyOptions.challenge);
        publicKeyOptions.user.id = base64UrlToUint8Array(publicKeyOptions.user.id);

        if (Array.isArray(publicKeyOptions.excludeCredentials)) {
            publicKeyOptions.excludeCredentials = publicKeyOptions.excludeCredentials.map(item => ({
                ...item,
                id: base64UrlToUint8Array(item.id)
            }));
        }

        // 3) Call the WebAuthn API in the browser.
        const credential = await navigator.credentials.create({
            publicKey: publicKeyOptions
        });

        if (!credential) {
            throw new Error('Passkey creation failed.');
        }

        // 4) Encode the result to send to the server.
        const payload = {
            publicKey: {
                credential: {
                    id: credential.id,
                    rawId: arrayBufferToBase64Url(credential.rawId),
                    response: {
                        attestationObject: arrayBufferToBase64Url(
                            credential.response.attestationObject
                        ),
                        clientDataJSON: arrayBufferToBase64Url(
                            credential.response.clientDataJSON
                        ),
                        transports: typeof credential.response.getTransports === 'function'
                            ? credential.response.getTransports()
                            : []
                    },
                    type: credential.type,
                    clientExtensionResults: credential.getClientExtensionResults(),
                    authenticatorAttachment: credential.authenticatorAttachment
                },
                label: passkeyLabel
            }
        };

        // 5) Send the credential to Spring Security for registration.
        const registerResponse = await fetch('/webauthn/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            body: JSON.stringify(payload)
        });

        if (!registerResponse.ok) {
            const text = await registerResponse.text();
            throw new Error(text || 'Passkey registration failed.');
        }

        const result = await registerResponse.json();
        if (result.success) {
            setMessage('Passkey registration was successful.');
            window.location.reload();
        } else {
            setMessage('Passkey registration failed.', true);
        }
    } catch (error) {
        console.error(error);
        setMessage(error.message || 'An error occurred while registering the passkey.', true);
    }
}