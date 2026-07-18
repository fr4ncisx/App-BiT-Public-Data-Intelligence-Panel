package com.appbit.geoanalytics.application.maps.out;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;

import java.util.List;

public interface RegionMapPort {

    List<RegionsMapResponse.RegionMapDTO> findAll();
}
