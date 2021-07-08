function changeControls(mode) {
  switch (mode) {
    case "animations":
      showElement("animationsPanel");
      break;
    case "still":
      showElement("stillPanel");
      break;

    default:
      // default is scrollingText
      showElement("scrollingTextPanel");
      break;
  }
}

function changeScrollingText(text) {
  modeChanged(`text?t=${text}`);
}

function changeScrollingTextSpeed(speed) {
  modeChanged(`text/speed/${speed}`);
}

function changeScrollingTextColor(color) {
  console.log(color);
  modeChanged(`text/color/${color.substring(1)}`);
}

const htmlEscape = (value) => {
  const output = value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
  return output;
};

const makeRequest = (url) => {
  const xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function () {
    logServerResponse(xmlhttp);
  };
  xmlhttp.open("POST", url, true);
  xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xmlhttp.send("&p=3");
};

const closeModal = () => {
  document.getElementsByClassName("modal")[0].style.display = "none";
};

function displayImage(imagePath, name) {
  switch (imagePath) {
    case "animations/save/":
      makeRequest(`/animations/write/${name}`);
      document.getElementsByClassName("modal")[0].style.display = "flex";
      break;

    case "animations/":
      makeRequest(`/animations/stream/${name}`);
      break;

    case "images/save/":
    default:
      makeRequest(`/still/save/${name}`);
      break;
  }
}

function getLastUpdateOrigin() {}

function hideElement(id) {
  const element = document.getElementById(id);
  element.style.display = "none";
}

function loadAnimations() {
  const url = "/animations/list";
  const elementName = "animations";
  const imagePath = "animations/";

  loadImageList(url, elementName, imagePath);
}

function loadImageList(url, elementName, imagePath) {
  logEvent("loading " + elementName + "...");

  const xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function () {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
      const list = xmlhttp.responseText;

      const names = list.split("-+-");
      let html = "<div class='thumb-holder'>";

      const thumbcard = names.map((item) => {
        const name = item.trim();

        if (name === "") {
          return "";
        }

        const saveButton = `<button onclick='displayImage("${imagePath}save/", "${htmlEscape(
          name
        )}")'>Save</button>`;
        const displayButton = `<button onclick='displayImage("${imagePath}", "${htmlEscape(
          name
        )}")'>Display</button>`;

        const showSave =
          imagePath === "images/" || imagePath === "animations/"
            ? ` ${saveButton}`
            : "";
        const output =
          `<div class='thumb'>` +
          `<h3>${name}</h3>` +
          `<img src="/files/${imagePath}${name}" alt="${name}" />` +
          `<span>` +
          displayButton +
          showSave +
          "</div>";
        return output;
      });

      html += thumbcard.join("");
      html += '<div class="spacer">&nbsp;</div>';

      html += "</div>";

      document.getElementById(elementName).innerHTML = html;
      logEvent("done loading " + elementName);
    }
  };

  xmlhttp.open("POST", url, true);
  xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xmlhttp.send("&p=3");
}

function loadImageResources() {
  loadStillImages();
  loadAnimations(); //this works
}

function loadStillImages() {
  const url = "/still/list";
  const elementName = "still";
  const imagePath = "images/";

  loadImageList(url, elementName, imagePath);
}

function logServerResponse(xmlhttp) {
  if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
    const s = xmlhttp.responseText;
    logEvent(s);
  }
}

function logEvent(message) {
  const e = document.getElementById("logs");
  const logs = message + "<br/>" + e.innerHTML;
  e.innerHTML = logs;
}

function modeChanged(mode, imageName) {
  if (imageName === null) {
    imageName = "";
  }

  changeControls(mode);

  const xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function () {
    logServerResponse(xmlhttp);
  };
  const url = "/" + mode;

  if (imageName != "" && !(imageName === undefined)) {
    url += "/" + imageName;
  }

  xmlhttp.open("POST", url, true);
  xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xmlhttp.send("&p=3");
}

function showElement(id) {
  hideElement("stillPanel");
  hideElement("scrollingTextPanel");
  hideElement("animationsPanel");
  const element = document.getElementById(id);
  element.style.display = "block";
}

function updateMode() {
  //    alert("in updateMode()");

  const xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function () {
    // display the correct mode UI
    const response = xmlhttp.responseText;
    const o = 2;

    switch (response) {
      case "ANIMATED_GIF": {
        modeChanged("animations");

        o = 0;

        break;
      }
      case "STILL_IMAGE": {
        modeChanged("still");
        o = 1;
        break;
      }
      default: {
        // scrolling text
        changeScrollingText("Pixelcade");

        break;
      }
    }

    document.getElementById("mode").selectedIndex = o;

    xmlhttp.responseText += " uploaded";

    logServerResponse(xmlhttp);
  };

  const url = "/upload/origin";
  xmlhttp.open("POST", url, true);
  xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xmlhttp.send("&p=3");
}
