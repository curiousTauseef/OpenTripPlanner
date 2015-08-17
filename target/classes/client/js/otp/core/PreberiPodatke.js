
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

    //var marker = L.marker([46.09192, 14.4817]).addTo(map);
    //marker.bindPopup("<b>Hello world22!</b><br>I am a popup22.").openPopup();

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
                console.log("")
                console.log(y);
                console.log(x);
                var marker = L.marker([y, x]).addTo(map);
                marker.bindPopup(ime).openPopup();
            }
        }




        if(entry.zasedenost){
            // rdeča  če je zasedenost nad 90%
            // modra če je zasedenost vmes
            // zelena če je zasedenost pod 10%

            var vsehZasedenih = entry.zasedenost.P_kratkotrajniki;
            var procentZasedenost = parseInt(Math.abs(vsehZasedenih)) * 1.0 / parseInt(Math.abs(vsehMest));

            //console.log(ime)
            //console.log("vseh zasedenih: "+vsehZasedenih)
            //console.log("vseh mest: "+vsehMest)
            //console.log("procent: "+procentZasedenost);
            //console.log("");

        } else {
            // vstavi sivo
        }
        
    });
    
    
	
}