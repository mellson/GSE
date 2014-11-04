//
//  APIUser.swift
//  PunchIn
//
//  Created by Kristian Andersen on 02/11/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import Foundation

extension Int {
    func toBool() -> Bool? {
        switch self {
        case 0: return false
        case 1: return true
        default: return nil
        }
    }
}

public class APIUser {
    let username: String
    let present: Bool
    let availability: Int
    
    public init(username: String, present: Bool, availability: Int) {
        self.username = username
        self.present = present
        self.availability = availability
    }
    
    public init(json: JSON) {
        self.username = json["UserName"].stringValue
        self.present = json["Present"].boolValue
        self.availability = json["Available"].integerValue
    }
}
