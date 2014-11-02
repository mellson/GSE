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
        private static Dictionary<string,string> sensorEndpointUrls = new Dictionary<string,string>();
        private static string baseURL = @"http://spcl.cloudapp.net:8080/";
        private static string method = "PUT";
        private static string contentType = "application/json";
        //Retrives the endpoint that the sensor sends data to
        public static void SetupConnection(string sensorName)
        {
            string endPoint =  baseURL + "register";
            var parameters = new Dictionary<string, string>{
                {"SensorName",sensorName},
                {"Time", "0"},
                {"Value","Init"}
            };
            var request = (HttpWebRequest)WebRequest.Create(endPoint);

            request.Method = method;
            request.ContentType = contentType;

            using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            {
                string json = JsonConvert.SerializeObject(parameters);

                streamWriter.Write(json);
                streamWriter.Flush();
                streamWriter.Close();
            }
            var httpResponse = (HttpWebResponse)request.GetResponse();
            using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
            {
                var result = streamReader.ReadToEnd();
                var resultDict = JsonConvert.DeserializeObject<Dictionary<string, string>>(result);
                sensorEndpointUrls.Add(sensorName, baseURL + Regex.Match(resultDict["Ok"], "sensor.*").Value);
            }
            
        }
        
        // Upload to infrastructure and return true if the upload was successfull
        public static bool UploadJson(string json)
        {
            var convertedJson = JsonConvert.DeserializeObject <Dictionary<string, string>>(json);
            if (!sensorEndpointUrls.ContainsKey(convertedJson["SensorName"])) SetupConnection(convertedJson["SensorName"]);
            Console.Out.WriteLine(json);
            string endPoint = sensorEndpointUrls[convertedJson["SensorName"]];
            var request = (HttpWebRequest)WebRequest.Create(endPoint);

            request.Method = method;
            request.ContentType = contentType;

            using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            {
                streamWriter.Write(json);
                streamWriter.Flush();
                streamWriter.Close();
            }
            var httpResponse = (HttpWebResponse)request.GetResponse();
            using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
            {
                var result = streamReader.ReadToEnd();
                Console.WriteLine(result);
            }
            return true;
        }
    }
}
