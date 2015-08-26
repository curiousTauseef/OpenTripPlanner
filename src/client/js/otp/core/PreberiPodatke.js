/*
 *
 * Klemen Koželj 17.8.2015
 *
 */


function preberiPodatke(url){
	var vrni;
	$.ajax({
            method: "GET",
            url: url,
            async: false,
            success: function (data) {
                vrni =  data;
            }
        });
	return vrni;
}

var resourcePath = otp.config.resourcePath || "";

var yellowParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkyellow.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greenParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkgreen.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var redParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkred.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greyParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkgrey.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var yellowBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bikeyellow.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greenBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bikegreen.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var redBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bikered.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greyBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bikegrey.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var yellowStopwatch = L.icon({
    iconUrl: resourcePath + 'images/stevci/counteryellow.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greenStopwatch = L.icon({
    iconUrl: resourcePath + 'images/stevci/countergreen.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var redStopwatch = L.icon({
    iconUrl: resourcePath + 'images/stevci/counterred.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greyStopwatch = L.icon({
    iconUrl: resourcePath + 'images/stevci/countergrey.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var work = L.icon({
    iconUrl: resourcePath + 'images/dogodki/work.png',
    iconSize:     [30, 30],
});

var closed22 = L.icon({
    iconUrl: resourcePath + 'images/dogodki/closed.png',
    iconSize:     [30, 30],
});

var jam = L.icon({
    iconUrl: resourcePath + 'images/dogodki/jam.png',
    iconSize:     [30, 30],
});

var truck = L.icon({
    iconUrl: resourcePath + 'images/dogodki/trucks.png',
    iconSize:     [30, 30],
});

var warning = L.icon({
    iconUrl: resourcePath + 'images/dogodki/warning.png',
    iconSize:     [30, 30],
});

