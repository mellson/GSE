using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Text.RegularExpressions;

namespace ApproximatorClient
{
    class ServerHandler
    {
        private static readonly Dictionary<string,string> SensorEndpointUrls = new Dictionary<string,string>();
        private const string BaseUrl = @"http://spcl.cloudapp.net:8080/";
        private const string Method = "PUT";
        private const string ContentType = "application/json";
        //Retrives the endpoint that the sensor sends data to
        public static void SetupConnection(string sensorName)
        {
            const string endPoint = BaseUrl + "register";
            var parameters = new Dictionary<string, string>{
                {"SensorName",sensorName},
                {"Time", "0"},
                {"Value","Init"}
            };
            var request = WebRequest.Create(endPoint);
            request.Method = Method;
            request.ContentType = ContentType;
            using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            {
                var json = JsonConvert.SerializeObject(parameters);
                streamWriter.Write(json);
                streamWriter.Flush();
                streamWriter.Close();
            }
            var httpResponse = request.GetResponse();
            using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
            {
                var result = streamReader.ReadToEnd();
                var resultDict = JsonConvert.DeserializeObject<Dictionary<string, string>>(result);
                SensorEndpointUrls.Add(sensorName, BaseUrl + Regex.Match(resultDict["Ok"], "sensor.*").Value);
            }
            
        }
        
        // Upload to infrastructure and return true if the upload was successfull
        public static bool UploadJson(string json)
        {
            var convertedJson = JsonConvert.DeserializeObject <Dictionary<string, string>>(json);
            if (!SensorEndpointUrls.ContainsKey(convertedJson["SensorName"])) SetupConnection(convertedJson["SensorName"]);
            Console.Out.WriteLine(json);
            var endPoint = SensorEndpointUrls[convertedJson["SensorName"]];
            var request = WebRequest.Create(endPoint);
            request.Method = Method;
            request.ContentType = ContentType;

            using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            {
                streamWriter.Write(json);
                streamWriter.Flush();
                streamWriter.Close();
            }
            var httpResponse = request.GetResponse();
            using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
            {
                var result = streamReader.ReadToEnd();
                Console.WriteLine(result);
            }
            return true;
        }
    }
}
