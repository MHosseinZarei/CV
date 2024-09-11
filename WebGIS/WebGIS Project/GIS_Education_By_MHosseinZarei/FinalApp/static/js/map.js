var map = L.map('map').setView([32.5, 53.5], 5);

// Base layer
var baseLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 4,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

// Define color scale function
function getColor(area) {
    return area > 10000 ? '#800026' :
           area > 5000  ? '#BD0026' :
           area > 2000  ? '#E31A1C' :
           area > 1000  ? '#FC4E2A' :
           area > 500   ? '#FD8D3C' :
           area > 200   ? '#FEB24C' :
           area > 100   ? '#FED976' :
                          '#FFEDA0';
}

// Style function for GeoJSON layer
function style(feature) {
    return {
        fillColor: getColor(feature.properties.area),
        weight: 2,
        opacity: 1,
        color: 'white',
        dashArray: '3',
        fillOpacity: 0.7
    };
}

// Add city boundaries from GeoJSON data
var geojson_data = window.geojson_data; // Data passed from map.html
var areaLayer = L.geoJSON(geojson_data, {
    style: style,  // Add style function here
    onEachFeature: function (feature, layer) {
        if (feature.properties && feature.properties.cityname) {
            layer.bindPopup('<strong>' + feature.properties.cityname + '</strong><br>' +
                            'Object ID: ' + feature.properties.objectid_1 + '<br>' +
                            'Area: ' + feature.properties.area + '<br>' +
                            'Perimeter: ' + feature.properties.perimeter + '<br>' +
                            'Province Name: ' + feature.properties.provincnam + '<br>' +
                            'Shape Length: ' + feature.properties.shape_leng + '<br>' +
                            'Shape Area: ' + feature.properties.shape_area);
        }
    }
});

// Add POIs from GeoJSON data
var geojson_data1 = window.geojson_data1; // Data passed from map.html
var poisLayer = L.geoJSON(geojson_data1, {
    pointToLayer: function (feature, latlng) {
        return L.marker(latlng);
    },
    onEachFeature: function (feature, layer) {
        if (feature.properties && feature.properties.name) {
            layer.bindPopup('<strong>' + feature.properties.name + '</strong>');
        }
    }
});

// Add legend to map
var legend = L.control({ position: 'bottomleft' });

legend.onAdd = function (map) {
    var div = L.DomUtil.create('div', 'legend'),
        grades = [0, 100, 200, 500, 1000, 2000, 5000, 10000],
        labels = [];
    div.innerHTML = '<strong style="font-size: 16px; text-align: center; display: block; margin-bottom: 10px; font-family: Times, serif;">Area</strong>' + div.innerHTML;

    // Loop through density intervals and generate a label with a colored square for each interval
    for (var i = 0; i < grades.length; i++) {
        div.innerHTML +=
            '<i style="background:' + getColor(grades[i] + 1) + '"></i> ' +
            grades[i] + (grades[i + 1] ? '&ndash;' + grades[i + 1] + '<br>' : '+');
    }

    return div;
};

legend.addTo(map);

// Function to update map layers based on checkbox states
function updateMapLayers() {
    if (document.getElementById('layer-empty').checked) {
        map.removeLayer(areaLayer);
        map.removeLayer(poisLayer);
        document.querySelector('.legend').style.display = 'none';
    } else {
        baseLayer.addTo(map);
    }

    if (document.getElementById('layer-area').checked) {
        map.addLayer(areaLayer);
        document.querySelector('.legend').style.display = 'block';
    } else {
        map.removeLayer(areaLayer);
        document.querySelector('.legend').style.display = 'none';
    }

    if (document.getElementById('layer-pois').checked) {
        map.addLayer(poisLayer);
    } else {
        map.removeLayer(poisLayer);
    }
}

// Add event listeners to checkboxes
document.getElementById('layer-empty').addEventListener('change', updateMapLayers);
document.getElementById('layer-area').addEventListener('change', updateMapLayers);
document.getElementById('layer-pois').addEventListener('change', updateMapLayers);

// Initial map layer update
updateMapLayers();

// Function to handle POI search
function searchPOI() {
    var searchValue = document.getElementById('poi-search').value.trim().toLowerCase();
    var found = false;
    
    geojson_data1.features.forEach(function(feature) {
        if (feature.properties.name.toLowerCase() === searchValue) {
            var coordinates = feature.geometry.coordinates;

            // Zoom to the POI location with animation over 5 seconds
            map.flyTo([coordinates[1], coordinates[0]], 17, {
                animate: true,
                duration: 5 
            });


            L.popup()
                .setLatLng([coordinates[1], coordinates[0]])
                .setContent('<strong>' + feature.properties.name + '</strong>')
                .openOn(map);
            found = true;
        }
    });

    if (!found) {
        document.getElementById('error-message').style.display = 'block';
        document.getElementById('error-message').style.opacity = '1';
        setTimeout(function() {
            document.getElementById('error-message').style.opacity = '0';
        }, 2000); 
    }

    return false; // Prevent form submission
}

// Function to download the map as a PNG
function downloadMap() {
    leafletImage(map, function(err, canvas) {
        var img = document.createElement('img');
        var dimensions = map.getSize();
        img.width = dimensions.x;
        img.height = dimensions.y;
        img.src = canvas.toDataURL();
        var link = document.createElement('a');
        link.href = img.src;
        link.download = 'map.png';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    });
}

