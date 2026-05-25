function reloadOnBackForward() {
    window.addEventListener("pageshow", (event) => {
        const isBackForward =
            event.persisted ||
            performance.getEntriesByType("navigation")[0]?.type === "back_forward";

        if (isBackForward) {
            window.location.reload();
        }
    });
}

window.createQRCode = function () {
    const $qr = $("#qr");
    const $qrText = $("#qrText");

    $.ajax({
        type: "GET",
        url: "/util/qrcode",
        contentType: "application/json"
    })
        .done((base64Image) => {
            $qr.attr("src", `data:image/png;base64,${base64Image}`);
            $qrText.show();
        })
        .fail(() => {
            alert("Error create QR code");
        });
};

reloadOnBackForward();