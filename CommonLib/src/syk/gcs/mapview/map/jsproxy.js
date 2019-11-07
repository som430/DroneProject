var jsproxy = {
    log: function(message) {
        if(jsproxy.java) {
            jsproxy.java.javascriptLog(message);
        } else {
            console.log(message);
        }
    },

    setMapZoom: function(zoom) {
        map.googlemap.setZoom(zoom);
    },

    setZoomSliderValue: function(zoom) {
        jsproxy.java.setZoomSliderValue(zoom);
    },

    setHomePosition: function(lat, lng) {
        if(map.uav != null) {
            map.uav.setHomePosition(lat, lng);
        }
    },

    getHomePosition: function() {
        if(map.uav != null && map.uav.homePosition != null) {
            var json = {
                "msgid":"getHomePosition",
                "lat": map.uav.homePosition.lat,
                "lng": map.uav.homePosition.lng
            };
            jsproxy.java.receiveFromMap(JSON.stringify(json));
        }
    },

    setCurrLocation: function(lat, lng, heading) {
        if (map.uav == null) {
            map.uav = new Uav();
            map.googlemap.setCenter({lat: lat, lng: lng});
            map.googlemap.setZoom(18);
            jsproxy.java.setZoomSliderValue(18);
        }
        map.uav.currLocation = {lat: lat, lng: lng};
        map.uav.heading = heading;
    },

    setMode: function(mode) {
        if(map.uav != null) {
            if (mode == 4) {
                //guidedMode
                map.uav.setMode(true, false, false, false);
            } else if (mode == 3) {
                //autoMode
                map.uav.setMode(false, true, false, false);
            } else if (mode == 6) {
                //rtlMode
                map.uav.setMode(false, false, true, false);
            } else if (mode == 9) {
                //landMode
                map.uav.setMode(false, false, false, true);
            }
        }
    },

    setMissionCurrent: function(missionCurrent) {
        if(map.uav != null) {
            map.uav.setMissionCurrent(missionCurrent);
        }
    },

    manualMake: function(manualAlt) {
        map.setMake(true, false, false, false);
        map.uav.manualAlt = manualAlt;
    },

    manualMove: function(manualLat, manualLng, manualAlt) {
        var json = {
        	"msgid":"manualMove",
        	"targetLat": manualLat,
        	"targetLng": manualLng,
        	"targetAlt": manualAlt
        };
        jsproxy.java.receiveFromMap(JSON.stringify(json));
    },

    missionMake: function() {
        map.setMake(false, true, false, false);
    },

    missionTableViewSync: function() {
        var missionItems = map.uav.getMissionItems();
        var strMissionItems = JSON.stringify(missionItems);
        jsproxy.java.missionTableViewSync(strMissionItems);
    },

    missionClear: function() {
        map.uav.missionClear();
    },

    missionMapSync: function(strMissionItems) {
        var missionItems = JSON.parse(strMissionItems);
        map.uav.missionRefresh(missionItems);
    },

    roiMake: function(seq) {
        map.setMake(false, true, true, false);
        map.roiSeq = seq;
    },

    missionMapSelectItem: function(strIndices) {
        var indices = JSON.parse(strIndices);
        map.uav.missionSelectItem(indices);
    },

    missionTableViewSelectItem: function(seq) {
        jsproxy.java.missionTableViewSelectItem(seq);
    },

    fenceMake: function() {
        this.fenceClear();
        map.setMake(false, false, false, true);
    },

    fenceClear: function () {
        map.uav.fenceClear();
    },

    getFencePoints: function() {
        var fencePoints = map.uav.getFencePoints();
        var json = {
        	"msgid":"fencePoints",
        	"points": fencePoints
        };
        jsproxy.java.receiveFromMap(JSON.stringify(json));        
    },

    fenceMapSync: function(strFencePoints) {
        var fencePoints = JSON.parse(strFencePoints);
        map.uav.fenceRefresh(fencePoints);
    },

    requestMarkerInfo: function() {
        var markerInfo = {};
        if (map.uav != null) {
            if (map.uav.manualTargetMarker != null) {
                markerInfo.manualTargetMarker = {
                    lat: map.uav.manualTargetMarker.getPosition().lat(),
                    lng: map.uav.manualTargetMarker.getPosition().lng()
                }
            }
            markerInfo.missionItems = map.uav.getMissionItems();
        }
        jsproxy.java.responseMarkerInfo(JSON.stringify(markerInfo));
    }
};
