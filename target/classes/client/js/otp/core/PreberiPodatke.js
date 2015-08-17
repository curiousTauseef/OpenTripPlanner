
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

    var marker = L.marker([46.09192, 14.4817]).addTo(map);
    marker.bindPopup("<b>Hello world22!</b><br>I am a popup22.").openPopup();

    ObjPa.forEach(function(entry) {
        var ime = entry.Ime;
        var pZaInvalide = entry.Invalidi_St_mest;
        var vsehMest = entry.St_mest;
        var vsehZasedenih = entry.P_kratkotrajniki;
        var y = entry.KoordinataY_wgs;
        var x = entry.KoordinataX_wgs;
        var opis = entry.Opis;
        console.log(opis);
    });
    
    
	
}