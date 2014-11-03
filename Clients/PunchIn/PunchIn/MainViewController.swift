//
//  MainViewController.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import UIKit

extension UITableViewCell: GenericCell {
    typealias T = APIUser
    
    func configureWithItem(item: T) {
        textLabel.text = item.username
        detailTextLabel?.text = availabilityString(item)
    }
    
    func availabilityString(item: T) -> String {
        var string = item.present ? "Present" : "Not Present"
        string += ", \(item.availability)% Available"
        
        return string
    }
}

class MainViewController: UIViewController {
    @IBOutlet weak var tableView: UITableView!
	@IBAction func done(segue: UIStoryboardSegue) {}
    
    private var dataSource: ArrayDataSource!
    private let apiClient = APIClient()
	
	override func viewDidLoad() {
		super.viewDidLoad()
        
        dataSource = ArrayDataSource(items: [], cellIdentifier: "usersCell")
        tableView.dataSource = dataSource
        
        fetchData()
		
		NSNotificationCenter.defaultCenter().addObserver(self, selector: "didEnterRegion", name: didEnterWorkPlaceNotification, object: nil)
		NSNotificationCenter.defaultCenter().addObserver(self, selector: "didLeaveRegion", name: didLeaveWorkPlaceNotification, object: nil)
	}
    
    func fetchData() {
        Async.userInitiated { [weak self] in
            self?.apiClient.fetchUsers { success, users in
                self?.dataSource.items = users ?? []
                self?.tableView.reloadData()
                
                Async.userInitiated(after: 2.0) {
                    self?.fetchData()
                    return
                }
                
                return
            }
            return
        }
    }
	
	deinit {
		NSNotificationCenter.defaultCenter().removeObserver(self)
	}
	
	func didEnterRegion() {
	}
	
	func didLeaveRegion() {
	}
}
