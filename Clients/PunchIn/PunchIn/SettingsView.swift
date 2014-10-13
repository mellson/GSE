//
//  SettingsView.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import UIKit
import MapKit

class SettingsView: UIView {
	@IBOutlet weak var searchBar: UISearchBar!
	@IBOutlet weak var mapview: MKMapView!
	@IBOutlet weak var activityIndicator: UIActivityIndicatorView!
	
	func hideKeyboard() {
		searchBar.resignFirstResponder()
	}
	
	func startSpinner() {
		activityIndicator.startAnimating()
	}
	
	func stopSpinner() {
		activityIndicator.stopAnimating()
	}
}
