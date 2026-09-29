const NativeBridge = {

postMessage(type, data = {}) {

    window.AndroidBridge.postMessage(
        JSON.stringify({
            type: type,
            data: data
        })
    );
},

onMessage(callback) {
    window.addEventListener("message", function (event) {
        callback(event)
    });
}
};


document.addEventListener("DOMContentLoaded", function () {
NativeBridge.onMessage(function(message) {
    console.log("got message from kotlin", message)
});

NativeBridge.postMessage(
    "Ping",
    {
        message: "JS Loaded"
    }
);

})