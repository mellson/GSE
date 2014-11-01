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
        private static string sensorEndpointUrl;
        private static string baseURL = @"http://spcl.cloudapp.net:8080/";
        private static string method = "PUT";
        private static string contentType = "application/json";
        //Retrives the endpoint that the sensor sends data to
        public static void SetupConnection()
        {
            string endPoint =  baseURL + "register";
            var parameters = new Dictionary<string, string>{
                {"name","sensor1"},
                {"user","user1"},
                {"jsonData","data"}
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
                sensorEndpointUrl = baseURL + Regex.Match(resultDict["Ok"], "sensor.*").Value;
            }
            
        }
        
        // Upload to infrastructure and return true if the upload was successfull
        public static bool UploadJson(string json)
        {
            if (String.IsNullOrEmpty(sensorEndpointUrl)) SetupConnection();
            Console.Out.WriteLine(json);
            string endPoint = sensorEndpointUrl;
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
            }
            return true;
        }
    }
}
