namespace Approximator.Sensors
{
    abstract class AbstractSensor
    {
        internal static bool UploadToServer(string json)
        {
            return ServerHandler.UploadJson(json);
        }
    }
}
