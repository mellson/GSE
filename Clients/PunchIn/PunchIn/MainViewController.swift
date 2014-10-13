//
//  MainViewController.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import UIKit

class MainViewController: UIViewController {
	@IBOutlet weak var statusLabel: UILabel!
	
	@IBAction func done(segue: UIStoryboardSegue) {}
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		NSNotificationCenter.defaultCenter().addObserver(self, selector: "didEnterRegion", name: didEnterWorkPlaceNotification, object: nil)
		NSNotificationCenter.defaultCenter().addObserver(self, selector: "didLeaveRegion", name: didLeaveWorkPlaceNotification, object: nil)
	}
	
	deinit {
		NSNotificationCenter.defaultCenter().removeObserver(self)
	}
	
	func didEnterRegion() {
		statusLabel.text = "YOU ARE HERE@!!!"
	}
	
	func didLeaveRegion() {
		statusLabel.text = "YOU ARE NOT HERE!!!"
	}
}
