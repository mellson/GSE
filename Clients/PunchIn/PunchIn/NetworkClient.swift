//
//  NetworkClient.swift
//  PunchIn
//
//  Created by Kristian Andersen on 07/10/14.
//  Copyright (c) 2014 kristian.co. All rights reserved.
//

import Foundation

public class NetworkClient: NSObject, NSURLSessionDelegate {
	private var session: NSURLSession!
	
	private let credential: NSURLCredential
	private let protectionSpace: NSURLProtectionSpace
	private var authFailureCount = 0
	
	public required init(host: String) {
		credential = NSURLCredential(
			user: "spcl",
			password: "password",
			persistence: .ForSession)
		
		protectionSpace = NSURLProtectionSpace(
			host: host,
			port: 8080,
			`protocol`: "http",
			realm: "Private API",
			authenticationMethod: NSURLAuthenticationMethodHTTPBasic)
		
		let storage = NSURLCredentialStorage.sharedCredentialStorage()
		storage.setDefaultCredential(credential, forProtectionSpace: protectionSpace)
		
		var config = NSURLSessionConfiguration.defaultSessionConfiguration()
		config.URLCredentialStorage = storage
		
		super.init()
		
		session = NSURLSession(configuration: config, delegate: self, delegateQueue: nil)
	}
	
	public func performRequest(request: NSURLRequest, completion: (JSON?, NSError?) -> Void) {
		println("Request: \(request.URL.absoluteString)")
		
		let task = session.dataTaskWithRequest(request) { data, response, error in
			if let error = error {
				return completion(nil, error)
			}
			
			if let urlResponse = response as? NSHTTPURLResponse {
				println("\(urlResponse.statusCode) \(urlResponse.URL?.absoluteString)")
			}
			
			
			if let data = data {
				var jsonError: NSError?
				var json = JSON(data: data, options: NSJSONReadingOptions(0), error: &jsonError)
				switch json {
				case .Null(let parseError):
					return completion(nil, parseError)
				default: break
				}
				
				return completion(json, jsonError)
			}
		}
		
		task.resume()
	}
	
	func cancelAllRequests() {
		session.invalidateAndCancel()
	}
	
	// MARK: - NSURLSessionDelegate
	
	public func URLSession(session: NSURLSession, didReceiveChallenge challenge: NSURLAuthenticationChallenge, completionHandler:(NSURLSessionAuthChallengeDisposition, NSURLCredential!) -> Void) {
		if authFailureCount == 0 {
			completionHandler(.UseCredential, credential)
		} else {
			completionHandler(.CancelAuthenticationChallenge, nil)
		}
	}
}
