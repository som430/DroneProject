var Uav = function() {
    this.homePosition = null;
    this.homeMarker = null;
    this.setHomePosition = function(lat, lng) {
        if(this.homePosition == null ||
            this.homePosition.lat != lat ||
            this.homePosition.lng != lng) {
            this.homePosition = {lat: lat, lng: lng};
            if (this.homeMarker != null) {
                this.homeMarker.setMap(null);
            }
            this.homeMarker = new google.maps.Marker({
                map: map.googlemap,
                position: this.homePosition,
                optimized: false,
                label: {text:"H", color:"#ffffff"}
            });
            this.homeMarker.missionItem = {
                seq: 0,
                command: 16,
                param1: 0,
                param2: 0,
                param3: 0,
                param4: 0,
                x: lat,
                y: lng,
                z: 0
            };
            this.missionMarkers.splice(0, 1, this.homeMarker);
            this.fenceMarkers.splice(0, 1, this.homeMarker);
            this.drawMissionPath();
            jsproxy.missionTableViewSync();
        }
    };

    this.currLocation = null;
    this.heading = 0;

    this.guidedMode = false;
    this.autoMode = false;
    this.rtlMode = false;
    this.landMode = false;
    this.setMode = function(guidedMode, autoMode, rtlMode, landMode) {
        this.guidedMode = guidedMode;
        this.autoMode = autoMode;
        this.rtlMode = rtlMode;
        this.landMode = landMode;
        if(guidedMode == false) {
            if(this.manualTargetMarker != null) {
                this.manualTargetMarker.setMap(null);
            }
            this.manualTargetMarker = null;
        }
    };

    this.uavFrame = new google.maps.Marker({
        map: map.googlemap,
        optimized: false,
        zIndex: google.maps.Marker.MAX_ZINDEX + 1
    });
    this.uavFrameIcon = {
        path: "M-30.14012510194162,-0.6472990536338017 C-30.14012510194162,-4.36100347511945 -26.9396213477545,-7.369104056522829 -22.988382145054345,-7.369104056522829 C-19.037142942354194,-7.369104056522829 -15.836639188167071,-4.36100347511945 -15.836639188167071,-0.6472990536338017 C-15.836639188167071,3.06640536785185 -19.037142942354194,6.074505949255226 -22.988382145054345,6.074505949255226 C-26.9396213477545,6.074505949255226 -30.14012510194162,3.06640536785185 -30.14012510194162,-0.6472990536338017 z M-6.711553535002267,24.0669865286702 C-6.711553535002267,20.353282107184548 -3.511049780815142,17.34518152578117 0.44018942188500887,17.34518152578117 C4.39142862458516,17.34518152578117 7.5919323787722846,20.353282107184548 7.5919323787722846,24.0669865286702 C7.5919323787722846,27.78069095015585 4.39142862458516,30.788791531559227 0.44018942188500887,30.788791531559227 C-3.511049780815142,30.788791531559227 -6.711553535002267,27.78069095015585 -6.711553535002267,24.0669865286702 z M17.145587020975555,0.20984434846580768 C17.145587020975555,-3.503860073019837 20.346090775162672,-6.51196065442322 24.29732997786283,-6.51196065442322 C28.24856918056298,-6.51196065442322 31.449072934750106,-3.503860073019837 31.449072934750106,0.20984434846580768 C31.449072934750106,3.923548769951452 28.24856918056298,6.931649351354835 24.29732997786283,6.931649351354835 C20.346090775162672,6.931649351354835 17.145587020975555,3.923548769951452 17.145587020975555,0.20984434846580768 z M-7.140127858656442,-24.79015735026657 C-7.140127858656442,-28.50386177175222 -3.939624104469317,-31.511962353155596 0.011615098230834064,-31.511962353155596 C3.962854300930985,-31.511962353155596 7.16335805511811,-28.50386177175222 7.16335805511811,-24.79015735026657 C7.16335805511811,-21.076452928780917 3.962854300930985,-18.06835234737754 0.011615098230834064,-18.06835234737754 C-3.939624104469317,-18.06835234737754 -7.140127858656442,-21.076452928780917 -7.140127858656442,-24.79015735026657 z M-15.496242274669473,0.06630312002640437 L16.634312041951254,0.06630312002640437 M0.28571338525838996,-17.78571321283068 L0.42857052811552876,17.5000010728836 M-25.14285719394684,-0.5714285969734192 C-25.14285719394684,-1.676400972664027 -24.247829569637446,-2.571428596973419 -23.14285719394684,-2.571428596973419 C-22.03788481825623,-2.571428596973419 -21.14285719394684,-1.676400972664027 -21.14285719394684,-0.5714285969734192 C-21.14285719394684,0.5335437787171886 -22.03788481825623,1.4285714030265808 -23.14285719394684,1.4285714030265808 C-24.247829569637446,1.4285714030265808 -25.14285719394684,0.5335437787171886 -25.14285719394684,-0.5714285969734192 z M-1.9999997168779375,-24.85714226961136 C-1.9999997168779375,-25.962114645301966 -1.104972092568545,-26.85714226961136 2.8312206271086104e-7,-26.85714226961136 C1.1049726588126703,-26.85714226961136 2.0000002831220627,-25.962114645301966 2.0000002831220627,-24.85714226961136 C2.0000002831220627,-23.75216989392075 1.1049726588126703,-22.85714226961136 2.8312206271086104e-7,-22.85714226961136 C-1.104972092568545,-22.85714226961136 -1.9999997168779375,-23.75216989392075 -1.9999997168779375,-24.85714226961136 z M22.42857150733471,0.2857148051261902 C22.42857150733471,-0.8192575705644174 23.3235991316441,-1.7142851948738098 24.42857150733471,-1.7142851948738098 C25.533543883025317,-1.7142851948738098 26.42857150733471,-0.8192575705644174 26.42857150733471,0.2857148051261902 C26.42857150733471,1.3906871808167978 25.533543883025317,2.28571480512619 24.42857150733471,2.28571480512619 C23.3235991316441,2.28571480512619 22.42857150733471,1.3906871808167978 22.42857150733471,0.2857148051261902 z M-1.5714304745197296,24.142857044935226 C-1.5714304745197296,23.03788466924462 -0.6764028502103372,22.142857044935226 0.4285695254802704,22.142857044935226 C1.5335419011708782,22.142857044935226 2.4285695254802704,23.03788466924462 2.4285695254802704,24.142857044935226 C2.4285695254802704,25.247829420625834 1.5335419011708782,26.142857044935226 0.4285695254802704,26.142857044935226 C-0.6764028502103372,26.142857044935226 -1.5714304745197296,25.247829420625834 -1.5714304745197296,24.142857044935226 z",
        strokeWeight: 3,
        strokeColor: "#ffff00"
    };

    this.uavFrameHead = new google.maps.Marker({
        map: map.googlemap,
        optimized: false,
        zIndex: google.maps.Marker.MAX_ZINDEX + 1
    });
    this.uavFrameHeadIcon = {
        path: "M-25.14285719394684,-0.5714285969734192 C-25.14285719394684,-1.676400972664027 -24.247829569637446,-2.571428596973419 -23.14285719394684,-2.571428596973419 C-22.03788481825623,-2.571428596973419 -21.14285719394684,-1.676400972664027 -21.14285719394684,-0.5714285969734192 C-21.14285719394684,0.5335437787171886 -22.03788481825623,1.4285714030265808 -23.14285719394684,1.4285714030265808 C-24.247829569637446,1.4285714030265808 -25.14285719394684,0.5335437787171886 -25.14285719394684,-0.5714285969734192 z M-1.9999997168779375,-24.85714226961136 C-1.9999997168779375,-25.962114645301966 -1.104972092568545,-26.85714226961136 2.8312206271086104e-7,-26.85714226961136 C1.1049726588126703,-26.85714226961136 2.0000002831220627,-25.962114645301966 2.0000002831220627,-24.85714226961136 C2.0000002831220627,-23.75216989392075 1.1049726588126703,-22.85714226961136 2.8312206271086104e-7,-22.85714226961136 C-1.104972092568545,-22.85714226961136 -1.9999997168779375,-23.75216989392075 -1.9999997168779375,-24.85714226961136 z",
        strokeWeight: 3,
        strokeColor: "#ff0000"
    };

    this.drawUav = function() {
        this.uavFrameIcon.rotation = 45 + this.heading;
        this.uavFrame.setIcon(this.uavFrameIcon);
        this.uavFrame.setPosition(this.currLocation);
        this.uavFrameHeadIcon.rotation = 45 + this.heading;
        this.uavFrameHead.setIcon(this.uavFrameHeadIcon);
        this.uavFrameHead.setPosition(this.currLocation);
        this.drawHeadingLine();
        this.drawDestinationLine();
    };

    this.headingLine = null;
    this.drawHeadingLine = function() {
        if (this.headingLine != null) {
            this.headingLine.setMap(null);
        }
        var startPoint = new google.maps.LatLng(this.currLocation);
        var endPoint = google.maps.geometry.spherical.computeOffset(
            startPoint, 500000, this.heading
        );
        this.headingLine = new google.maps.Polyline({
            path: [startPoint, endPoint],
            strokeColor: "#ff0000",
            strokeWeight: 2,
            map: map.googlemap
        });
    };

    this.destinationLocation = null;
    this.destinationLine = null;
    this.drawDestinationLine = function() {
        if (this.destinationLine != null) {
            this.destinationLine.setMap(null);
        }
        this.destinationLocation = this.currLocation;
        if (this.guidedMode == true) {
            if(this.manualTargetMarker != null) {
                this.destinationLocation = this.manualTargetMarker.getPosition().toJSON();
            }
        } else if (this.autoMode == true) {
            if(this.missionCurrentMarker != null) {
                this.destinationLocation = this.missionCurrentMarker.getPosition().toJSON();
            }
        } else if (this.rtlMode == true) {
            this.destinationLocation = this.homePosition;
        }
        var angle = google.maps.geometry.spherical.computeHeading(
            new google.maps.LatLng(this.currLocation),
            new google.maps.LatLng(this.destinationLocation)
        );
        if (angle < 0) {
            angle += 360;
        }
        var startPoint = new google.maps.LatLng(this.currLocation);
        var endPoint = google.maps.geometry.spherical.computeOffset(
            startPoint, 500000, angle
        );
        this.destinationLine = new google.maps.Polyline({
            path: [startPoint, endPoint],
            strokeColor: "#ff9900",
            strokeWeight: 2,
            map: map.googlemap
        });
    };

    this.manualAlt = 0;
    this.manualTargetMarker = null;
    this.manualMove = function(manualLat, manualLng) {
        this.setMode(true, false, false, false);
        if (this.manualTargetMarker != null) {
            this.manualTargetMarker.setPosition({lat: manualLat, lng: manualLng});
            this.manualTargetMarker.setMap(map.googlemap);
        } else {
            this.manualTargetMarker = new google.maps.Marker({
                map: map.googlemap,
                position: {lat: manualLat, lng: manualLng},
                optimized: false
            });
        }
        jsproxy.manualMove(manualLat, manualLng, this.manualAlt);
    };

    this.missionMarkers = [];
    this.missionMarkerMake = function(missionItem) {
        var marker = new google.maps.Marker({
            map: map.googlemap,
            position: {lat: missionItem.x, lng: missionItem.y},
            optimized: false,
            zIndex: google.maps.Marker.MAX_ZINDEX + 2
        });
        if(missionItem.command != 201) {
            marker.setLabel({
                color: "#000000",
                fontSize: "12px",
                fontWeight: "600",
                text: String(missionItem.seq)
            });
            marker.setIcon({
                path: google.maps.SymbolPath.CIRCLE,
                fillOpacity: 1,
                fillColor: "#ffffff",
                strokeColor: "#00ffff",
                strokeWeight: 1,
                scale: 12
            });
        } else {
            marker.setLabel({text:"R", color:"#ffff00"});
        }
        marker.missionItem = missionItem;
        marker.setDraggable(true);
        var outter = this;
        marker.addListener("drag", function() {
            marker.missionItem.x = marker.getPosition().lat();
            marker.missionItem.y = marker.getPosition().lng();
            outter.drawMissionPath();
            jsproxy.missionTableViewSync();
        });
        marker.addListener("click", function() {
            jsproxy.missionTableViewSelectItem(marker.missionItem.seq);
        });
        this.missionMarkers.push(marker);
        this.drawMissionPath();
        jsproxy.missionTableViewSync();
    };

    this.missionPolylines = [];
    this.drawMissionPath = function() {
        for(var i=0; i<this.missionPolylines.length; i++) {
            this.missionPolylines[i].setMap(null);
        }
        this.missionPolylines = [];
        //var startMarker = this.uavFrame;
        var startMarker = this.homeMarker;
        var endMarker = null;
        for(var i=1; i<this.missionMarkers.length; i++) {
            endMarker = this.missionMarkers[i];
            if(endMarker.missionItem.command == 21 ||
                endMarker.missionItem.command == 22 ||
                endMarker.missionItem.command == 177 ||
                endMarker.missionItem.command == 201) {
                continue;
            }
            var polyline = new google.maps.Polyline({
                map: map.googlemap,
                strokeColor: "#1ea4ff",
                strokeWeight: 3,
                strokeOpacity: 1.0
            });
            polyline.setPath([startMarker.getPosition(), endMarker.getPosition()]);
            this.missionPolylines.push(polyline);
            startMarker = endMarker;
        }
    };

    this.getMissionItems = function() {
        var missionMarkers = map.uav.missionMarkers;
        var missionItems = [];
        for(var i=0; i<missionMarkers.length; i++) {
            var missionItem = missionMarkers[i].missionItem;
            missionItems.push(missionItem);
        }
        return missionItems;
    };

    this.missionClear = function() {
        map.setMake(false, false, false, false);

        for(var i=1; i<map.uav.missionMarkers.length; i++) {
            map.uav.missionMarkers[i].setMap(null);
        }
        map.uav.missionMarkers = [map.uav.homeMarker];

        for(var i=0; i<map.uav.missionPolylines.length; i++) {
            map.uav.missionPolylines[i].setMap(null);
        }
        map.uav.missionPolylines = [];
    };

    this.missionRefresh = function(missionItems) {
        for (var i = 1; i < map.uav.missionMarkers.length; i++) {
            map.uav.missionMarkers[i].setMap(null);
        }
        map.uav.missionMarkers = [map.uav.homeMarker];

        for (var i = 1; i < missionItems.length; i++) {
            map.uav.missionMarkerMake(missionItems[i]);
        }
    };

    this.missionSelectItem = function(indices) {
        for(var i=1; i<map.uav.missionMarkers.length; i++) {
            if(map.uav.missionMarkers[i].missionItem.command == 201) {
                //ROI인 경우
                continue;
            }
            map.uav.missionMarkers[i].setIcon({
                path: google.maps.SymbolPath.CIRCLE,
                fillOpacity: 1,
                fillColor: "#ffffff",
                strokeColor: "#00ffff",
                strokeWeight: 1,
                scale: 12
            });
        }
        //선택된 것만 노란색으로 변경
        for(var i=0; i<indices.length; i++) {
            var seq = indices[i];
            if(seq != 0) {
                if(map.uav.missionMarkers[seq].missionItem.command == 201) {
                    continue;
                }
                map.uav.missionMarkers[seq].setIcon({
                    path: google.maps.SymbolPath.CIRCLE,
                    fillOpacity: 1,
                    fillColor: "#ffff00",
                    strokeColor: "#00ffff",
                    strokeWeight: 1,
                    scale: 12
                });
            }
        }
    };

    this.missionCurrentMarker = null;
    this.setMissionCurrent = function(missionCurrent) {
        this.missionCurrentMarker = this.missionMarkers[missionCurrent];
    };

    this.fenceClear = function() {
        map.setMake(false, false, false, false);
        if(map.uav.fencePolygon != null) {
            map.uav.fencePolygon.setMap(null);
        }
        for(var i=1; i<map.uav.fenceMarkers.length; i++) {
            map.uav.fenceMarkers[i].setMap(null);
        }
        map.uav.fenceMarkers.splice(1, map.uav.fenceMarkers.length-1);
    };

    this.fenceMarkers = [];
    this.fenceMarkerMake = function(lat, lng) {
        var marker = new google.maps.Marker({
            map: map.googlemap,
            position: {lat: lat, lng: lng},
            /*icon: {
                url: 'beachflag.png',
                size: new google.maps.Size(20, 32),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(0, 32)
            },*/
            icon: {
                path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
                fillOpacity: 1,
                fillColor: "#ff0000",
                strokeColor: "#ffff00",
                strokeWeight: 2,
                scale: 5
            },
            draggable: true,
            optimized: false
        });

        if(this.fenceMarkers.length < 3) {
            //현재 펜스 마크가 2개(index:0,1)일 경우 추가 위치
            this.fenceMarkers.push(marker);
        } else if(this.fenceMarkers.length == 3) {
            //현재 펜스 마크가 3개(index:0,1,2)일 경우 추가 위치
            this.fenceMarkers.push(marker);
            //다각형을 만들기 위해서 index1 펜스 마크를 끝에 한번 더 추가
            this.fenceMarkers.push(this.fenceMarkers[1]);
        } else {
            //현재 펜스 마크가 4개 이상일 경우 추가 위치
            //뒤에서 두번째로 추가
            this.fenceMarkers.splice(this.fenceMarkers.length-1, 0, marker);
        }
        var self = this;
        marker.addListener("drag", function() {
            self.drawFencePolygon();
        });
        this.drawFencePolygon();
    };

    this.fencePolygon = null;
    this.drawFencePolygon = function() {
        if(this.fencePolygon != null) {
            this.fencePolygon.setMap(null);
        }
        var fenceVertex = [];
        for(var i=1; i<this.fenceMarkers.length; i++) {
            fenceVertex.push(this.fenceMarkers[i].getPosition());
        }
        this.fencePolygon = new google.maps.Polygon({
            map: map.googlemap,
            paths: fenceVertex,
            strokeColor: "#FF0000",
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillOpacity: 0.15
        });
        this.fencePolygon.addListener("click", function(ev) {
            if (map.manualMake == true) {
                map.uav.manualMove(ev.latLng.lat(), ev.latLng.lng());
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
            }
        });
    };

    this.getFencePoints = function() {
        var fencePoints = [];
        for(var i=0; i<map.uav.fenceMarkers.length; i++) {
            var fenceMarker = map.uav.fenceMarkers[i];
            var fencePoint = {
                idx:i,
                lat:fenceMarker.getPosition().lat(),
                lng:fenceMarker.getPosition().lng()
            };
            fencePoints.push(fencePoint);
        }
        return fencePoints;
    };

    this.fenceRefresh = function(fencePoints) {
        if(map.uav.fenceMarkers.length > 1) {
            for(var i=1; i<map.uav.fenceMarkers.length; i++) {
                map.uav.fenceMarkers[i].setMap(null);
            }
            map.uav.fenceMarkers.splice(1, map.uav.fenceMarkers.length-1);
        }
        for(var i=1; i<fencePoints.length; i++) {
            map.uav.fenceMarkerMake(fencePoints[i].lat, fencePoints[i].lng);
        }
    };
};