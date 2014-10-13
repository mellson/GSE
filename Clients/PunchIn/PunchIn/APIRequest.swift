//
//  APIRequest.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import Foundation

public class APIRequest {
	public enum Method: String {
		case GET = "GET"
		case POST = "POST"
		case PUT = "PUT"
		case PATCH = "PATCH"
		case DELETE = "DELETE"
	}
	
	public private(set) var path: String
	public private(set) var method: Method
	private var headers = Dictionary<String, String>()
	private var body = Dictionary<String, AnyObject>()
	
	public class func DELETE(path: String) -> APIRequest {
		return APIRequest(.DELETE, path: path)
	}
	
	// MARK: APIRequest (Public)
	
	public init(_ method: Method, path: String) {
		let qoramp = (path as NSString).containsString("?") ? "&" : "?"
		let endpoint = "\(path)"
		
		self.path = endpoint
		self.method = method
	}
	
	public class func GET(path: String) -> APIRequest {
		return APIRequest(.GET, path: path)
	}
	
	public class func POST(path: String) -> APIRequest {
		return APIRequest(.POST, path: path)
	}
	
	public class func PUT(path: String) -> APIRequest {
		return APIRequest(.PUT, path: path)
	}
	
	public class func PATCH(path: String) -> APIRequest {
		return APIRequest(.PATCH, path: path)
	}
	
	public func URLRequest(#baseURL: NSURL) -> NSURLRequest {
		let url = NSURL(string: path, relativeToURL: baseURL)
		var request = NSMutableURLRequest(URL: url!)
		
		request.HTTPMethod = method.rawValue
		request.timeoutInterval = 30
		
		setHeaderValue(value: getLocaleLanguage(), forKey: "Accept-Language")
		setHeaderValue(value: getApplicationAgent(), forKey: "User-Agent")
		
		
		if !body.isEmpty {
			var jsonError: NSError?
			let data = NSJSONSerialization.dataWithJSONObject(body, options: NSJSONWritingOptions(0), error: &jsonError)
			
			if let error = jsonError {
				println("TMDBRequest: Got error while serializing JSON body data. \(error). \(error.userInfo)")
			}
			
			request.HTTPBody = data
			setHeaderValue(value: "application/json", forKey: "Content-Type")
		}
		
		for (key, value) in headers {
			request.setValue(value, forHTTPHeaderField: key)
		}
		
		return request
	}
	
	public func setHeaderValue(#value: String, forKey key: String) {
		headers[key] = value
	}
	
	public func setBodyValue(#value: AnyObject, forKey key: String) {
		body[key] = value
	}
	
	// MARK: APIRequest (Private)
	
	private func getLocaleLanguage() -> String {
		let defaultValue = "en-US"
		
		let defaults = NSUserDefaults.standardUserDefaults()
		if let languages = defaults.arrayForKey("AppleLanguages") as? [String] {
			return languages.first ?? defaultValue
		}
		
		return defaultValue
	}
	
	private func getApplicationAgent() -> String {
		if let userInfo = NSBundle.mainBundle().infoDictionary {
			let name = userInfo["CFBundleName"] as? String ?? ""
			let version = userInfo["CFBundleShortVersionString"] as? String ?? ""
			let build = userInfo["CFBundleVersion"] as? String ?? ""
			var specific = version
			
			if build != version {
				specific = "\(version) (\(build))"
			}
			
			return "\(name) \(specific)"
		}
		
		return ""
	}
}
