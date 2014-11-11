using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace ApproximatorClient
{
    class ServerHandler
    {
        private static readonly Dictionary<string,string> SensorEndpointUrls = new Dictionary<string,string>();
//        private const string BaseUrl = @"http://localhost:8080/";
        private const string BaseUrl = @"http://spcl.cloudapp.net/";
        private const string Method = "PUT";
        private const string ContentType = "application/json";
        //Retrives the endpoint that the sensor sends data to
        public static void SetupConnection(SensorRegistration registration)
        {
            const string endPoint = BaseUrl + "register";            
            var request = WebRequest.Create(endPoint);
            request.Method = Method;
            request.ContentType = ContentType;
            using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            {
                var json = JsonConvert.SerializeObject(registration);
                streamWriter.Write(json);
                streamWriter.Flush();
                streamWriter.Close();
            }
            var httpResponse = request.GetResponse();
            using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
            {
                var result = streamReader.ReadToEnd();
                var resultDict = JsonConvert.DeserializeObject<Dictionary<string, string>>(result);
                SensorEndpointUrls.Add(registration.SensorName, BaseUrl + resultDict["Ok"]);
            }
            
        }
        
        // Upload to infrastructure and return true if the upload was successfull
        public static bool UploadJson(string json)
        {
            SensorReading sensorReading = null;
            try
            {
                Console.Out.WriteLine(json);
                sensorReading = JsonConvert.DeserializeObject<SensorReading>(json);
                if (!SensorEndpointUrls.ContainsKey(sensorReading.SensorName)) SetupConnection(sensorReading.GetSensorRegistration());
                var endPoint = SensorEndpointUrls[sensorReading.SensorName];
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
            }
            catch (Exception e)
            {
                if (sensorReading != null) SensorEndpointUrls.Remove(sensorReading.SensorName);
                Console.WriteLine("ERROR");
                Console.WriteLine("Message: {0}", e.Message);
                Console.WriteLine("Exiting");
                return false;
            }
            return true;
        }
    }
}
