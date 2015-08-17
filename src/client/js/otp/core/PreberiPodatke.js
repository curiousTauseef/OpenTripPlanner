
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

var blueParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkingBlue.png',
    iconSize:     [25, 25],
});

var greenParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkingGreen.png',
    iconSize:     [25, 25],
});

var redParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkingRed.png',
    iconSize:     [25, 25],
});

var greyParking = L.icon({
    iconUrl: resourcePath + 'images/parking/parkingGrey.png',
    iconSize:     [25, 25],
});

var blueBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bicikeljBlue.png',
    iconSize:     [25, 25],
});

var greenBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bicikeljGreen.png',
    iconSize:     [25, 25],
});

var redBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bicikeljRed.png',
    iconSize:     [25, 25],
});

var greyBicikelj = L.icon({
    iconUrl: resourcePath + 'images/bicikelj/bicikeljGrey.png',
    iconSize:     [25, 25],
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
                    var marker = L.marker([y, x], {icon: blueParking});               
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

    jQuery.each(objBLJ, function(i, tren) {
            var ime = tren.name;
            var y = tren.lat;
            var x = tren.lng;
            var available = parseInt(tren.station.available);
            var total = parseInt(tren.station.total);
            var ticket = parseInt(tren.station.ticket);
            var free = parseInt(tren.station.free);
            if(y !== null && x !== null){
                if(y !== undefined && x !== undefined){
                    if(available > 5 && free > 5){
                        var marker = L.marker([y, x], {icon: greenBicikelj})
                        marker.bindPopup(ime + "<br> # vseh mest: " + total + "<br> # prosta kolesa: " + available + "<br> # prosta mesta: " + free + "<br> # kolesa v okvari: " + ticket);
                        bicikeLJ_array[bicikeLJ_array.length] = marker;
                    } else if (available > 0 || free > 0){
                        var marker = L.marker([y, x], {icon: blueBicikelj})
                        marker.bindPopup(ime + "<br> # vseh mest: " + total + "<br> # prosta kolesa: " + available + "<br> # prosta mesta: " + free + "<br> # kolesa v okvari: " + ticket);
                        bicikeLJ_array[bicikeLJ_array.length] = marker;
                    } else if ( available === 0 || free === 0){
                        var marker = L.marker([y, x], {icon: redBicikelj})
                        marker.bindPopup(ime + "<br> # vseh mest: " + total + "<br> # prosta kolesa: " + available + "<br> # prosta mesta: " + free + "<br> # kolesa v okvari: " + ticket);
                        bicikeLJ_array[bicikeLJ_array.length] = marker;
                    } else {
                        var marker = L.marker([y, x], {icon: greyBicikelj})
                        marker.bindPopup(ime + "<br> # vseh mest: " + total + "<br> # prosta kolesa: " + available + "<br> # prosta mesta: " + free + "<br> # kolesa v okvari: " + ticket);
                        bicikeLJ_array[bicikeLJ_array.length] = marker;
                    }
                }
            }
    });


    var bicikeLJ_layer = L.layerGroup(bicikeLJ_array);
    return bicikeLJ_layer; 
}