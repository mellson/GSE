//
//  APIClient.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import Foundation

public let DidEncounterNetworkErrorNotification = "co.kristian.PunchIn.DidEncounterNetworkErrorNotification"

public class APIClient {
	private let networkClient = NetworkClient(host: "spcl.cloudapp.net")
	
	private var currentNode: NSURL?
	private let rootNode = NSURL(string: "http://spcl.cloudapp.net:8080")!
	
	public var baseURL: NSURL {
		return currentNode? ?? rootNode
	}
	
	public init() {
		
	}
	
	public func fetchClusterNodeAdress(completion: (Bool, String?) -> Void) {
		let path = "private"
		let request = APIRequest.GET(path)
		
		let handler: (Bool, String?) -> Void = { [weak self] success, address in
			if let address = address {
				if let welf = self {
					welf.currentNode = NSURL(string: address)
				}
			}
			
			completion(success, address)
		}
		
		performRequest(
			request,
			parsingBlock: { [unowned self] json in
				return json["address"].string ?? self.rootNode.absoluteString!
			},
			completion: handler)
	}
	
	private func performRequest<T>(request: APIRequest, parsingBlock: (JSON) -> T, completion: (Bool, T?) -> Void) {
		
		networkClient.performRequest(request.URLRequest(baseURL: baseURL)) { [weak self] json, error in
			
			let success = error == nil
			var object: T?
			
			Async.userInitiated {
				if let json = json {
					object = parsingBlock(json)
				} else {
					if let let welf = self {
						welf.reportError(error)
					}
				}
			}.main {
				completion(success, object)
			}
		}
	}
	
	private func escapePath(path: String) -> String {
		let set = NSCharacterSet.URLHostAllowedCharacterSet()
		return path.stringByAddingPercentEncodingWithAllowedCharacters(set)!
	}
	
	private func reportError(error: NSError?) {
		NSNotificationCenter.defaultCenter().postNotificationName(DidEncounterNetworkErrorNotification, object: error)
	}
}
