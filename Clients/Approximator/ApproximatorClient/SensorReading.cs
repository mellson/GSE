namespace ApproximatorClient
{
    class SensorRegistration
    {
        public string SensorName { get; set; }
        public string UserName { get; set; }
    }

    class SensorReading
    {
        public string SensorName { get; set; }
        public string UserName { get; set; }
        public string Value { get; set; }

        public SensorRegistration GetSensorRegistration()
        {
            return new SensorRegistration{SensorName = SensorName, UserName = UserName};
        }
    }
}
