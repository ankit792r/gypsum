console.log("JavaScript file loaded!");

document.addEventListener("DOMContentLoaded", function () {

console.log("DOM loaded!");

const status = document.getElementById("status");
const button = document.getElementById("testButton");
const result = document.getElementById("result");

// This proves that JavaScript executed
status.innerText = "✅ JavaScript is working!";

status.style.background = "#d4edda";
status.style.color = "#155724";

button.addEventListener("click", function () {

    console.log("Button clicked!");

    result.innerText =
        "🎉 JavaScript button click is working!\n\n" +
        "Time: " + new Date().toLocaleTimeString();

    result.style.background = "#d4edda";
    result.style.color = "#155724";
});


});
