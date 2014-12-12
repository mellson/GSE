using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using Newtonsoft.Json;
using log4net;

namespace ApproximatorClient
{
    class ServerHandler
    {
        private static readonly Dictionary<string,string> SensorEndpointUrls = new Dictionary<string,string>();
        private const string BaseUrl = @"http://localhost:8080/";
//        private const string BaseUrl = @"http://spcl.cloudapp.net/";
        private const string Method = "PUT";
        private const string ContentType = "application/json";
        private static readonly ILog dataLog = LogManager.GetLogger("DataLogger");

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
            var stream = httpResponse.GetResponseStream();
            if (stream == null) return;
            using (var streamReader = new StreamReader(stream))
            {
                var result = streamReader.ReadToEnd();
                var resultDict = JsonConvert.DeserializeObject<Dictionary<string, string>>(result);
                SensorEndpointUrls.Add(registration.SensorName, BaseUrl + resultDict["Ok"]);
            }
        }
        
        // Upload to infrastructure and return true if the upload was successful
        public static bool UploadJson(string json)
        {
            SensorReading sensorReading = null;
            try
            {
                dataLog.Info(json);
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
                var stream = httpResponse.GetResponseStream();
                if (stream != null)
                    using (var streamReader = new StreamReader(stream))
                    {
                        var result = streamReader.ReadToEnd();
//                        Console.WriteLine(result);
                    }
            }
            catch (Exception e)
            {
                if (sensorReading != null) SensorEndpointUrls.Remove(sensorReading.SensorName);
                dataLog.InfoFormat(@"Message: {0}", e.Message);
                return false;
            }
            return true;
        }
    }
}
