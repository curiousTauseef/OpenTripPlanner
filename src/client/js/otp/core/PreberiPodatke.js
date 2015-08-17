
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


function parseParkirisca(parkiriscaObject){
	
}