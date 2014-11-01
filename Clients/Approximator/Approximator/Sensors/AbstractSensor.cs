using System;
using System.Globalization;

namespace Approximator.Sensors
{
    public abstract class AbstractSensor
    {
        internal SensorReading LastReading;

        internal static bool UploadToServer(string json)
        {
            return ServerHandler.UploadJson(json);
        }

        internal void UpdateReadingForSensorName(string sensorName)
        {
            LastReading = new SensorReading
            {
                SensorName = sensorName,
                Time = FormatDateTime(DateTime.Now)
            };
        }

        internal static string FormatDateTime(DateTime dateTime)
        {
            return dateTime.ToString("HH:mm:ss fff", CultureInfo.CurrentUICulture);
        }
    }
}