// Function to download the POIs as a Shapefile
function downloadShapefile() {
    window.location.href = exportUrl;
}



var marker;


// Click event to add marker
map.on('click', function(e) {
    var addMarkerCheckbox = document.getElementById('add-marker-checkbox');
    if (addMarkerCheckbox.checked) {
        if (marker) {
            map.removeLayer(marker); // Remove previous marker if exists
        }
        marker = L.marker(e.latlng).addTo(map);
        document.getElementById('poi-lat').value = e.latlng.lat;
        document.getElementById('poi-lng').value = e.latlng.lng;
    }
});

// Function to submit POI form
function submitPOI() {
    var name = document.getElementById('poi-name').value;
    var lat = document.getElementById('poi-lat').value;
    var lng = document.getElementById('poi-lng').value;

    if (name && lat && lng) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', addPoiUrl, true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.setRequestHeader('X-CSRFToken', csrfToken);
        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4 && xhr.status == 200) {
                var response = JSON.parse(xhr.responseText);
                if (response.status === 'success') {
                    var thankYouMessage = document.getElementById('thank-you-message');
                    thankYouMessage.style.opacity = '1'; 
                    thankYouMessage.style.display = 'block';
                    
                    setTimeout(function() {
                        thankYouMessage.style.opacity = '0';
                        setTimeout(function() {
                            thankYouMessage.style.display = 'none';
                        }, 1000); 
                    }, 3000); 

                    if (marker) {
                        map.removeLayer(marker); // Remove marker after successfully adding POI
                        marker = null; // Reset marker variable
                    }
                    document.getElementById('poi-form').reset(); // Reset form fields
                } else {
                    alert('Failed to add POI');
                }
            }

        };
        xhr.send('name=' + name + '&lat=' + lat + '&lng=' + lng);
    } else {
        alert('Please provide a name and select a location on the map');
    }

    return false; // Prevent form submission
}

var userMarker;
var nearestLine;
var distanceLabel;
var poiMarkers = []; // To store POI markers
var nearestCourseActive = false; // Flag to track the state of the Nearest Course button

map.on('click', function(e) {
if (nearestCourseActive) {
if (userMarker) {
    map.removeLayer(userMarker);
}
if (nearestLine) {
    map.removeLayer(nearestLine);
    nearestLine = null;
}
if (distanceLabel) {
    map.removeLayer(distanceLabel);
    distanceLabel = null;
}
userMarker = L.marker(e.latlng).addTo(map);
document.getElementById('poi-lat').value = e.latlng.lat;
document.getElementById('poi-lng').value = e.latlng.lng;
}
});

function calculateDistance(latlng1, latlng2) {
var R = 6371000; 
var dLat = (latlng2.lat - latlng1.lat) * Math.PI / 180;
var dLon = (latlng2.lng - latlng1.lng) * Math.PI / 180;
var lat1 = latlng1.lat * Math.PI / 180;
var lat2 = latlng2.lat * Math.PI / 180;

var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
var distance = R * c;
return distance;
}

function showNearestCourse(userLatLng) {

if (nearestLine) {
map.removeLayer(nearestLine);
}
if (distanceLabel) {
map.removeLayer(distanceLabel);
}

var nearestDistance = Infinity;
var nearestPoi = null;
var nearestPoiLatLng = null;

poisLayer.eachLayer(function(layer) {
var poiLatLng = layer.getLatLng();
var distance = calculateDistance(userLatLng, poiLatLng);

if (distance < nearestDistance) {
    nearestDistance = distance;
    nearestPoi = layer;
    nearestPoiLatLng = poiLatLng;
}
});

if (nearestPoi) {
nearestLine = L.polyline([userLatLng, nearestPoiLatLng], {color: 'red'}).addTo(map);

var midPointLat = (userLatLng.lat + nearestPoiLatLng.lat) / 2;
var midPointLng = (userLatLng.lng + nearestPoiLatLng.lng) / 2;

distanceLabel = L.marker([midPointLat, midPointLng], {
    icon: L.divIcon({
        className: 'distance-label',
        html: '<div style="background: white; padding: 5px; border-radius: 5px; box-shadow: 0 0 5px rgba(0,0,0,0.5);">' +
              'Distance: ' + Math.round(nearestDistance) + ' meters' + '</div>',
        iconSize: [100, 40]
    })
}).addTo(map);
}
}



document.getElementById('nearest-course-btn').addEventListener('click', function() {
nearestCourseActive = !nearestCourseActive; // Toggle the state of the button

if (nearestCourseActive) {
this.textContent = 'Stop Analysis'; // Change button text to indicate active state
} else {
this.textContent = 'Nearest Course'; // Change button text back to normal

if (nearestLine) {
    map.removeLayer(nearestLine);
    nearestLine = null;
}
if (distanceLabel) {
    map.removeLayer(distanceLabel);
    distanceLabel = null;
}
if (userMarker) {
    map.removeLayer(userMarker);
    userMarker = null;
}
}
});

function performAnalysis() {
if (userMarker && nearestCourseActive) {
showNearestCourse(userMarker.getLatLng());
}
}

// Call performAnalysis function every time the map is clicked
map.on('click', performAnalysis);
