//
//  LocationController.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import Foundation
import CoreLocation

public let didEnterWorkPlaceNotification = "co.kristian.PunchIn.didEnterWorkPlaceNotification"

public let didLeaveWorkPlaceNotification = "co.kristian.PunchIn.didLeaveWorkPlaceNotification"

private let locationCoordinateLatDefaultKey = "co.kristian.PunchIn.locationCoordinateLatDefaultKey"
private let locationCoordinateLonDefaultKey = "co.kristian.PunchIn.locationCoordinateLonDefaultKey"

class LocationController: NSObject, CLLocationManagerDelegate {
	private var locationManager: CLLocationManager
	private var didTryToGetLocationAuth = false
	
	private var isMonitoring = false
	private var monitoredRegion: CLRegion?
	
	class var sharedController: LocationController {
		struct Static {
			static let instance: LocationController = LocationController()
		}
		
		return Static.instance
	}
	
	override init() {
		locationManager = CLLocationManager()
		
		super.init()
		
		locationManager.delegate = self
	}
	
	func setCoordinateForMonitoring(coordinate: CLLocationCoordinate2D) {
		let defaults = NSUserDefaults.standardUserDefaults()
		
		if isMonitoring {
			stopMonitoring()
		}
		
		defaults.setDouble(coordinate.latitude, forKey: locationCoordinateLatDefaultKey)
		defaults.setDouble(coordinate.longitude, forKey: locationCoordinateLonDefaultKey)
		defaults.synchronize()
		
		resumeMonitoring()
	}
	
	func stopMonitoring() {
		if let region = monitoredRegion {
			locationManager.stopMonitoringForRegion(region)
		}
	}
	
	func resumeMonitoring() {
		
		attemptLocationAuthorization()
	}
	
	private func attemptLocationAuthorization() {
		switch CLLocationManager.authorizationStatus() {
		case .AuthorizedWhenInUse:
			fallthrough
		case .Authorized:
			startLocationUpdates()
		case .Denied:
			fallthrough
		case .Restricted:
			// did not authorize
			println("fuck")
		case .NotDetermined:
			if !CLLocationManager.locationServicesEnabled() && didTryToGetLocationAuth {
				// did not authorize
			} else {
				didTryToGetLocationAuth = true
				locationManager.requestAlwaysAuthorization()
			}
		}
	}
	
	private func startLocationUpdates() {
//		let defaults = NSUserDefaults.standardUserDefaults()
//		
//		let latitude = defaults.doubleForKey(locationCoordinateLatDefaultKey)
//		let longitude = defaults.doubleForKey(locationCoordinateLonDefaultKey)
//		
//		let coordinate = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
//		var region = CLCircularRegion(center: coordinate, radius: 200, identifier: "monitored_work_place")
//		
//		monitoredRegion = region
		
//		locationManager.startMonitoringForRegion(region)
        let uuid = NSUUID(UUIDString: "f7826da6-4fa2-4e98-8024-bc5b71e0893e")
        let region = CLBeaconRegion(proximityUUID: uuid, identifier: "robocat.office")
        
        locationManager.startRangingBeaconsInRegion(region)
	}
	
	// MARK: - CLLocationManagerDelegate
	
	func locationManager(manager: CLLocationManager!, didChangeAuthorizationStatus status: CLAuthorizationStatus) {
		attemptLocationAuthorization()
	}
    
    func locationManager(manager: CLLocationManager!, didDetermineState state: CLRegionState, forRegion region: CLRegion!) {
        switch region.identifier {
        case "robocat.office":
            if state == .Inside {
                didEnterRegion()
            } else if state == .Outside {
                didLeaveRegion()
            }
        default: break
        }
    }

	private func didEnterRegion() {
		NSNotificationCenter.defaultCenter().postNotificationName(didEnterWorkPlaceNotification, object: nil)
	}
	
	private func didLeaveRegion() {
		NSNotificationCenter.defaultCenter().postNotificationName(didLeaveWorkPlaceNotification, object: nil)
	}
}
