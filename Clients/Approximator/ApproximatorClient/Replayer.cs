using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using ApproximatorClient.Sensors;
using Newtonsoft.Json;

namespace ApproximatorClient
{

    class Replayer
    {
        public Replayer(string pathToLog)
        {
            DateTime? startTime = null;
            const string startString = "SensorReadingWithTime(";
            var readings = new List<Reading>();
            var log = File.ReadLines(pathToLog);
            foreach (var s in log)
            {
                if (s.StartsWith(startString))
                {
                    var content = s.Replace(startString, "");
                    var contents = content.Substring(0, content.Length - 1).Split(',');
                    var sensor = contents[0];
                    var user = contents[1];
                    var time = DateTime.Parse(contents[2]).ToUniversalTime();
                    startTime = startTime ?? time;
                    var value = contents[3];
                    readings.Add(new Reading{Sensor = sensor, User = user, Time = time, Value = value});
                }
                else if (s.Contains("Prompted user"))
                    readings.Add(new Reading {Prompt = true});
            }
            for (var i = 0; i < readings.Count - 1; i++)
            {
                if (readings[i].Prompt) continue;
                var thisTime = readings[i].Time;
                var nextTime = readings[i + 1].Time;
                var difference = nextTime.Subtract(thisTime).Milliseconds;
                if (difference > 0)
                    readings[i].SleepUntilNextReading = difference;
            }
            foreach (var reading in readings)
            {
                if (reading.Prompt)
                {
                    Console.Out.WriteLine("Prompted user");
                    continue;
                }
                var sensorReading = new SensorReading
                {
                    SensorName = reading.Sensor,
                    UserName = reading.User,
                    Value = reading.Value
                };
                ServerHandler.UploadJson(JsonConvert.SerializeObject(sensorReading));
                Thread.Sleep(reading.SleepUntilNextReading);
            }
        }
    }

    internal class Reading
    {
        public string Sensor { get; set; }
        public string User { get; set; }
        public DateTime Time { get; set; }
        public string Value { get; set; }
        public bool Prompt { get; set; }
        public int SleepUntilNextReading { get; set; }
    }
}
