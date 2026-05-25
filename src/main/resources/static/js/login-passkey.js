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
    for (let i = 0; i < bytes.byteLength; i++) {
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
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');

    if (!tokenMeta || !headerMeta) {
        throw new Error('No CSRF token was found on the login page.');
    }

    return {
        token: tokenMeta.content,
        header: headerMeta.content
    };
}

function setLoginPasskeyMessage(message, isError = false) {
    const el = document.getElementById('login-passkey-message');
    if (!el) return;

    el.textContent = message;
    el.className = isError ? 'text-danger' : 'text-success';
}

async function loginWithPasskey() {
    try {
        if (!window.PublicKeyCredential) {
            setLoginPasskeyMessage('This browser does not support passkeys/WebAuthn.', true);
            return;
        }

        const { token, header } = getCsrf();

        // 1) Fetch authentication options from the server.
        const optionsResponse = await fetch('/webauthn/authenticate/options', {
            method: 'POST',
            headers: {
                [header]: token
            },
            credentials: 'same-origin'
        });

        if (!optionsResponse.ok) {
            const text = await optionsResponse.text();
            throw new Error(text || 'Cannot get authentication options');
        }

        const publicKeyOptions = await optionsResponse.json();

        // 2) Decode challenge
        publicKeyOptions.challenge = base64UrlToUint8Array(publicKeyOptions.challenge);

        // 3) Decode allowCredentials
        if (Array.isArray(publicKeyOptions.allowCredentials)) {
            publicKeyOptions.allowCredentials = publicKeyOptions.allowCredentials.map(item => ({
                ...item,
                id: base64UrlToUint8Array(item.id)
            }));
        }

        // 4) Call the WebAuthn API to get the assertion.
        const credential = await navigator.credentials.get({
            publicKey: publicKeyOptions
        });

        if (!credential) {
            throw new Error('Failed to retrieve the passkey assertion.');
        }

        // 5) Create a payload for the assertion and send it to the server.
        const payload = {
            id: credential.id,
            rawId: arrayBufferToBase64Url(credential.rawId),
            response: {
                authenticatorData: arrayBufferToBase64Url(credential.response.authenticatorData),
                clientDataJSON: arrayBufferToBase64Url(credential.response.clientDataJSON),
                signature: arrayBufferToBase64Url(credential.response.signature),
                userHandle: credential.response.userHandle
                    ? arrayBufferToBase64Url(credential.response.userHandle)
                    : null
            },
            type: credential.type,
            clientExtensionResults: credential.getClientExtensionResults(),
            authenticatorAttachment: credential.authenticatorAttachment
        };

        // 6) Send assertion to WebAuthn login endpoint
        const loginResponse = await fetch('/login/webauthn', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        });

        if (!loginResponse.ok) {
            const text = await loginResponse.text();
            throw new Error(text || 'Passkey login failed.');
        }

        const result = await loginResponse.json();

        if (result.authenticated) {
            setLoginPasskeyMessage('Login successful.');
            if (result.redirectUrl) {
                window.location.href = '/home';
            } else {
                window.location.href = '/';
            }
        } else {
            setLoginPasskeyMessage('Passkey login failed.', true);
        }
    } catch (error) {
        console.error(error);
        setLoginPasskeyMessage(error.message || 'An error occurred while logging in with passkey.', true);
    }
}