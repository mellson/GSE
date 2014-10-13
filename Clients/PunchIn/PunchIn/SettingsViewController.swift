//
//  SettingsViewController.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import UIKit
import MapKit
import CoreLocation

class SettingsViewController: UIViewController, MKMapViewDelegate, UISearchBarDelegate {
	lazy private var geoCoder: CLGeocoder = {
		CLGeocoder()
	}()
	
	private var settingsView: SettingsView {
		return view as SettingsView
	}
	
	private var selectedCoordinate: CLLocationCoordinate2D?
	
	// MARK: - SettingsViewController (Private)
	
	private func searchForLocation(location: String) {
		if countElements(location) < 1 {
			return
		}
		
		settingsView.hideKeyboard()
		settingsView.startSpinner()
		
		geoCoder.geocodeAddressString(location) { [weak self] placemarks, error in
			if let error = error {
				println("Some error while geocoding")
			}
			
			if let welf = self {
				if let firstPlace = placemarks?.first as? CLPlacemark {
					let location = firstPlace.location
					welf.selectedCoordinate = location.coordinate
					
					let zoomedRegion = MKCoordinateRegionMakeWithDistance(
						location.coordinate,
						2000,
						2000)
					
					let region = MKCoordinateRegion(
						center: location.coordinate,
						span: zoomedRegion.span)
					
					let annotation = MKPointAnnotation()
					annotation.coordinate = firstPlace.location.coordinate
					annotation.title = firstPlace.thoroughfare
					annotation.subtitle = firstPlace.locality
					
					welf.settingsView.mapview.addAnnotation(annotation)
					welf.settingsView.mapview.setRegion(region, animated: true)
				}
				
				welf.settingsView.stopSpinner()
			}
		}
	}
	
	@IBAction func done(sender: AnyObject!) {
		if let coordinate = selectedCoordinate {
			LocationController.sharedController.setCoordinateForMonitoring(coordinate)
		}
		
		dismissViewControllerAnimated(true, completion: nil)
	}
	
	// MARK: - MKMapViewDelegate
	
	// MARK: - UISearchBarDelegate
	
	func searchBarResultsListButtonClicked(searchBar: UISearchBar) {
		searchForLocation(searchBar.text)
	}
	
	func searchBarSearchButtonClicked(searchBar: UISearchBar) {
		searchForLocation(searchBar.text)
	}
}
