var map = {
    googlemap: null,
    init: function() {
        map.googlemap = new google.maps.Map(
            document.getElementById('map'),
            {
                center: {lat: 35.788870, lng: -117.266334},
                zoom: 3,
                mapTypeControl: false,
                mapTypeId: "satellite",
                streetViewControl: false,
                zoomControl: false,
                rotateControl: false,
                fullscreenControl: false
            }
        );

        google.maps.event.addListenerOnce(
            map.googlemap,
            "idle", function () {
                map.uavDraw.start();
            }
        );

        document.getElementById("map").addEventListener(
            "wheel", function() {
                var zoom = map.googlemap.getZoom();
                if(zoom < 3) {
                    zoom = 3;
                    map.googlemap.setZoom(zoom);
                }
                jsproxy.setZoomSliderValue(zoom);
            }
        );

        google.maps.event.addListener(map.googlemap, 'click', function(ev) {
            if (map.manualMake == true) {
                map.uav.manualMove(ev.latLng.lat(), ev.latLng.lng())
            } else if (map.missionMake == true) {
                if(map.roiMake == false) {
                    var missionItem = {
                        seq: map.uav.missionMarkers.length,
                        command: 16,
                        param1: 0,
                        param2: 0,
                        param3: 0,
                        param4: 0,
                        x: ev.latLng.lat(),
                        y: ev.latLng.lng(),
                        z: 10
                    };
                    map.uav.missionMarkerMake(missionItem);
                } else {
                    for(var i=0; i<map.uav.missionMarkers.length; i++) {
                        var missionMarker = map.uav.missionMarkers[i];
                        if(missionMarker.missionItem.command == 201 && missionMarker.missionItem.seq == map.roiSeq) {
                            missionMarker.missionItem.x = ev.latLng.lat();
                            missionMarker.missionItem.y = ev.latLng.lng();
                            missionMarker.setPosition({lat:ev.latLng.lat(), lng:ev.latLng.lng()});
                            map.roiMake = false;
                            jsproxy.missionTableViewSync();
                        }
                    }
                }
            } else if (map.fenceMake == true) {
                map.uav.fenceMarkerMake(ev.latLng.lat(), ev.latLng.lng());
            }
        });
    },

    uav: null,
    uavDraw: {
        count: 1,
        start: function () {
            setInterval(
                function() {
                    if(map.uav != null) {
                        map.uav.drawUav();
                        if(map.uavDraw.count == 3) {
                            map.googlemap.panTo(map.uav.currLocation);
                            map.uavDraw.count = 1;
                        } else {
                            ++map.uavDraw.count;
                        }
                    }
                },
                1000
            );
        }
    },

    manualMake: false,
    missionMake: false,
    roiMake: false,
    roiSeq: 0,
    fenceMake: false,
    setMake: function(manualMake, missionMake, roiMake, fenceMake) {
        this.manualMake = manualMake;
        this.missionMake = missionMake;
        if(missionMake==true && map.uav.missionMarkers.length == 0) {
            var missionItem = {
                seq: 0,
                command: 16, //MAV_CMD_NAV_WAYPOINT
                param1: 0,
                param2: 0,
                param3: 0,
                param4: 0,
                x: map.uav.homeMarker.getPosition().lat(),
                y: map.uav.homeMarker.getPosition().lng(),
                z: 0
            };
            map.uav.homeMarker.missionItem = missionItem;
            map.uav.missionMarkers = [map.uav.homeMarker];
        }
        this.roiMake = roiMake;
        this.fenceMake = fenceMake;
    }
};