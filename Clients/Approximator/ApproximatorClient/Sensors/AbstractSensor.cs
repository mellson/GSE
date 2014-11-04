using System;
using System.Reactive.Linq;
using Newtonsoft.Json;

namespace ApproximatorClient.Sensors
{
    public abstract class AbstractSensor
    {
        private const double SecondsBetweenUploads = 1;
        internal string SensorName;
        internal string UserName;
        internal SensorReading LastReading;

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
            var updateEverySecond = Observable.Interval(TimeSpan.FromSeconds(SecondsBetweenUploads));
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
