Parse.Cloud.job("getUSGS", function(request, response) {
  Parse.Cloud.httpRequest({
    url: 'http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson'
  }).then(function(httpResponse) {
  obj = JSON.parse(httpResponse.text);
  var result = "";
  for(i = 0; i < obj.features.length; i++){
    var mag = obj.features[i].properties.mag;
    var lat = obj.features[i].geometry.coordinates[0].toString();
    var lon = obj.features[i].geometry.coordinates[1].toString();
    if( mag > 5.5 ){
        var Danger = Parse.Object.extend("Danger");
        var danger = new Danger();
        result = result + " " + mag + " " + lon + " " + lat;
        danger.save({
          magnitude: mag.toString(),
          longitude: lon,
          lattitude: lat
        },{
            success: function(danger) {
              console.log(result = "");
          },
            error: function(danger, error) {
              console.log("Notdone" + i);
          }
        });
      }
    }
      Parse.Push.send({
        channels: [ "Giants" ],
        data: {
          alert: result,
          title: "bigblue"
        }
      }, {
        success: function() {
          // Push was successful
        },
        error: function(error) {
          // Handle error
        }
      });
    response.success(result);
  }, function(httpResponse) {
    response.success("notdone");
  });
});