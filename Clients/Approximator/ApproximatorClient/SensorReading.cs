using System;

namespace ApproximatorClient
{
    class SensorRegistration
    {
        public string SensorName { get; set; }
        public string UserName { get; set; }
    }

    class SensorReading : IEquatable<SensorReading>
    {
        public string SensorName { get; set; }
        public string UserName { get; set; }
        public string Value { get; set; }

        public SensorRegistration GetSensorRegistration()
        {
            return new SensorRegistration{SensorName = SensorName, UserName = UserName};
        }

        public bool Equals(SensorReading other)
        {
            return SensorName == other.SensorName && UserName == other.UserName && Value == other.Value;
        }
    }
}
