
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


function parseParkirisca(ObjPa, map){

    var parkings_array = [];

    ObjPa.forEach(function(entry) {
        var ime = entry.Ime;
        var pZaInvalide = entry.Invalidi_St_mest;
        var vsehMest = entry.St_mest;
        var y = entry.KoordinataY_wgs;
        var x = entry.KoordinataX_wgs;
        var opis = entry.Opis;
        var delavnik = entry.U_delovnik;
        if(y !== null && x !== null){
            if(y !== undefined && x !== undefined){
                if(entry.zasedenost){
                // rdeča  če je zasedenost nad 90%
                // modra če je zasedenost vmes
                // zelena če je zasedenost pod 10%
                
                var marker = L.marker([y, x])
                marker.bindPopup(ime).openPopup();
                parkings_array[parkings_array.length] = marker;

                var vsehZasedenih = entry.zasedenost.P_kratkotrajniki;
                var procentZasedenost = parseInt(Math.abs(vsehZasedenih)) * 1.0 / parseInt(Math.abs(vsehMest));
                
                /*
                console.log("")
                console.log(y);
                console.log(x);
                console.log(ime)
                console.log("vseh zasedenih: "+vsehZasedenih)
                console.log("vseh mest: "+vsehMest)
                console.log("procent: "+procentZasedenost);
                console.log("");
                */

                } else {
                    // vstavi sivo
                    var marker = L.marker([y, x])
                    marker.bindPopup(ime).openPopup();
                    parkings_array[parkings_array.length] = marker;                }
            }
        }        
    });
    var parkings_layer = L.layerGroup(parkings_array);
    return parkings_layer;
}