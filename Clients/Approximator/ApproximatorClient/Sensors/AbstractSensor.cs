using System;
using System.Globalization;
using System.Reactive.Linq;
using Newtonsoft.Json;

namespace ApproximatorClient.Sensors
{
    public abstract class AbstractSensor
    {
        internal string SensorName;
        internal SensorReading LastReading;

        internal static bool UploadToServer(string json)
        {
            return ServerHandler.UploadJson(json);
        }

        internal void UpdateReadingForSensorName(string name, string value)
        {
            LastReading = new SensorReading
            {
                SensorName = name,
                Value = value,
                Time = FormatDateTime(DateTime.Now)
            };
        }

        internal static string FormatDateTime(DateTime dateTime)
        {
            return dateTime.ToString("HH:mm:ss fff", CultureInfo.CurrentUICulture);
        }

        public void StartUploadingEverySecond()
        {
            var updateEverySecond = Observable.Interval(TimeSpan.FromSeconds(1));
            updateEverySecond.Subscribe(UploadHelper);
        }

        private SensorReading _lastUploadedReading;
        private void UploadHelper(long number)
        {
            if (_lastUploadedReading == LastReading) return;
            _lastUploadedReading = LastReading;
            UploadToServer(JsonConvert.SerializeObject(LastReading));
        }
    }
}