var yellowCar = L.icon({
    iconUrl: resourcePath + 'images/car/caryellow.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greenCar = L.icon({
    iconUrl: resourcePath + 'images/car/cargreen.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var redCar = L.icon({
    iconUrl: resourcePath + 'images/car/carred.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

var greyCar = L.icon({
    iconUrl: resourcePath + 'images/car/cargrey.png',
    iconSize:     [25, 45],
    iconAnchor:   [12.5, 45]
});

function parseParkirisca(ObjPa){
    var parkings_array = [];
    ObjPa.forEach(function(entry) {
        var ime = entry.Ime;
        var pZaInvalide = entry.Invalidi_St_mest;
        var vsehMest = entry.St_mest;
        var y = entry.KoordinataY_wgs;
        var x = entry.KoordinataX_wgs;
        var opis = entry.Opis;
        var delavnik = entry.U_delovnik;
        if(!delavnik){
            delavnik = "";
        }
        if(y !== null && x !== null){
            if(y !== undefined && x !== undefined){
                if(entry.zasedenost){
                var vsehZasedenih = entry.zasedenost.P_kratkotrajniki;
                var prosta_mesta = parseInt(Math.abs(vsehMest)) - parseInt(Math.abs(vsehZasedenih));
                var procentZasedenost = parseInt(Math.abs(vsehZasedenih)) * 1.0 / parseInt(Math.abs(vsehMest));
                if(procentZasedenost >= 0.9){
                    var marker = L.marker([y, x], {icon: redParking});               
                    marker.bindPopup(ime + "<br># Mesta za invalide: " + pZaInvalide +"<br> # vseh mest: " + vsehMest + "<br> # prostih: " + prosta_mesta + "<br> # zasedenih: " + vsehZasedenih + "<br> " + opis +"<br> " + delavnik).openPopup();
                    parkings_array[parkings_array.length] = marker;
                } else if( procentZasedenost <= 0.1){
                    var marker = L.marker([y, x], {icon: greenParking});               
                    marker.bindPopup(ime + "<br># Mesta za invalide: " + pZaInvalide +"<br> # vseh mest: " + vsehMest + "<br> # prostih: " + prosta_mesta + "<br> # zasedenih: " + vsehZasedenih + "<br> " + opis +"<br> " + delavnik).openPopup();
                    parkings_array[parkings_array.length] = marker;
                }else {
                    var marker = L.marker([y, x], {icon: yellowParking});               
                    marker.bindPopup(ime + "<br># Mesta za invalide: " + pZaInvalide +"<br> # vseh mest: " + vsehMest + "<br> # prostih: " + prosta_mesta + "<br> # zasedenih: " + vsehZasedenih + "<br> " + opis +"<br> " + delavnik).openPopup();
                    parkings_array[parkings_array.length] = marker;
                }
                } else {
                    var marker = L.marker([y, x], {icon: greyParking})
                    marker.bindPopup(ime + "<br># Mesta za invalide: " + pZaInvalide +"<br> # vseh mest: " + vsehMest + "<br> # prostih: " + prosta_mesta + "<br> # zasedenih: " + vsehZasedenih + "<br> " + opis +"<br> " + delavnik).openPopup();
                    parkings_array[parkings_array.length] = marker;                }
            }
        }        
    });
    var parkings_layer = L.layerGroup(parkings_array);
    return parkings_layer;
}

function parserBicikelj(objBLJ){
    var bicikeLJ_array = [];
    objBLJ.forEach(function(entry){
        var ime = entry.name;
        var address = entry.address;
        var x = entry.position.lng;
        var y = entry.position.lat;
        var all = parseInt(entry.bike_stands);
        var bikes = parseInt(entry.available_bikes);
        var stands = parseInt(entry.available_bike_stands);
        if(bikes > 5 && stands > 5){
            var marker = L.marker([y, x], {icon: greenBicikelj})
            marker.bindPopup(ime + "<br> # vseh mest: " + all + "<br> # prosta kolesa: " + bikes + "<br> # prosta mesta: " + stands);
            bicikeLJ_array[bicikeLJ_array.length] = marker;
        } else if (bikes > 0 && stands > 0){
            var marker = L.marker([y, x], {icon: yellowBicikelj})
            marker.bindPopup(ime + "<br> # vseh mest: " + all + "<br> # prosta kolesa: " + bikes + "<br> # prosta mesta: " + stands);
            bicikeLJ_array[bicikeLJ_array.length] = marker;
        } else if ( bikes === 0 || stands === 0){
            var marker = L.marker([y, x], {icon: redBicikelj})
            marker.bindPopup(ime + "<br> # vseh mest: " + all + "<br> # prosta kolesa: " + bikes + "<br> # prosta mesta: " + stands);
            bicikeLJ_array[bicikeLJ_array.length] = marker;
        } else {
            var marker = L.marker([y, x], {icon: greyBicikelj})
            marker.bindPopup(ime + "<br> # vseh mest: " + all + "<br> # prosta kolesa: " + bikes + "<br> # prosta mesta: " + stands);
            bicikeLJ_array[bicikeLJ_array.length] = marker;
        }
    });
    var bicikeLJ_layer = L.layerGroup(bicikeLJ_array);
    return bicikeLJ_layer; 
}

function parseStevecPrometa(objST){
    var stevci_array = [];
    objST.forEach(function(stevec){
        var ime = stevec.stevci_cestaOpis;
        var smer = stevec.stevci_smerOpis;
        var x = stevec.stevci_geoX_wgs;
        var y = stevec.stevci_geoY_wgs;
        var zasedenost = parseInt(stevec.stevci_occ);
        var opis = stevec.summary;
        if(zasedenost){
            if(zasedenost >= 750){
                var marker = L.marker([y, x], {icon: redStopwatch})
                marker.bindPopup(opis);
                stevci_array[stevci_array.length] = marker;
            }else if(zasedenost <= 250){
                var marker = L.marker([y, x], {icon: greenStopwatch})
                marker.bindPopup(opis);
                stevci_array[stevci_array.length] = marker;
            }else{
                var marker = L.marker([y, x], {icon: yellowStopwatch})
                marker.bindPopup(ime + " " + smer + "<br>" + opis);
                stevci_array[stevci_array.length] = marker;
            }
        }else{
            var marker = L.marker([y, x], {icon: greyStopwatch})
            marker.bindPopup(opis);
            stevci_array[stevci_array.length] = marker;
        }
    });
    var stevci_layer = L.layerGroup(stevci_array);
    return stevci_layer;
}

function parserDogodkiNaCestah(objDG){
    var dogodki_array = [];
    objDG.forEach(function(dog){
        var vzrok = dog.vzrok;
        var opis = dog.opis;
        var y = dog.y_wgs;
        var x = dog.x_wgs;
        if(vzrok === "Izredni dogodek"){
            var marker = L.marker([y, x], {icon: warning})
            marker.bindPopup(opis);
            dogodki_array[dogodki_array.length] = marker;
        } else if(vzrok === "Delo na cesti"){
            var marker = L.marker([y, x], {icon: work})
            marker.bindPopup(opis);
            dogodki_array[dogodki_array.length] = marker;
        } else if(vzrok === "Zaprta cesta"){
            var marker = L.marker([y, x], {icon: closed22})
            marker.bindPopup(opis);
            dogodki_array[dogodki_array.length] = marker;
        } else if(vzrok === "Prepoved za tovornjake"){
            var marker = L.marker([y, x], {icon: truck})
            marker.bindPopup(opis);
            dogodki_array[dogodki_array.length] = marker;
        } else if(vzrok === "Zastoj"){
            var marker = L.marker([y, x], {icon: jam})
            marker.bindPopup(opis);
            dogodki_array[dogodki_array.length] = marker;
        }
    });
    var dogodki_layer = L.layerGroup(dogodki_array);
    return dogodki_layer; 
}

function parserCarSharingPostaje(objCS){
    var carRental_array = [];
    objCS.forEach(function(stat){
        var ime = stat.name;
        var cars = parseInt(stat.numberOfCars);
        var places = parseInt(stat.parkingPlaces);
        var y = stat.geoLocation.latitude;
        var x = stat.geoLocation.longitude;
        if(places === 0 || cars === 0){
            var marker = L.marker([y, x], {icon: redCar})
            marker.bindPopup(ime + "<br> # avtomobilov: " + cars + "<br> # prostorov: " + places);
            carRental_array[carRental_array.length] = marker;
        }else if( places > 4 && cars > 4){
            var marker = L.marker([y, x], {icon: greenCar})
            marker.bindPopup(ime + "<br> # avtomobilov: " + cars + "<br> # prostorov: " + places);
            carRental_array[carRental_array.length] = marker;
        }else{
            var marker = L.marker([y, x], {icon: yellowCar})
            marker.bindPopup(ime + "<br> # avtomobilov: " + cars + "<br> # prostorov: " + places);
            carRental_array[carRental_array.length] = marker;
        }
    });
    var carRental_layer = L.layerGroup(carRental_array);
    return carRental_layer;
}

/*
        Del kode is Map.js

        this.lmap = new L.Map('map', mapProps);
               
        var resourcePath = otp.config.resourcePath || "";
        L.Icon.Default.imagePath = resourcePath + 'images/leaflet/';

        var parkirisca = preberiPodatke("http://opendata.si/promet/parkirisca/lpt/").Parkirisca;
        var bicikeLjPostaje = preberiPodatke("http://opendata.si/promet/bicikelj/list/").markers;
        var stevciPrometa = preberiPodatke("http://opendata.si/promet/counters/").feed.entry;
        var dogodkiNaCestah = preberiPodatke("http://opendata.si/promet/events/").dogodki.dogodek;
        var carSharingPostaje = preberiPodatke("https://maas-api.comtrade.com/api/locations?list=all");

        var markerjiParkirisca = parseParkirisca(parkirisca);
        var markerjiBicikeLJ = parserBicikelj(bicikeLjPostaje);
        var markerjiStevci = parseStevecPrometa(stevciPrometa);
        var markerjiDogodki = parserDogodkiNaCestah(dogodkiNaCestah);
        var markerjiCarSharing = parserCarSharingPostaje(carSharingPostaje);

        var overlaysFromAPI = {
            "Parkirišča": markerjiParkirisca,
            "BicikeLJ": markerjiBicikeLJ,
            "Avant2go": markerjiCarSharing,
            "Števci prometa": markerjiStevci,
            "Dogodki na cestah": markerjiDogodki
        }

        this.layer_control = L.control.layers(this.baseLayers, overlaysFromAPI).addTo(this.lmap);
        L.control.zoom({ position : 'topright' }).addTo(this.lmap);

*/