//
//  ArrayDataSource.swift
//  PunchIn
//
//  Created by Kristian Andersen on 02/11/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import UIKit

protocol GenericCell {
    typealias ItemType
    
    func configureWithItem(item: ItemType)
}


class ArrayDataSource: NSObject, UITableViewDataSource {
    var items: [APIUser] = []
    var cellIdentifier: String
    
    init(items: [APIUser], cellIdentifier: String) {
        self.items = items
        self.cellIdentifier = cellIdentifier
        
        super.init()
    }
    
    func itemAtIndexPath(indexPath: NSIndexPath) -> APIUser? {
        return items[indexPath.row]
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(cellIdentifier, forIndexPath: indexPath) as UITableViewCell
        
        if let item = itemAtIndexPath(indexPath) {
            cell.configureWithItem(item)
        }
        
        return cell
    }
}
