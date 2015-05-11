// With JQuery
$('#points-id').slider({
	formatter: function(value) {
		return 'Current value: ' + value;
	}
});

function changeControls(mode)
{
    switch(mode)
    {
        case "animations":
        {
            hideElement("stillPanel");
            hideElement("scrollingTextPanel");
            
            showElement("animationsPanel");
            
            break;
        }
        case "still":
        {
            hideElement("animationsPanel");
            hideElement("scrollingTextPanel");
            
            showElement("stillPanel");
            
            break;
        }
        default:
	case "scrolling":
        {
            // scrolling text
            hideElement("animationsPanel");
            hideElement("stillPanel");
            
            showElement("scrollingTextPanel");
            
            break;
        }
    }
}

function changeScrollingText(text)
{
    var modeString = "text?t=" + text;
    
    modeChanged(modeString);
}

function changeScrollingTextSpeed(speed)
{
    var speedString = "text/speed/" + speed;
    
    modeChanged(speedString);
}

function changeScrollingTextColor(color)
{
    var hex = color.substring(1);
    var colorString = "text/color/" + hex;
    
    modeChanged(colorString);
}

function displayImage(imagePath, name)
{
    var mode;
    
    switch(imagePath)
    {
        case "animations/":
        {
            mode = "animation/";
            break;
        }
        default:
        {
            // still images
            mode = "still/";
        }
    }
    
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        logServerResponse(xmlhttp);
    }
    var url = "/api/" + mode + name;
    xmlhttp.open("POST", url, true);
    xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlhttp.send("&p=3");    
}

function hideElement(id)
{
    var element = document.getElementById(id);
    element.style.display = 'none';
}

function loadAnimations()
{
    var url = "/api/animation/list";
    var elementName = "animations";
    var imagePath = "animations/";
    
    loadImageList(url, elementName, imagePath);
}

function loadImageList(url, elementName, imagePath)
{
    logEvent("loading " + elementName + "...");
    
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if (xmlhttp.readyState==4 && xmlhttp.status==200)      
        {
            var list = xmlhttp.responseText;
            
            var names = list.split("-+-");
            
            var html = "<div class='thumb-holder'>";
            
 //           var c = 0;
            var columns = 4;
            
            for(var n in names)
            {
                var name = names[n].trim();
                
                var mod = parseInt(n) % columns;
                if(mod == 0)
                {
                    html += "<tr>";
                }
                
                if(name != "")
                {
                    html += "<div class='col-sm-3 col-md-2'>";
                    html += "<div class='thumbnail'>";
                    html += "<img src=\"/" + imagePath + name + "\" " + 
                                   "width=\"50\" height=\"50\" alt=\"" + name +  "\"" +  
                                   ">";
                    html += "<div class='caption text-center'>";
                    html += "<h5>" + name + "</h5>";                    
                    html += "<button onclick=\"displayImage('" + imagePath + "', '" + name + "')\" class=\"btn btn-primary text-center\">Display</button>";
                    html += "</div>";
                    html += "</div>";
                    html += "</div>";
                }
                
                if(mod == 0)
                {
                    html += "</tr>";
                }
            }
            
            html += "<div class=\"spacer\">&nbsp;</div>";
            
            html += "</div>";
            
            var e = document.getElementById(elementName);
            e.innerHTML = html;
            
            logEvent("done loading " + elementName);
        }
    }
    
    xmlhttp.open("POST", url, true);
    xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlhttp.send("&p=3");    
}

function loadImageResources()
{
    loadStillImages();
    loadAnimations();
}

function loadStillImages()
{
    var url = "/api/still/list";
    var elementName = "still";
    var imagePath = "images/";
    
    loadImageList(url, elementName, imagePath);
}

function logServerResponse(xmlhttp)
{
    if (xmlhttp.readyState==4 && xmlhttp.status==200)      
    {
        var s = xmlhttp.responseText;
        logEvent(s);
    }
}

function logEvent(message)
{
    var e = document.getElementById("logs");
    
    var logs = message + "<br/>" + e.innerHTML;
    
    e.innerHTML = logs;
}

function modeChanged(mode, imageName)
{
    if(imageName === null)
    {
        imageName = "";
    }
    
    changeControls(mode);
    
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        logServerResponse(xmlhttp);
    }
    var url = "/api/" + mode;
    
    if(imageName != "" && !(imageName === undefined))
    {
        url += "/" + imageName;
    }
    
    xmlhttp.open("POST", url, true);
    xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlhttp.send("&p=3");
}

function showElement(id)
{
    var element = document.getElementById(id);
    element.style.display = 'block';
}
