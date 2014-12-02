using System;
using System.Reactive.Linq;
using Newtonsoft.Json;

namespace ApproximatorClient.Sensors
{
    public abstract class AbstractSensor
    {
        internal string SensorName;
        internal string UserName;
        internal SensorReading LastReading;
        internal TimeSpan UploadInterval = TimeSpan.FromMilliseconds(200);

        internal static bool UploadToServer(string json)
        {
            return ServerHandler.UploadJson(json);
        }

        internal void UpdateReadingForSensorName(string name, string username, string value)
        {
            LastReading = new SensorReading
            {
                SensorName = name,
                Value = value,
                UserName = username
            };
        }

        public void StartUploading()
        {
            var updateEverySecond = Observable.Interval(UploadInterval);
            updateEverySecond.Subscribe(UploadHelper);
        }

        private SensorReading _lastUploadedReading = new SensorReading();
        private void UploadHelper(long number)
        {
            if (_lastUploadedReading.Equals(LastReading)) return;
            _lastUploadedReading = LastReading;
            UploadToServer(JsonConvert.SerializeObject(LastReading));
        }
    }
}
